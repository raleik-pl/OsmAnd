package net.osmand.plus.backup;

import androidx.annotation.NonNull;

import net.osmand.FileUtils;
import net.osmand.OperationLog;
import net.osmand.PlatformUtil;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.backup.PrepareBackupResult.RemoteFilesType;
import net.osmand.plus.settings.backend.backup.SettingsItemReader;
import net.osmand.plus.settings.backend.backup.SettingsItemType;
import net.osmand.plus.settings.backend.backup.SettingsItemsFactory;
import net.osmand.plus.settings.backend.backup.items.FileSettingsItem;
import net.osmand.plus.settings.backend.backup.items.FileSettingsItem.FileSubtype;
import net.osmand.plus.settings.backend.backup.items.GpxSettingsItem;
import net.osmand.plus.settings.backend.backup.items.SettingsItem;
import net.osmand.util.Algorithms;

import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static net.osmand.plus.backup.BackupHelper.INFO_EXT;

class BackupImporter {

	private static final Log LOG = PlatformUtil.getLog(BackupImporter.class);

	private static final int THREAD_POOL_SIZE = 4;

	private final BackupHelper backupHelper;

	private boolean cancelled;

	private final ExecutorService executor = new AsyncReaderTaskExecutor();
	private final Map<Future<?>, AsyncReaderTask> tasks = new ConcurrentHashMap<>();
	private final Map<Future<?>, Throwable> exceptions = new ConcurrentHashMap<>();

	public static class CollectItemsResult {
		public List<SettingsItem> items;
		public List<RemoteFile> remoteFiles;
	}

	BackupImporter(@NonNull BackupHelper backupHelper) {
		this.backupHelper = backupHelper;
	}

	@NonNull
	CollectItemsResult collectItems(boolean readItems) throws IllegalArgumentException, IOException {
		CollectItemsResult result = new CollectItemsResult();
		StringBuilder error = new StringBuilder();
		OperationLog operationLog = new OperationLog("collectRemoteItems", BackupHelper.DEBUG);
		operationLog.startOperation();
		try {
			backupHelper.downloadFileList((status, message, remoteFiles) -> {
				if (status == BackupHelper.STATUS_SUCCESS) {
					result.remoteFiles = remoteFiles;
					try {
						result.items = getRemoteItems(remoteFiles, readItems);
					} catch (IOException e) {
						error.append(e.getMessage());
					}
				} else {
					error.append(message);
				}
			});
		} catch (UserNotRegisteredException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
		operationLog.finishOperation();
		if (!Algorithms.isEmpty(error)) {
			throw new IOException(error.toString());
		}
		return result;
	}

	void importItems(@NonNull List<SettingsItem> items, boolean forceReadData) throws IllegalArgumentException {
		if (Algorithms.isEmpty(items)) {
			throw new IllegalArgumentException("No items");
		}
		Collection<RemoteFile> remoteFiles = backupHelper.getBackup().getRemoteFiles(RemoteFilesType.UNIQUE).values();
		if (Algorithms.isEmpty(remoteFiles)) {
			throw new IllegalArgumentException("No remote files");
		}
		OperationLog operationLog = new OperationLog("importItems", BackupHelper.DEBUG);
		operationLog.startOperation();
		Map<RemoteFile, SettingsItem> remoteFileItems = new HashMap<>();
		for (RemoteFile remoteFile : remoteFiles) {
			SettingsItem item = null;
			for (SettingsItem settingsItem : items) {
				String fileName = remoteFile.item != null ? remoteFile.item.getFileName() : null;
				if (fileName != null && settingsItem.applyFileName(fileName)) {
					item = settingsItem;
					remoteFileItems.put(remoteFile, item);
					break;
				}
			}
			if (item != null && (!item.shouldReadOnCollecting() || forceReadData)) {
				AsyncReaderTask task = new AsyncReaderTask(remoteFile, item, forceReadData);
				Future<?> future = executor.submit(task);
				tasks.put(future, task);
			}
		}

		executor.shutdown();
		boolean finished = false;
		while (!finished) {
			try {
				finished = executor.awaitTermination(100, TimeUnit.MILLISECONDS);
				if (isCancelled() || isInterrupted()) {
					for (Future<?> future : tasks.keySet()) {
						future.cancel(false);
					}
				}
			} catch (InterruptedException e) {
				// ignore
			}
		}

		for (Entry<RemoteFile, SettingsItem> fileItem : remoteFileItems.entrySet()) {
			fileItem.getValue().setLocalModifiedTime(fileItem.getKey().getClienttimems());
		}
		operationLog.finishOperation();
	}

	private void importItem(@NonNull RemoteFile remoteFile, @NonNull SettingsItem item, boolean forceReadData) {
		OsmandApplication app = backupHelper.getApp();
		File tempDir = FileUtils.getTempDir(app);
		FileInputStream is = null;
		try {
			SettingsItemReader<? extends SettingsItem> reader = item.getReader();
			if (reader != null) {
				String fileName = remoteFile.getTypeNamePath();
				File tempFile = new File(tempDir, fileName);
				Map<File, RemoteFile> map = new HashMap<>();
				map.put(tempFile, remoteFile);
				Map<File, String> errors = backupHelper.downloadFiles(map);
				if (errors.isEmpty()) {
					is = new FileInputStream(tempFile);
					reader.readFromStream(is, remoteFile.getName());
					if (forceReadData) {
						item.apply();
					}
					backupHelper.updateFileUploadTime(remoteFile.getType(), remoteFile.getName(), remoteFile.getClienttimems());
					if (item instanceof FileSettingsItem) {
						String itemFileName = BackupHelper.getFileItemName((FileSettingsItem) item);
						if (app.getAppPath(itemFileName).isDirectory()) {
							backupHelper.updateFileUploadTime(item.getType().name(), itemFileName,
									remoteFile.getClienttimems());
						}
					}
				} else {
					throw new IOException("Error reading temp item file " + fileName + ": " +
							errors.values().iterator().next());
				}
			}
			item.applyAdditionalParams(reader);
		} catch (IllegalArgumentException e) {
			item.getWarnings().add(app.getString(R.string.settings_item_read_error, item.getName()));
			LOG.error("Error reading item data: " + item.getName(), e);
		} catch (IOException e) {
			item.getWarnings().add(app.getString(R.string.settings_item_read_error, item.getName()));
			LOG.error("Error reading item data: " + item.getName(), e);
		} catch (UserNotRegisteredException e) {
			item.getWarnings().add(app.getString(R.string.settings_item_read_error, item.getName()));
			LOG.error("Error reading item data: " + item.getName(), e);
		} finally {
			Algorithms.closeStream(is);
		}
	}

	@NonNull
	private List<SettingsItem> getRemoteItems(@NonNull List<RemoteFile> remoteFiles, boolean readItems) throws IllegalArgumentException, IOException {
		if (remoteFiles.isEmpty()) {
			return Collections.emptyList();
		}
		List<SettingsItem> items = new ArrayList<>();
		try {
			OperationLog operationLog = new OperationLog("getRemoteItems", BackupHelper.DEBUG);
			operationLog.startOperation();
			JSONObject json = new JSONObject();
			JSONArray itemsJson = new JSONArray();
			json.put("items", itemsJson);
			Map<File, RemoteFile> remoteInfoFilesMap = new HashMap<>();
			Map<String, RemoteFile> remoteItemFilesMap = new HashMap<>();
			List<RemoteFile> remoteInfoFiles = new ArrayList<>();
			Set<String> remoteInfoNames = new HashSet<>();
			List<RemoteFile> noInfoRemoteItemFiles = new ArrayList<>();
			OsmandApplication app = backupHelper.getApp();
			File tempDir = FileUtils.getTempDir(app);

			List<RemoteFile> uniqueRemoteFiles = new ArrayList<>();
			Set<String> uniqueFileIds = new TreeSet<>();
			for (RemoteFile rf : remoteFiles) {
				String fileId = rf.getTypeNamePath();
				if (uniqueFileIds.add(fileId) && !rf.isDeleted()) {
					uniqueRemoteFiles.add(rf);
				}
			}
			operationLog.log("build uniqueRemoteFiles");

			Map<String, Long> infoMap = backupHelper.getDbHelper().getUploadedFileInfoMap();
			for (RemoteFile remoteFile : uniqueRemoteFiles) {
				Long uploadTime = infoMap.get(remoteFile.getType() + "___" + remoteFile.getName());
				if (uploadTime != null && uploadTime == remoteFile.getClienttimems()) {
					//continue;
				}
				String fileName = remoteFile.getTypeNamePath();
				if (fileName.endsWith(INFO_EXT)) {
					if (readItems) {
						remoteInfoFilesMap.put(new File(tempDir, fileName), remoteFile);
					}
					String itemFileName = fileName.substring(0, fileName.length() - INFO_EXT.length());
					remoteInfoNames.add(itemFileName);
					remoteInfoFiles.add(remoteFile);
				} else if (!remoteItemFilesMap.containsKey(fileName)) {
					remoteItemFilesMap.put(fileName, remoteFile);
				}
			}
			operationLog.log("build maps");

			for (Entry<String, RemoteFile> remoteFileEntry : remoteItemFilesMap.entrySet()) {
				String itemFileName = remoteFileEntry.getKey();
				boolean hasInfo = false;
				for (String remoteInfoName : remoteInfoNames) {
					if (itemFileName.equals(remoteInfoName) || itemFileName.startsWith(remoteInfoName + "/")) {
						hasInfo = true;
						break;
					}
				}
				if (!hasInfo) {
					noInfoRemoteItemFiles.add(remoteFileEntry.getValue());
				}
			}
			operationLog.log("build noInfoRemoteItemFiles");

			if (readItems) {
				generateItemsJson(itemsJson, remoteInfoFilesMap, noInfoRemoteItemFiles);
			} else {
				generateItemsJson(itemsJson, remoteInfoFiles, noInfoRemoteItemFiles);
			}
			operationLog.log("generateItemsJson");

			SettingsItemsFactory itemsFactory = new SettingsItemsFactory(app, json);
			operationLog.log("create setting items");
			List<SettingsItem> settingsItemList = itemsFactory.getItems();
			if (settingsItemList.isEmpty()) {
				return Collections.emptyList();
			}
			updateFilesInfo(remoteItemFilesMap, settingsItemList);
			items.addAll(settingsItemList);
			operationLog.log("updateFilesInfo");

			if (readItems) {
				Map<RemoteFile, SettingsItemReader<? extends SettingsItem>> remoteFilesForRead = new HashMap<>();
				for (SettingsItem item : settingsItemList) {
					if (item.shouldReadOnCollecting()) {
						List<RemoteFile> foundRemoteFiles = getItemRemoteFiles(item, remoteItemFilesMap);
						for (RemoteFile remoteFile : foundRemoteFiles) {
							SettingsItemReader<? extends SettingsItem> reader = item.getReader();
							if (reader != null) {
								remoteFilesForRead.put(remoteFile, reader);
							}
						}
					}
				}
				Map<File, RemoteFile> remoteFilesForDownload = new HashMap<>();
				for (RemoteFile remoteFile : remoteFilesForRead.keySet()) {
					String fileName = remoteFile.getTypeNamePath();
					remoteFilesForDownload.put(new File(tempDir, fileName), remoteFile);
				}
				if (!remoteFilesForDownload.isEmpty()) {
					downloadAndReadItemFiles(remoteFilesForRead, remoteFilesForDownload);
				}
				operationLog.log("readItems");
			}
			operationLog.finishOperation();
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Error reading items", e);
		} catch (JSONException e) {
			throw new IllegalArgumentException("Error parsing items", e);
		} catch (UserNotRegisteredException e) {
			throw new IllegalArgumentException("User is not registered", e);
		} catch (IOException e) {
			throw new IOException(e);
		}
		return items;
	}

	@NonNull
	private List<RemoteFile> getItemRemoteFiles(@NonNull SettingsItem item, @NonNull Map<String, RemoteFile> remoteFiles) {
		List<RemoteFile> res = new ArrayList<>();
		String fileName = item.getFileName();
		if (!Algorithms.isEmpty(fileName)) {
			if (fileName.charAt(0) != '/') {
				fileName = "/" + fileName;
			}
			if (item instanceof GpxSettingsItem) {
				GpxSettingsItem gpxItem = (GpxSettingsItem) item;
				String folder = gpxItem.getSubtype().getSubtypeFolder();
				if (!Algorithms.isEmpty(folder) && folder.charAt(0) != '/') {
					folder = "/" + folder;
				}
				if (fileName.startsWith(folder)) {
					fileName = fileName.substring(folder.length() - 1);
				}
			}
			String typeFileName = item.getType().name() + fileName;
			RemoteFile remoteFile = remoteFiles.remove(typeFileName);
			if (remoteFile != null) {
				res.add(remoteFile);
			}
			Iterator<Entry<String, RemoteFile>> it = remoteFiles.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, RemoteFile> fileEntry = it.next();
				String remoteFileName = fileEntry.getKey();
				if (remoteFileName.startsWith(typeFileName + "/")) {
					res.add(fileEntry.getValue());
					it.remove();
				}
			}
		}
		return res;
	}

	private void generateItemsJson(@NonNull JSONArray itemsJson,
								   @NonNull List<RemoteFile> remoteInfoFiles,
								   @NonNull List<RemoteFile> noInfoRemoteItemFiles) throws JSONException {
		for (RemoteFile remoteFile : remoteInfoFiles) {
			String fileName = remoteFile.getName();
			fileName = fileName.substring(0, fileName.length() - INFO_EXT.length());
			String type = remoteFile.getType();
			JSONObject itemJson = new JSONObject();
			itemJson.put("type", type);
			if (SettingsItemType.GPX.name().equals(type)) {
				fileName = FileSubtype.GPX.getSubtypeFolder() + fileName;
			}
			if (SettingsItemType.PROFILE.name().equals(type)) {
				JSONObject appMode = new JSONObject();
				String name = fileName.replaceFirst("profile_", "");
				if (name.endsWith(".json")) {
					name = name.substring(0, name.length() - 5);
				}
				appMode.put("stringKey", name);
				itemJson.put("appMode", appMode);
			}
			itemJson.put("file", fileName);
			itemsJson.put(itemJson);
		}
		addRemoteFilesToJson(itemsJson, noInfoRemoteItemFiles);
	}

	private void generateItemsJson(@NonNull JSONArray itemsJson,
								   @NonNull Map<File, RemoteFile> remoteInfoFiles,
								   @NonNull List<RemoteFile> noInfoRemoteItemFiles) throws UserNotRegisteredException, JSONException, IOException {
		Map<File, String> errors = backupHelper.downloadFiles(remoteInfoFiles);
		if (errors.isEmpty()) {
			for (File file : remoteInfoFiles.keySet()) {
				String jsonStr = Algorithms.getFileAsString(file);
				if (!Algorithms.isEmpty(jsonStr)) {
					itemsJson.put(new JSONObject(jsonStr));
				} else {
					throw new IOException("Error reading item info: " + file.getName());
				}
			}
		} else {
			throw new IOException("Error downloading items info");
		}
		addRemoteFilesToJson(itemsJson, noInfoRemoteItemFiles);
	}

	private void addRemoteFilesToJson(@NonNull JSONArray itemsJson, @NonNull List<RemoteFile> noInfoRemoteItemFiles) throws JSONException {
		Set<String> fileItems = new HashSet<>();
		for (RemoteFile remoteFile : noInfoRemoteItemFiles) {
			String type = remoteFile.getType();
			String fileName = remoteFile.getName();
			if (type.equals(SettingsItemType.FILE.name()) && fileName.startsWith(FileSubtype.VOICE.getSubtypeFolder())) {
				FileSubtype subtype = FileSubtype.getSubtypeByFileName(fileName);
				int lastSeparatorIndex = fileName.lastIndexOf('/');
				if (lastSeparatorIndex > 0) {
					fileName = fileName.substring(0, lastSeparatorIndex);
				}
				String typeName = subtype + "___" + fileName;
				if (!fileItems.contains(typeName)) {
					fileItems.add(typeName);
					JSONObject itemJson = new JSONObject();
					itemJson.put("type", type);
					itemJson.put("file", fileName);
					itemJson.put("subtype", subtype);
					itemsJson.put(itemJson);
				}
			} else {
				JSONObject itemJson = new JSONObject();
				itemJson.put("type", type);
				itemJson.put("file", fileName);
				itemsJson.put(itemJson);
			}
		}
	}

	private void downloadAndReadItemFiles(@NonNull Map<RemoteFile, SettingsItemReader<? extends SettingsItem>> remoteFilesForRead,
										  @NonNull Map<File, RemoteFile> remoteFilesForDownload) throws UserNotRegisteredException, IOException {
		OsmandApplication app = backupHelper.getApp();
		Map<File, String> errors = backupHelper.downloadFiles(remoteFilesForDownload);
		if (errors.isEmpty()) {
			for (Entry<File, RemoteFile> entry : remoteFilesForDownload.entrySet()) {
				File tempFile = entry.getKey();
				RemoteFile remoteFile = entry.getValue();
				if (tempFile.exists()) {
					SettingsItemReader<? extends SettingsItem> reader = remoteFilesForRead.get(remoteFile);
					if (reader != null) {
						SettingsItem item = reader.getItem();
						FileInputStream is = new FileInputStream(tempFile);
						try {
							reader.readFromStream(is, item.getFileName());
							item.applyAdditionalParams(reader);
						} catch (IllegalArgumentException e) {
							item.getWarnings().add(app.getString(R.string.settings_item_read_error, item.getName()));
							LOG.error("Error reading item data: " + item.getName(), e);
						} catch (IOException e) {
							item.getWarnings().add(app.getString(R.string.settings_item_read_error, item.getName()));
							LOG.error("Error reading item data: " + item.getName(), e);
						} finally {
							Algorithms.closeStream(is);
						}
					} else {
						throw new IOException("No reader for: " + tempFile.getName());
					}
				} else {
					throw new IOException("No temp item file: " + tempFile.getName());
				}
			}
		} else {
			throw new IOException("Error downloading temp item files");
		}
	}

	private void updateFilesInfo(@NonNull Map<String, RemoteFile> remoteFiles,
								 @NonNull List<SettingsItem> settingsItemList) {
		Map<String, RemoteFile> remoteFilesMap = new HashMap<>(remoteFiles);
		for (SettingsItem settingsItem : settingsItemList) {
			List<RemoteFile> foundRemoteFiles = getItemRemoteFiles(settingsItem, remoteFilesMap);
			for (RemoteFile remoteFile : foundRemoteFiles) {
				settingsItem.setLastModifiedTime(remoteFile.getClienttimems());
				remoteFile.item = settingsItem;
				if (settingsItem instanceof FileSettingsItem) {
					FileSettingsItem fileSettingsItem = (FileSettingsItem) settingsItem;
					fileSettingsItem.setSize(remoteFile.getFilesize());
				}
			}
		}
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	private boolean isInterrupted() {
		return !exceptions.isEmpty();
	}

	private class AsyncReaderTask implements Callable<Void> {

		private final RemoteFile remoteFile;
		private final SettingsItem item;
		private final boolean forceReadData;

		public AsyncReaderTask(@NonNull RemoteFile remoteFile, @NonNull SettingsItem item, boolean forceReadData) {
			this.remoteFile = remoteFile;
			this.item = item;
			this.forceReadData = forceReadData;
		}

		@Override
		public Void call() throws Exception {
			importItem(remoteFile, item, forceReadData);
			return null;
		}
	}

	private class AsyncReaderTaskExecutor extends ThreadPoolExecutor {

		public AsyncReaderTaskExecutor() {
			super(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
		}

		protected void afterExecute(Runnable r, Throwable t) {
			super.afterExecute(r, t);
			if (r instanceof Future<?>) {
				if (t == null && ((Future<?>) r).isDone()) {
					try {
						((Future<?>) r).get();
					} catch (CancellationException | InterruptedException e) {
						// ignore
					} catch (ExecutionException ee) {
						exceptions.put((Future<?>) r, ee.getCause());
					}
				} else {
					exceptions.put((Future<?>) r, t);
				}
			}
		}
	}
}