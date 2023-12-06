package net.osmand.plus.track.helpers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public enum GpxParameter {

	GPX_COL_NAME("fileName", "TEXT", String.class, null),
	GPX_COL_DIR("fileDir", "TEXT", String.class, null),
	GPX_COL_TOTAL_DISTANCE("totalDistance", "double", Double.class, 0),
	GPX_COL_TOTAL_TRACKS("totalTracks", "int", Integer.class, 0),
	GPX_COL_START_TIME("startTime", "long", Long.class, Long.MAX_VALUE),
	GPX_COL_END_TIME("endTime", "long", Long.class, Long.MIN_VALUE),
	GPX_COL_TIME_SPAN("timeSpan", "long", Long.class, 0L),
	GPX_COL_TIME_MOVING("timeMoving", "long", Long.class, 0L),
	GPX_COL_TOTAL_DISTANCE_MOVING("totalDistanceMoving", "double", Double.class, 0),
	GPX_COL_DIFF_ELEVATION_UP("diffElevationUp", "double", Double.class, 0),
	GPX_COL_DIFF_ELEVATION_DOWN("diffElevationDown", "double", Double.class, 0),
	GPX_COL_AVG_ELEVATION("avgElevation", "double", Double.class, 0),
	GPX_COL_MIN_ELEVATION("minElevation", "double", Double.class, 99999),
	GPX_COL_MAX_ELEVATION("maxElevation", "double", Double.class, -100),
	GPX_COL_MAX_SPEED("maxSpeed", "double", Double.class, 0),
	GPX_COL_AVG_SPEED("avgSpeed", "double", Double.class, 0),
	GPX_COL_POINTS("points", "int", Integer.class, 0),
	GPX_COL_WPT_POINTS("wptPoints", "int", Integer.class, 0),
	GPX_COL_COLOR("color", "TEXT", Integer.class, 0),
	GPX_COL_FILE_LAST_MODIFIED_TIME("fileLastModifiedTime", "long", Long.class, 0L),
	GPX_COL_FILE_LAST_UPLOADED_TIME("fileLastUploadedTime", "long", Long.class, 0L),
	GPX_COL_FILE_CREATION_TIME("fileCreationTime", "long", Long.class, -1L),
	GPX_COL_SPLIT_TYPE("splitType", "int", Integer.class, 0),
	GPX_COL_SPLIT_INTERVAL("splitInterval", "double", Double.class, 0.0),
	GPX_COL_API_IMPORTED("apiImported", "int", Boolean.class, false),  // 1 = true, 0 = false
	GPX_COL_WPT_CATEGORY_NAMES("wptCategoryNames", "TEXT", String.class, null),
	GPX_COL_SHOW_AS_MARKERS("showAsMarkers", "int", Boolean.class, false),  // 1 = true, 0 = false
	GPX_COL_JOIN_SEGMENTS("joinSegments", "int", Boolean.class, false),  // 1 = true, 0 = false
	GPX_COL_SHOW_ARROWS("showArrows", "int", Boolean.class, false),  // 1 = true, 0 = false
	GPX_COL_SHOW_START_FINISH("showStartFinish", "int", Boolean.class, true),  // 1 = true, 0 = false
	GPX_COL_WIDTH("width", "TEXT", String.class, null),
	GPX_COL_GRADIENT_SPEED_COLOR("gradientSpeedColor", "TEXT", String.class, null),
	GPX_COL_GRADIENT_ALTITUDE_COLOR("gradientAltitudeColor", "TEXT", String.class, null),
	GPX_COL_GRADIENT_SLOPE_COLOR("gradientSlopeColor", "TEXT", String.class, null),
	GPX_COL_COLORING_TYPE("gradientScaleType", "TEXT", String.class, null),
	GPX_COL_SMOOTHING_THRESHOLD("smoothingThreshold", "double", Double.class, Double.NaN),
	GPX_COL_MIN_FILTER_SPEED("minFilterSpeed", "double", Double.class, Double.NaN),
	GPX_COL_MAX_FILTER_SPEED("maxFilterSpeed", "double", Double.class, Double.NaN),
	GPX_COL_MIN_FILTER_ALTITUDE("minFilterAltitude", "double", Double.class, Double.NaN),
	GPX_COL_MAX_FILTER_ALTITUDE("maxFilterAltitude", "double", Double.class, Double.NaN),
	GPX_COL_MAX_FILTER_HDOP("maxFilterHdop", "double", Double.class, Double.NaN),
	GPX_COL_START_LAT("startLat", "double", Double.class, null),
	GPX_COL_START_LON("startLon", "double", Double.class, null),
	GPX_COL_NEAREST_CITY_NAME("nearestCityName", "TEXT", String.class, null);


	private final String columnName;
	private final String columnType;
	private final Class<?> typeClass;
	private final Object defaultValue;

	GpxParameter(@NonNull String columnName, @NonNull String columnType, @NonNull Class<?> typeClass, @Nullable Object defaultValue) {
		this.columnName = columnName;
		this.columnType = columnType;
		this.typeClass = typeClass;
		this.defaultValue = defaultValue;
	}

	@NonNull
	public String getColumnName() {
		return columnName;
	}

	@NonNull
	public String getColumnType() {
		return columnType;
	}

	@NonNull
	public Class<?> getTypeClass() {
		return typeClass;
	}

	@Nullable
	public Object getDefaultValue() {
		return defaultValue;
	}

	public boolean isNullSupported() {
		return defaultValue == null;
	}

	public boolean isValidValue(@Nullable Object value) {
		return value == null && isNullSupported() || value != null && getTypeClass() == value.getClass();
	}
}
