package net.osmand.plus.download.local;

import static net.osmand.IndexConstants.BACKUP_INDEX_DIR;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.osmand.IndexConstants;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.utils.FileUtils;

import java.io.File;

public class LocalFileItem extends LocalItem {

	private final File file;
	private final String path;
	private final String fileName;
	private final long size;

	@Nullable
	private Object attachedObject;
	private long lastModified;


	public LocalFileItem(@NonNull File file, @NonNull LocalItemType type) {
		super(type);
		this.file = file;
		this.fileName = file.getName();
		this.path = file.getAbsolutePath();
		this.size = file.length();
		this.lastModified = file.lastModified();
	}

	@NonNull
	public File getFile() {
		return file;
	}

	@NonNull
	public String getPath() {
		return path;
	}

	@NonNull
	public String getFileName() {
		return fileName;
	}

	public long getSize() {
		return size;
	}

	public boolean isBackuped(@NonNull OsmandApplication app) {
		File backupDir = FileUtils.getExistingDir(app, BACKUP_INDEX_DIR);
		File hiddenBackupDir = app.getAppInternalPath(IndexConstants.HIDDEN_BACKUP_DIR);
		return path.startsWith(backupDir.getAbsolutePath()) || path.startsWith(hiddenBackupDir.getAbsolutePath());
	}

	public boolean isHidden(@NonNull OsmandApplication app) {
		File hiddenDir = app.getAppInternalPath(IndexConstants.HIDDEN_DIR);
		return path.startsWith(hiddenDir.getAbsolutePath());
	}

	@Nullable
	public Object getAttachedObject() {
		return attachedObject;
	}

	public void setAttachedObject(@Nullable Object object) {
		this.attachedObject = object;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	@NonNull
	@Override
	public String toString() {
		return fileName;
	}
}