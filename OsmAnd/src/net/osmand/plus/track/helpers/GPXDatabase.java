package net.osmand.plus.track.helpers;

import static net.osmand.IndexConstants.GPX_INDEX_DIR;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_API_IMPORTED;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_AVG_ELEVATION;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_AVG_SPEED;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_COLOR;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_COLORING_TYPE;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_DIFF_ELEVATION_DOWN;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_DIFF_ELEVATION_UP;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_DIR;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_END_TIME;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_FILE_CREATION_TIME;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_FILE_LAST_MODIFIED_TIME;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_FILE_LAST_UPLOADED_TIME;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_GRADIENT_ALTITUDE_COLOR;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_GRADIENT_SLOPE_COLOR;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_GRADIENT_SPEED_COLOR;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_JOIN_SEGMENTS;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_MAX_ELEVATION;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_MAX_FILTER_ALTITUDE;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_MAX_FILTER_HDOP;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_MAX_FILTER_SPEED;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_MAX_SPEED;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_MIN_ELEVATION;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_MIN_FILTER_ALTITUDE;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_MIN_FILTER_SPEED;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_NAME;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_NEAREST_CITY_NAME;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_POINTS;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_SHOW_ARROWS;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_SHOW_AS_MARKERS;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_SHOW_START_FINISH;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_SMOOTHING_THRESHOLD;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_SPLIT_INTERVAL;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_SPLIT_TYPE;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_START_LAT;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_START_LON;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_START_TIME;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_TIME_MOVING;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_TIME_SPAN;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_TOTAL_DISTANCE;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_TOTAL_DISTANCE_MOVING;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_TOTAL_TRACKS;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_WIDTH;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_WPT_CATEGORY_NAMES;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_WPT_POINTS;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.osmand.PlatformUtil;
import net.osmand.data.LatLon;
import net.osmand.gpx.GPXTrackAnalysis;
import net.osmand.gpx.GPXUtilities;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.api.SQLiteAPI.SQLiteConnection;
import net.osmand.plus.api.SQLiteAPI.SQLiteCursor;
import net.osmand.plus.routing.ColoringType;
import net.osmand.plus.track.GradientScaleType;
import net.osmand.plus.utils.AndroidDbUtils;
import net.osmand.util.Algorithms;

import org.apache.commons.logging.Log;
import org.bouncycastle.util.Arrays;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GPXDatabase {

	public static final Log LOG = PlatformUtil.getLog(GPXDatabase.class);

	private static final int DB_VERSION = 16;
	private static final String DB_NAME = "gpx_database";

	private static final String GPX_TABLE_NAME = "gpxTable";
	private static final String GPX_INDEX_NAME_DIR = "indexNameDir";

	private static final String TMP_NAME_COLUMN_COUNT = "itemsCount";
	private static final String TMP_NAME_COLUMN_NOT_NULL = "nonnull";

	public static final long UNKNOWN_TIME_THRESHOLD = 10;

	private static final String GPX_UPDATE_PARAMETERS_START = "UPDATE " + GPX_TABLE_NAME + " SET ";
	private static final String GPX_FIND_BY_NAME_AND_DIR = " WHERE " + GPX_COL_NAME.getColumnName() + " = ? AND " + GPX_COL_DIR.getColumnName() + " = ?";

	private static final String GPX_MIN_CREATE_DATE = "SELECT " +
			"MIN(" + GPX_COL_FILE_CREATION_TIME.getColumnName() + ") " +
			" FROM " + GPX_TABLE_NAME + " WHERE " + GPX_COL_FILE_CREATION_TIME.getColumnName() +
			" > " + UNKNOWN_TIME_THRESHOLD;

	private static final String GPX_MAX_TRACK_DURATION = "SELECT " +
			"MAX(" + GPX_COL_TOTAL_DISTANCE.getColumnName() + ") " +
			" FROM " + GPX_TABLE_NAME;

	private static final String GPX_TRACK_FOLDERS_COLLECTION = "SELECT " +
			GPX_COL_DIR.getColumnName() + ", count (*) as " + TMP_NAME_COLUMN_COUNT +
			" FROM " + GPX_TABLE_NAME +
			" group by " + GPX_COL_DIR.getColumnName() +
			" ORDER BY " + GPX_COL_DIR.getColumnName() + " ASC";

	private static final String GPX_TRACK_NEAREST_CITIES_COLLECTION = "SELECT " +
			GPX_COL_NEAREST_CITY_NAME.getColumnName() + ", count (*) as " + TMP_NAME_COLUMN_COUNT +
			" FROM " + GPX_TABLE_NAME +
			" WHERE " + GPX_COL_NEAREST_CITY_NAME.getColumnName() + " NOT NULL" + " AND " +
			GPX_COL_NEAREST_CITY_NAME.getColumnName() + " <> '' " +
			" group by " + GPX_COL_NEAREST_CITY_NAME.getColumnName() +
			" ORDER BY " + TMP_NAME_COLUMN_COUNT + " DESC";

	private static final String GPX_TRACK_COLORS_COLLECTION = "SELECT DISTINCT " +
			"case when " + GPX_COL_COLOR.getColumnName() + " is null then '' else " + GPX_COL_COLOR.getColumnName() + " end as " + TMP_NAME_COLUMN_NOT_NULL + ", " +
			"count (*) as " + TMP_NAME_COLUMN_COUNT +
			" FROM " + GPX_TABLE_NAME +
			" group by " + TMP_NAME_COLUMN_NOT_NULL +
			" ORDER BY " + TMP_NAME_COLUMN_COUNT + " DESC";

	private static final String GPX_TRACK_WIDTH_COLLECTION = "SELECT DISTINCT " +
			"case when " + GPX_COL_WIDTH.getColumnName() + " is null then '' else " + GPX_COL_WIDTH.getColumnName() + " end as " + TMP_NAME_COLUMN_NOT_NULL + ", " +
			"count (*) as " + TMP_NAME_COLUMN_COUNT +
			" FROM " + GPX_TABLE_NAME +
			" group by " + TMP_NAME_COLUMN_NOT_NULL +
			" ORDER BY " + TMP_NAME_COLUMN_COUNT + " DESC";

	private final OsmandApplication app;

	GPXDatabase(@NonNull OsmandApplication app) {
		this.app = app;
		// init database
		SQLiteConnection db = openConnection(false);
		if (db != null) {
			db.close();
		}
	}

	SQLiteConnection openConnection(boolean readonly) {
		SQLiteConnection conn = app.getSQLiteAPI().getOrCreateDatabase(DB_NAME, readonly);
		if (conn == null) {
			return null;
		}
		if (conn.getVersion() < DB_VERSION) {
			if (readonly) {
				conn.close();
				conn = app.getSQLiteAPI().getOrCreateDatabase(DB_NAME, false);
			}
			int version = conn.getVersion();
			conn.setVersion(DB_VERSION);
			if (version == 0) {
				onCreate(conn);
			} else {
				onUpgrade(conn, version, DB_VERSION);
			}
		}
		return conn;
	}

	private void onCreate(SQLiteConnection db) {
		db.execSQL(getCreateTableQuery());
		db.execSQL("CREATE INDEX IF NOT EXISTS " + GPX_INDEX_NAME_DIR + " ON " + GPX_TABLE_NAME + " (" + GPX_COL_NAME.getColumnName() + ", " + GPX_COL_DIR.getColumnName() + ");");
	}

	@NonNull
	private String getCreateTableQuery() {
		StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS " + GPX_TABLE_NAME + " (");
		Iterator<GpxParameter> iterator = new Arrays.Iterator<>(GpxParameter.values());
		while (iterator.hasNext()) {
			GpxParameter parameter = iterator.next();
			builder.append(" ").append(parameter.getColumnName()).append(" ").append(parameter.getColumnType());

			if (iterator.hasNext()) {
				builder.append(", ");
			} else {
				builder.append(");");
			}
		}
		return builder.toString();
	}

	@NonNull
	private String getSelectQuery() {
		StringBuilder builder = new StringBuilder("SELECT ");
		Iterator<GpxParameter> iterator = new Arrays.Iterator<>(GpxParameter.values());
		while (iterator.hasNext()) {
			GpxParameter parameter = iterator.next();
			builder.append(parameter.getColumnName());

			if (iterator.hasNext()) {
				builder.append(", ");
			} else {
				builder.append(" FROM ").append(GPX_TABLE_NAME);
			}
		}
		return builder.toString();
	}

	private void onUpgrade(SQLiteConnection db, int oldVersion, int newVersion) {
		if (oldVersion < 2) {
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_COLOR.getColumnName() + " TEXT");
		}
		if (oldVersion < 3) {
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_FILE_LAST_MODIFIED_TIME.getColumnName() + " long");
		}

		if (oldVersion < 4) {
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_SPLIT_TYPE.getColumnName() + " int");
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_SPLIT_INTERVAL.getColumnName() + " double");
		}

		if (oldVersion < 5) {
			boolean colorColumnExists = false;
			boolean fileLastModifiedTimeColumnExists = false;
			boolean splitTypeColumnExists = false;
			boolean splitIntervalColumnExists = false;
			SQLiteCursor cursor = db.rawQuery("PRAGMA table_info(" + GPX_TABLE_NAME + ")", null);
			if (cursor.moveToFirst()) {
				do {
					String columnName = cursor.getString(1);
					if (!colorColumnExists && columnName.equals(GPX_COL_COLOR.getColumnName())) {
						colorColumnExists = true;
					} else if (!fileLastModifiedTimeColumnExists && columnName.equals(GPX_COL_FILE_LAST_MODIFIED_TIME.getColumnName())) {
						fileLastModifiedTimeColumnExists = true;
					} else if (!splitTypeColumnExists && columnName.equals(GPX_COL_SPLIT_TYPE.getColumnName())) {
						splitTypeColumnExists = true;
					} else if (!splitIntervalColumnExists && columnName.equals(GPX_COL_SPLIT_INTERVAL.getColumnName())) {
						splitIntervalColumnExists = true;
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
			if (!colorColumnExists) {
				db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_COLOR.getColumnName() + " TEXT");
			}
			if (!fileLastModifiedTimeColumnExists) {
				db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_FILE_LAST_MODIFIED_TIME.getColumnName() + " long");
				for (GpxDataItem item : getItems()) {
					updateGpxParameter(item, GPX_COL_FILE_LAST_MODIFIED_TIME, item.getFile().lastModified());
				}
			}
			if (!splitTypeColumnExists) {
				db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_SPLIT_TYPE.getColumnName() + " int");
			}
			if (!splitIntervalColumnExists) {
				db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_SPLIT_INTERVAL.getColumnName() + " double");
			}
		}

		if (oldVersion < 6) {
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_API_IMPORTED.getColumnName() + " int");
			db.execSQL("UPDATE " + GPX_TABLE_NAME +
					" SET " + GPX_COL_API_IMPORTED.getColumnName() + " = ? " +
					"WHERE " + GPX_COL_API_IMPORTED.getColumnName() + " IS NULL", new Object[] {0});
		}

		if (oldVersion < 7) {
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_WPT_CATEGORY_NAMES.getColumnName() + " TEXT");
		}

		if (oldVersion < 8) {
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_SHOW_AS_MARKERS.getColumnName() + " int");
			db.execSQL("UPDATE " + GPX_TABLE_NAME +
					" SET " + GPX_COL_SHOW_AS_MARKERS.getColumnName() + " = ? " +
					"WHERE " + GPX_COL_SHOW_AS_MARKERS.getColumnName() + " IS NULL", new Object[] {0});
		}
		if (oldVersion < 10) {
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_JOIN_SEGMENTS.getColumnName() + " int");
			db.execSQL("UPDATE " + GPX_TABLE_NAME +
					" SET " + GPX_COL_JOIN_SEGMENTS.getColumnName() + " = ? " +
					"WHERE " + GPX_COL_JOIN_SEGMENTS.getColumnName() + " IS NULL", new Object[] {0});
		}
		if (oldVersion < 11) {
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_SHOW_ARROWS.getColumnName() + " int");
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_SHOW_START_FINISH.getColumnName() + " int");
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_WIDTH.getColumnName() + " TEXT");
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_GRADIENT_SPEED_COLOR.getColumnName() + " TEXT");
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_GRADIENT_ALTITUDE_COLOR.getColumnName() + " TEXT");
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_GRADIENT_SLOPE_COLOR.getColumnName() + " TEXT");
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_COLORING_TYPE.getColumnName() + " TEXT");

			db.execSQL(GPX_UPDATE_PARAMETERS_START + GPX_COL_SHOW_ARROWS.getColumnName() + " = ? " +
					"WHERE " + GPX_COL_SHOW_ARROWS.getColumnName() + " IS NULL", new Object[] {0});
			db.execSQL(GPX_UPDATE_PARAMETERS_START + GPX_COL_SHOW_START_FINISH.getColumnName() + " = ? " +
					"WHERE " + GPX_COL_SHOW_START_FINISH.getColumnName() + " IS NULL", new Object[] {1});
		}
		if (oldVersion < 12) {
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_FILE_LAST_UPLOADED_TIME.getColumnName() + " long");
		}
		if (oldVersion < 13) {
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_SMOOTHING_THRESHOLD.getColumnName() + " double");
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_MIN_FILTER_SPEED.getColumnName() + " double");
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_MAX_FILTER_SPEED.getColumnName() + " double");
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_MIN_FILTER_ALTITUDE.getColumnName() + " double");
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_MAX_FILTER_ALTITUDE.getColumnName() + " double");
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_MAX_FILTER_HDOP.getColumnName() + " double");
		}
		if (oldVersion < 14) {
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_START_LAT.getColumnName() + " double");
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_START_LON.getColumnName() + " double");
		}
		if (oldVersion < 15) {
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_NEAREST_CITY_NAME.getColumnName() + " TEXT");
		}
		if (oldVersion < 16) {
			db.execSQL("ALTER TABLE " + GPX_TABLE_NAME + " ADD " + GPX_COL_FILE_CREATION_TIME.getColumnName() + " long");
		}
		db.execSQL("CREATE INDEX IF NOT EXISTS " + GPX_INDEX_NAME_DIR + " ON " + GPX_TABLE_NAME + " (" + GPX_COL_NAME.getColumnName() + ", " + GPX_COL_DIR.getColumnName() + ");");
	}

	private boolean updateGpxParameters(@NonNull Map<GpxParameter, Object> rowsToUpdate, @NonNull Map<String, Object> rowsToSearch) {
		SQLiteConnection db = openConnection(false);
		if (db != null) {
			try {
				return updateGpxParameters(db, rowsToUpdate, rowsToSearch);
			} finally {
				db.close();
			}
		}
		return false;
	}

	public boolean updateGpxParameter(@NonNull GpxDataItem item, @NonNull GpxParameter parameter, @Nullable Object value) {
		if (parameter.isValidValue(value)) {
			Map<GpxParameter, Object> map = Collections.singletonMap(parameter, value);
			boolean success = updateGpxParameters(map, getRowsToSearch(item.getFile()));
			if (success) {
				item.getGpxData().setValue(parameter, value);
			}
			return success;
		} else {
			LOG.warn("Invalid value " + value + " for parameter " + parameter);
		}
		return false;
	}

	private boolean updateGpxParameters(@NonNull SQLiteConnection db, @NonNull Map<GpxParameter, Object> rowsToUpdate, @NonNull Map<String, Object> rowsToSearch) {
		Map<String, Object> map = new LinkedHashMap<>();
		for (Map.Entry<GpxParameter, Object> entry : rowsToUpdate.entrySet()) {
			map.put(entry.getKey().getColumnName(), entry.getValue());
		}
		Pair<String, Object[]> pair = AndroidDbUtils.createDbUpdateQuery(GPX_TABLE_NAME, map, rowsToSearch);
		db.execSQL(pair.first, pair.second);
		return true;
	}

	@NonNull
	private Map<String, Object> getRowsToSearch(@NonNull File file) {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put(GPX_COL_NAME.getColumnName(), getFileName(file));
		map.put(GPX_COL_DIR.getColumnName(), getFileDir(file));
		return map;
	}

	public boolean rename(@NonNull File currentFile, @NonNull File newFile) {
		Map<GpxParameter, Object> map = new LinkedHashMap<>();
		map.put(GPX_COL_NAME, getFileName(newFile));
		map.put(GPX_COL_DIR, getFileDir(newFile));

		return updateGpxParameters(map, getRowsToSearch(currentFile));
	}

	public boolean updateSplit(@NonNull GpxDataItem item, int splitType, double splitInterval) {
		Map<GpxParameter, Object> map = new LinkedHashMap<>();
		map.put(GPX_COL_SPLIT_TYPE, splitType);
		map.put(GPX_COL_SPLIT_INTERVAL, splitInterval);

		boolean success = updateGpxParameters(map, getRowsToSearch(item.getFile()));
		if (success) {
			GpxData data = item.getGpxData();
			data.setSplitType(splitType);
			data.setSplitInterval(splitInterval);
		}
		return success;
	}

	public boolean updateGpsFiltersConfig(@NonNull GpxDataItem item, double smoothingThreshold,
	                                      double minSpeed, double maxSpeed, double minAltitude,
	                                      double maxAltitude, double maxHdop) {
		Map<GpxParameter, Object> map = new LinkedHashMap<>();
		map.put(GPX_COL_SMOOTHING_THRESHOLD, smoothingThreshold);
		map.put(GPX_COL_MIN_FILTER_SPEED, minSpeed);
		map.put(GPX_COL_MAX_FILTER_SPEED, maxSpeed);
		map.put(GPX_COL_MIN_FILTER_ALTITUDE, minAltitude);
		map.put(GPX_COL_MAX_FILTER_ALTITUDE, maxAltitude);
		map.put(GPX_COL_MAX_FILTER_HDOP, maxHdop);

		boolean success = updateGpxParameters(map, getRowsToSearch(item.getFile()));
		if (success) {
			GpxData data = item.getGpxData();
			data.setValue(GPX_COL_SMOOTHING_THRESHOLD, smoothingThreshold);
			data.setValue(GPX_COL_MIN_FILTER_SPEED, minSpeed);
			data.setValue(GPX_COL_MAX_FILTER_SPEED, maxSpeed);
			data.setValue(GPX_COL_MIN_FILTER_ALTITUDE, minAltitude);
			data.setValue(GPX_COL_MAX_FILTER_ALTITUDE, maxAltitude);
			data.setValue(GPX_COL_MAX_FILTER_HDOP, maxHdop);
		}
		return success;
	}

	public boolean updateAppearance(@NonNull GpxDataItem item, int color, @NonNull String width,
	                                boolean showArrows, boolean showStartFinish, int splitType,
	                                double splitInterval, @Nullable String coloringType) {
		Map<GpxParameter, Object> map = new LinkedHashMap<>();

		map.put(GPX_COL_COLOR, color == 0 ? "" : Algorithms.colorToString(color));
		map.put(GPX_COL_WIDTH, width);
		map.put(GPX_COL_SHOW_ARROWS, showArrows ? 1 : 0);
		map.put(GPX_COL_SHOW_START_FINISH, showStartFinish ? 1 : 0);
		map.put(GPX_COL_SPLIT_TYPE, splitType);
		map.put(GPX_COL_SPLIT_INTERVAL, splitInterval);
		map.put(GPX_COL_COLORING_TYPE, coloringType);

		boolean success = updateGpxParameters(map, getRowsToSearch(item.getFile()));
		if (success) {
			GpxData data = item.getGpxData();
			data.setColor(color);
			data.setWidth(width);
			data.setShowArrows(showArrows);
			data.setShowStartFinish(showStartFinish);
			data.setSplitType(splitType);
			data.setSplitInterval(splitInterval);
			data.setColoringType(coloringType);
		}
		return success;
	}

	public boolean remove(@NonNull File file) {
		SQLiteConnection db = openConnection(false);
		if (db != null) {
			try {
				String fileName = getFileName(file);
				String fileDir = getFileDir(file);
				db.execSQL("DELETE FROM " + GPX_TABLE_NAME + GPX_FIND_BY_NAME_AND_DIR,
						new Object[] {fileName, fileDir});
			} finally {
				db.close();
			}
			return true;
		}
		return false;
	}

	public boolean add(@NonNull GpxDataItem item) {
		SQLiteConnection db = openConnection(false);
		if (db != null) {
			try {
				insert(item, db);
			} finally {
				db.close();
			}
			return true;
		}
		return false;
	}

	@NonNull
	private String getFileName(@NonNull File file) {
		return file.getName();
	}

	@NonNull
	private String getFileDir(@NonNull File file) {
		if (file.getParentFile() == null) {
			return "";
		}
		File gpxDir = app.getAppPath(GPX_INDEX_DIR);
		String fileDir = new File(file.getPath().replace(gpxDir.getPath() + "/", "")).getParent();
		return fileDir != null ? fileDir : "";
	}

	void insert(@NonNull GpxDataItem item, @NonNull SQLiteConnection db) {
		File file = item.getFile();
		GpxData data = item.getGpxData();
		String fileName = getFileName(file);
		String fileDir = getFileDir(file);
		GPXTrackAnalysis analysis = data.getAnalysis();
		String color = data.getColor() == 0 ? "" : Algorithms.colorToString(data.getColor());

		Map<String, Object> rowsMap = new LinkedHashMap<>();
		rowsMap.put(GPX_COL_NAME.getColumnName(), fileName);
		rowsMap.put(GPX_COL_DIR.getColumnName(), fileDir);
		rowsMap.put(GPX_COL_COLOR.getColumnName(), color);
		rowsMap.put(GPX_COL_FILE_LAST_MODIFIED_TIME.getColumnName(), file.lastModified());
		rowsMap.put(GPX_COL_FILE_LAST_UPLOADED_TIME.getColumnName(), data.getFileLastUploadedTime());
		rowsMap.put(GPX_COL_FILE_CREATION_TIME.getColumnName(), data.getFileCreationTime());
		rowsMap.put(GPX_COL_SPLIT_TYPE.getColumnName(), data.getSplitType());
		rowsMap.put(GPX_COL_SPLIT_INTERVAL.getColumnName(), data.getSplitInterval());
		rowsMap.put(GPX_COL_API_IMPORTED.getColumnName(), data.isImportedByApi() ? 1 : 0);
		rowsMap.put(GPX_COL_SHOW_AS_MARKERS.getColumnName(), data.isShowAsMarkers() ? 1 : 0);
		rowsMap.put(GPX_COL_JOIN_SEGMENTS.getColumnName(), data.isJoinSegments() ? 1 : 0);
		rowsMap.put(GPX_COL_SHOW_ARROWS.getColumnName(), data.isShowArrows() ? 1 : 0);
		rowsMap.put(GPX_COL_SHOW_START_FINISH.getColumnName(), data.isShowStartFinish() ? 1 : 0);
		rowsMap.put(GPX_COL_WIDTH.getColumnName(), data.getWidth());
		rowsMap.put(GPX_COL_COLORING_TYPE.getColumnName(), data.getColoringType());
		rowsMap.put(GPX_COL_SMOOTHING_THRESHOLD.getColumnName(), data.getSmoothingThreshold());
		rowsMap.put(GPX_COL_MIN_FILTER_SPEED.getColumnName(), data.getMinFilterSpeed());
		rowsMap.put(GPX_COL_MAX_FILTER_SPEED.getColumnName(), data.getMaxFilterSpeed());
		rowsMap.put(GPX_COL_MIN_FILTER_ALTITUDE.getColumnName(), data.getMinFilterAltitude());
		rowsMap.put(GPX_COL_MAX_FILTER_ALTITUDE.getColumnName(), data.getMaxFilterAltitude());
		rowsMap.put(GPX_COL_MAX_FILTER_HDOP.getColumnName(), data.getMaxFilterHdop());
		rowsMap.put(GPX_COL_NEAREST_CITY_NAME.getColumnName(), data.getNearestCityName());

		if (analysis != null) {
			rowsMap.put(GPX_COL_TOTAL_DISTANCE.getColumnName(), analysis.totalDistance);
			rowsMap.put(GPX_COL_TOTAL_TRACKS.getColumnName(), analysis.totalTracks);
			rowsMap.put(GPX_COL_START_TIME.getColumnName(), analysis.startTime);
			rowsMap.put(GPX_COL_END_TIME.getColumnName(), analysis.endTime);
			rowsMap.put(GPX_COL_TIME_SPAN.getColumnName(), analysis.timeSpan);
			rowsMap.put(GPX_COL_TIME_MOVING.getColumnName(), analysis.timeMoving);
			rowsMap.put(GPX_COL_TOTAL_DISTANCE_MOVING.getColumnName(), analysis.totalDistanceMoving);
			rowsMap.put(GPX_COL_DIFF_ELEVATION_UP.getColumnName(), analysis.diffElevationUp);
			rowsMap.put(GPX_COL_DIFF_ELEVATION_DOWN.getColumnName(), analysis.diffElevationDown);
			rowsMap.put(GPX_COL_AVG_ELEVATION.getColumnName(), analysis.avgElevation);
			rowsMap.put(GPX_COL_MIN_ELEVATION.getColumnName(), analysis.minElevation);
			rowsMap.put(GPX_COL_MAX_ELEVATION.getColumnName(), analysis.maxElevation);
			rowsMap.put(GPX_COL_MAX_SPEED.getColumnName(), analysis.maxSpeed);
			rowsMap.put(GPX_COL_AVG_SPEED.getColumnName(), analysis.avgSpeed);
			rowsMap.put(GPX_COL_POINTS.getColumnName(), analysis.points);
			rowsMap.put(GPX_COL_WPT_POINTS.getColumnName(), analysis.wptPoints);
			rowsMap.put(GPX_COL_WPT_CATEGORY_NAMES.getColumnName(), Algorithms.encodeCollection(analysis.wptCategoryNames));
			rowsMap.put(GPX_COL_START_LAT.getColumnName(), analysis.latLonStart != null ? analysis.latLonStart.getLatitude() : null);
			rowsMap.put(GPX_COL_START_LON.getColumnName(), analysis.latLonStart != null ? analysis.latLonStart.getLongitude() : null);
		}
		db.execSQL(AndroidDbUtils.createDbInsertQuery(GPX_TABLE_NAME, rowsMap.keySet()), rowsMap.values().toArray());
	}

	public boolean updateAnalysis(@NonNull GpxDataItem item, @Nullable GPXTrackAnalysis analysis) {
		SQLiteConnection db = openConnection(false);
		if (db != null) {
			try {
				return updateAnalysis(db, item, analysis);
			} finally {
				db.close();
			}
		}
		return false;
	}

	public boolean updateAnalysis(@NonNull SQLiteConnection db, @NonNull GpxDataItem item, @Nullable GPXTrackAnalysis analysis) {
		boolean hasAnalysis = analysis != null;
		long fileLastModifiedTime = hasAnalysis ? item.getFile().lastModified() : 0;

		Map<GpxParameter, Object> map = new LinkedHashMap<>();
		map.put(GPX_COL_TOTAL_DISTANCE, hasAnalysis ? analysis.totalDistance : null);
		map.put(GPX_COL_TOTAL_TRACKS, hasAnalysis ? analysis.totalTracks : null);
		map.put(GPX_COL_START_TIME, hasAnalysis ? analysis.startTime : null);
		map.put(GPX_COL_END_TIME, hasAnalysis ? analysis.endTime : null);
		map.put(GPX_COL_TIME_SPAN, hasAnalysis ? analysis.timeSpan : null);
		map.put(GPX_COL_TIME_MOVING, hasAnalysis ? analysis.timeMoving : null);
		map.put(GPX_COL_TOTAL_DISTANCE_MOVING, hasAnalysis ? analysis.totalDistanceMoving : null);
		map.put(GPX_COL_DIFF_ELEVATION_UP, hasAnalysis ? analysis.diffElevationUp : null);
		map.put(GPX_COL_DIFF_ELEVATION_DOWN, hasAnalysis ? analysis.diffElevationDown : null);
		map.put(GPX_COL_AVG_ELEVATION, hasAnalysis ? analysis.avgElevation : null);
		map.put(GPX_COL_MIN_ELEVATION, hasAnalysis ? analysis.minElevation : null);
		map.put(GPX_COL_MAX_ELEVATION, hasAnalysis ? analysis.maxElevation : null);
		map.put(GPX_COL_MAX_SPEED, hasAnalysis ? analysis.maxSpeed : null);
		map.put(GPX_COL_AVG_SPEED, hasAnalysis ? analysis.avgSpeed : null);
		map.put(GPX_COL_POINTS, hasAnalysis ? analysis.points : null);
		map.put(GPX_COL_WPT_POINTS, hasAnalysis ? analysis.wptPoints : null);
		map.put(GPX_COL_FILE_LAST_MODIFIED_TIME, fileLastModifiedTime);
		map.put(GPX_COL_WPT_CATEGORY_NAMES, hasAnalysis ? Algorithms.encodeCollection(analysis.wptCategoryNames) : null);
		map.put(GPX_COL_START_LAT, hasAnalysis && analysis.latLonStart != null ? analysis.latLonStart.getLatitude() : null);
		map.put(GPX_COL_START_LON, hasAnalysis && analysis.latLonStart != null ? analysis.latLonStart.getLongitude() : null);

		boolean success = updateGpxParameters(db, map, getRowsToSearch(item.getFile()));
		if (success) {
			GpxData data = item.getGpxData();
			data.setAnalysis(analysis);
			data.setFileLastModifiedTime(fileLastModifiedTime);
		}
		return success;
	}

	@NonNull
	private GpxDataItem readItem(SQLiteCursor query) {
		String fileName = query.getString(0);
		String fileDir = query.getString(1);
		float totalDistance = (float) query.getDouble(2);
		int totalTracks = query.getInt(3);
		long startTime = query.getLong(4);
		long endTime = query.getLong(5);
		long timeSpan = query.getLong(6);
		long timeMoving = query.getLong(7);
		float totalDistanceMoving = (float) query.getDouble(8);
		double diffElevationUp = query.getDouble(9);
		double diffElevationDown = query.getDouble(10);
		double avgElevation = query.getDouble(11);
		double minElevation = query.getDouble(12);
		double maxElevation = query.getDouble(13);
		float maxSpeed = (float) query.getDouble(14);
		float avgSpeed = (float) query.getDouble(15);
		int points = query.getInt(16);
		int wptPoints = query.getInt(17);
		String color = query.getString(18);
		long fileLastModifiedTime = query.getLong(19);
		long fileLastUploadedTime = query.getLong(20);
		long fileCreateTime = query.isNull(21) ? -1 : query.getLong(21);
		int splitType = query.getInt(22);
		double splitInterval = query.getDouble(23);
		boolean apiImported = query.getInt(24) == 1;
		String wptCategoryNames = query.getString(25);
		boolean showAsMarkers = query.getInt(26) == 1;
		boolean joinSegments = query.getInt(27) == 1;
		boolean showArrows = query.getInt(28) == 1;
		boolean showStartFinish = query.getInt(29) == 1;
		String width = query.getString(30);
		String coloringTypeName = query.getString(34);
		double smoothingThreshold = query.getDouble(35);
		double minFilterSpeed = query.getDouble(36);
		double maxFilterSpeed = query.getDouble(37);
		double minFilterAltitude = query.getDouble(38);
		double maxFilterAltitude = query.getDouble(39);
		double maxFilterHdop = query.getDouble(40);

		LatLon latLonStart = null;
		if (!query.isNull(41) && !query.isNull(42)) {
			double lat = query.getDouble(41);
			double lon = query.getDouble(42);
			latLonStart = new LatLon(lat, lon);
		}
		String nearestCityName = query.getString(43);

		GPXTrackAnalysis analysis = new GPXTrackAnalysis();
		analysis.totalDistance = totalDistance;
		analysis.totalTracks = totalTracks;
		analysis.startTime = startTime;
		analysis.endTime = endTime;
		analysis.timeSpan = timeSpan;
		analysis.timeMoving = timeMoving;
		analysis.totalDistanceMoving = totalDistanceMoving;
		analysis.diffElevationUp = diffElevationUp;
		analysis.diffElevationDown = diffElevationDown;
		analysis.avgElevation = avgElevation;
		analysis.minElevation = minElevation;
		analysis.maxElevation = maxElevation;
		analysis.minSpeed = maxSpeed;
		analysis.maxSpeed = maxSpeed;
		analysis.avgSpeed = avgSpeed;
		analysis.points = points;
		analysis.wptPoints = wptPoints;
		analysis.latLonStart = latLonStart;
		analysis.wptCategoryNames = wptCategoryNames != null ? Algorithms.decodeStringSet(wptCategoryNames) : null;

		File dir;
		if (Algorithms.isEmpty(fileDir)) {
			dir = app.getAppPath(GPX_INDEX_DIR);
		} else {
			dir = new File(app.getAppPath(GPX_INDEX_DIR), fileDir);
		}
		GpxDataItem item = new GpxDataItem(new File(dir, fileName));
		GpxData data = item.getGpxData();

		data.setAnalysis(analysis);
		data.setContainingFolder(fileDir);
		data.setColor(GPXUtilities.parseColor(color, 0));
		data.setFileLastModifiedTime(fileLastModifiedTime);
		data.setFileLastUploadedTime(fileLastUploadedTime);
		data.setFileCreationTime(fileCreateTime);
		data.setSplitType(splitType);
		data.setSplitInterval(splitInterval);
		data.setImportedByApi(apiImported);
		data.setShowAsMarkers(showAsMarkers);
		data.setJoinSegments(joinSegments);
		data.setShowArrows(showArrows);
		data.setShowStartFinish(showStartFinish);
		data.setWidth(width);
		data.setNearestCityName(nearestCityName);

		if (ColoringType.getNullableTrackColoringTypeByName(coloringTypeName) != null) {
			data.setColoringType(coloringTypeName);
		} else if (GradientScaleType.getGradientTypeByName(coloringTypeName) != null) {
			GradientScaleType scaleType = GradientScaleType.getGradientTypeByName(coloringTypeName);
			ColoringType coloringType = ColoringType.fromGradientScaleType(scaleType);
			data.setColoringType(coloringType == null ? null : coloringType.getName(null));
		}

		data.setValue(GPX_COL_SMOOTHING_THRESHOLD, smoothingThreshold);
		data.setValue (GPX_COL_MIN_FILTER_SPEED, minFilterSpeed);
		data.setValue (GPX_COL_MAX_FILTER_SPEED, maxFilterSpeed);
		data.setValue (GPX_COL_MIN_FILTER_ALTITUDE, minFilterAltitude);
		data.setValue (GPX_COL_MAX_FILTER_ALTITUDE, maxFilterAltitude);
		data.setValue (GPX_COL_MAX_FILTER_HDOP, maxFilterHdop);

		return item;
	}

	public long getTracksMinCreateDate() {
		long minDate = -1;
		SQLiteConnection db = openConnection(false);
		if (db != null) {
			try {
				SQLiteCursor query = db.rawQuery(GPX_MIN_CREATE_DATE, null);
				if (query != null) {
					try {
						if (query.moveToFirst()) {
							minDate = query.getLong(0);
						}
					} finally {
						query.close();
					}
				}
			} finally {
				db.close();
			}
		}
		return minDate;
	}

	public double getTracksMaxDuration() {
		double maxLength = 0.0;
		SQLiteConnection db = openConnection(false);
		if (db != null) {
			try {
				SQLiteCursor query = db.rawQuery(GPX_MAX_TRACK_DURATION, null);
				if (query != null) {
					try {
						if (query.moveToFirst()) {
							maxLength = query.getDouble(0);
						}
					} finally {
						query.close();
					}
				}
			} finally {
				db.close();
			}
		}
		return maxLength;
	}

	public List<Pair<String, Integer>> getTrackFolders() {
		return getStringIntItemsCollection(GPX_TRACK_FOLDERS_COLLECTION);
	}

	public List<Pair<String, Integer>> getNearestCityCollection() {
		return getStringIntItemsCollection(GPX_TRACK_NEAREST_CITIES_COLLECTION);
	}

	public List<Pair<String, Integer>> getTrackColorsCollection() {
		return getStringIntItemsCollection(GPX_TRACK_COLORS_COLLECTION);
	}

	public List<Pair<String, Integer>> getTrackWidthCollection() {
		return getStringIntItemsCollection(GPX_TRACK_WIDTH_COLLECTION);
	}

	public List<Pair<String, Integer>> getStringIntItemsCollection(String dataQuery) {
		ArrayList<Pair<String, Integer>> folderCollection = new ArrayList<>();
		SQLiteConnection db = openConnection(false);
		if (db != null) {
			try {
				SQLiteCursor query = db.rawQuery(dataQuery, null);
				if (query != null) {
					try {
						if (query.moveToFirst()) {
							do {
								folderCollection.add(new Pair<>(query.getString(0), query.getInt(1)));
							} while (query.moveToNext());
						}
					} finally {
						query.close();
					}
				}
			} finally {
				db.close();
			}
		}
		return folderCollection;
	}

	@NonNull
	public List<GpxDataItem> getItems() {
		Set<GpxDataItem> items = new HashSet<>();
		SQLiteConnection db = openConnection(false);
		if (db != null) {
			try {
				SQLiteCursor query = db.rawQuery(getSelectQuery(), null);
				if (query != null) {
					try {
						if (query.moveToFirst()) {
							do {
								items.add(readItem(query));
							} while (query.moveToNext());
						}
					} finally {
						query.close();
					}
				}
			} finally {
				db.close();
			}
		}
		return new ArrayList<>(items);
	}

	@Nullable
	public GpxDataItem getItem(File file) {
		GpxDataItem result = null;
		SQLiteConnection db = openConnection(false);
		if (db != null) {
			try {
				result = getItem(file, db);
			} finally {
				db.close();
			}
		}
		return result;
	}

	@Nullable
	public GpxDataItem getItem(File file, SQLiteConnection db) {
		GpxDataItem result = null;
		String fileName = getFileName(file);
		String fileDir = getFileDir(file);
		SQLiteCursor query = db.rawQuery(getSelectQuery() + GPX_FIND_BY_NAME_AND_DIR, new String[] {fileName, fileDir});
		if (query != null) {
			try {
				if (query.moveToFirst()) {
					result = readItem(query);
				}
			} finally {
				query.close();
			}
		}
		return result;
	}

	@NonNull
	public List<GpxDataItem> getSplitItems() {
		List<GpxDataItem> items = new ArrayList<>();
		SQLiteConnection db = openConnection(false);
		if (db != null) {
			try {
				SQLiteCursor query = db.rawQuery(getSelectQuery() + " WHERE " + GPX_COL_SPLIT_TYPE.getColumnName() + " != ?", new String[] {String.valueOf(0)});
				if (query != null) {
					try {
						if (query.moveToFirst()) {
							do {
								items.add(readItem(query));
							} while (query.moveToNext());
						}
					} finally {
						query.close();
					}
				}
			} finally {
				db.close();
			}
		}
		return items;
	}
}
