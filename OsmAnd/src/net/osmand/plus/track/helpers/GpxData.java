package net.osmand.plus.track.helpers;

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
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_JOIN_SEGMENTS;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_MAX_ELEVATION;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_MAX_FILTER_ALTITUDE;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_MAX_FILTER_HDOP;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_MAX_FILTER_SPEED;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_MAX_SPEED;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_MIN_ELEVATION;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_MIN_FILTER_ALTITUDE;
import static net.osmand.plus.track.helpers.GpxParameter.GPX_COL_MIN_FILTER_SPEED;
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

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.osmand.gpx.GPXFile;
import net.osmand.gpx.GPXTrackAnalysis;
import net.osmand.plus.routing.ColoringType;
import net.osmand.plus.track.GpxSplitType;
import net.osmand.plus.track.GradientScaleType;
import net.osmand.plus.track.helpers.GpsFilterHelper.AltitudeFilter;
import net.osmand.plus.track.helpers.GpsFilterHelper.HdopFilter;
import net.osmand.plus.track.helpers.GpsFilterHelper.SmoothingFilter;
import net.osmand.plus.track.helpers.GpsFilterHelper.SpeedFilter;
import net.osmand.util.Algorithms;

import java.util.HashMap;
import java.util.Map;

public class GpxData {

	@Nullable
	private GPXTrackAnalysis analysis;

	private final Map<GpxParameter, Object> parameters = new HashMap<>();

	public GpxData() {
		init();
	}

	private void init() {
		for (GpxParameter parameter : GpxParameter.values()) {
			parameters.put(parameter, parameter.getDefaultValue());
		}
	}

	@Nullable
	public Object getValue(@NonNull GpxParameter parameter) {
		return parameters.get(parameter);
	}

	public boolean setValue(@NonNull GpxParameter parameter, @Nullable Object value) {
		if (parameter.isValidValue(value)) {
			parameters.put(parameter, value);
			return true;
		}
		return false;
	}

	@Nullable
	public GPXTrackAnalysis getAnalysis() {
		return analysis;
	}

	public void setAnalysis(@Nullable GPXTrackAnalysis analysis) {
		this.analysis = analysis;
		boolean hasAnalysis = analysis != null;
		setValue(GPX_COL_TOTAL_DISTANCE, hasAnalysis ? analysis.totalDistance : null);
		setValue(GPX_COL_TOTAL_TRACKS, hasAnalysis ? analysis.totalTracks : null);
		setValue(GPX_COL_START_TIME, hasAnalysis ? analysis.startTime : null);
		setValue(GPX_COL_END_TIME, hasAnalysis ? analysis.endTime : null);
		setValue(GPX_COL_TIME_SPAN, hasAnalysis ? analysis.timeSpan : null);
		setValue(GPX_COL_TIME_MOVING, hasAnalysis ? analysis.timeMoving : null);
		setValue(GPX_COL_TOTAL_DISTANCE_MOVING, hasAnalysis ? analysis.totalDistanceMoving : null);
		setValue(GPX_COL_DIFF_ELEVATION_UP, hasAnalysis ? analysis.diffElevationUp : null);
		setValue(GPX_COL_DIFF_ELEVATION_DOWN, hasAnalysis ? analysis.diffElevationDown : null);
		setValue(GPX_COL_AVG_ELEVATION, hasAnalysis ? analysis.avgElevation : null);
		setValue(GPX_COL_MIN_ELEVATION, hasAnalysis ? analysis.minElevation : null);
		setValue(GPX_COL_MAX_ELEVATION, hasAnalysis ? analysis.maxElevation : null);
		setValue(GPX_COL_MAX_SPEED, hasAnalysis ? analysis.maxSpeed : null);
		setValue(GPX_COL_AVG_SPEED, hasAnalysis ? analysis.avgSpeed : null);
		setValue(GPX_COL_POINTS, hasAnalysis ? analysis.points : null);
		setValue(GPX_COL_WPT_POINTS, hasAnalysis ? analysis.wptPoints : null);
		setValue(GPX_COL_WPT_CATEGORY_NAMES, hasAnalysis ? Algorithms.encodeCollection(analysis.wptCategoryNames) : null);
		setValue(GPX_COL_START_LAT, hasAnalysis && analysis.latLonStart != null ? analysis.latLonStart.getLatitude() : null);
		setValue(GPX_COL_START_LON, hasAnalysis && analysis.latLonStart != null ? analysis.latLonStart.getLongitude() : null);
	}

	@ColorInt
	public int getColor() {
		Integer value = (Integer) parameters.get(GPX_COL_COLOR);
		return value != null ? value : (int) GPX_COL_COLOR.getDefaultValue();
	}

	public void setColor(@ColorInt int color) {
		setValue(GPX_COL_COLOR, color);
	}

	@Nullable
	public String getWidth() {
		String value = (String) parameters.get(GPX_COL_WIDTH);
		return value != null ? value : (String) GPX_COL_WIDTH.getDefaultValue();
	}

	public void setWidth(@Nullable String width) {
		setValue(GPX_COL_WIDTH, width);
	}

	@Nullable
	public String getColoringType() {
		String value = (String) parameters.get(GPX_COL_COLORING_TYPE);
		return value != null ? value : (String) GPX_COL_COLORING_TYPE.getDefaultValue();
	}

	public void setColoringType(@Nullable String coloringType) {
		setValue(GPX_COL_COLORING_TYPE, coloringType);
	}

	@Nullable
	public String getNearestCityName() {
		String value = (String) parameters.get(GPX_COL_NEAREST_CITY_NAME);
		return value != null ? value : (String) GPX_COL_NEAREST_CITY_NAME.getDefaultValue();
	}

	public void setNearestCityName(@Nullable String nearestCityName) {
		setValue(GPX_COL_NEAREST_CITY_NAME, nearestCityName);
	}

	@Nullable
	public String getContainingFolder() {
		String value = (String) parameters.get(GPX_COL_DIR);
		return value != null ? value : (String) GPX_COL_DIR.getDefaultValue();
	}

	public void setContainingFolder(@Nullable String containingFolder) {
		setValue(GPX_COL_DIR, containingFolder);
	}

	public int getSplitType() {
		Integer value = (Integer) parameters.get(GPX_COL_SPLIT_TYPE);
		return value != null ? value : (int) GPX_COL_SPLIT_TYPE.getDefaultValue();
	}

	public void setSplitType(int splitType) {
		setValue(GPX_COL_SPLIT_TYPE, splitType);
	}

	public double getSplitInterval() {
		Double value = (Double) parameters.get(GPX_COL_SPLIT_INTERVAL);
		return value != null ? value : (double) GPX_COL_SPLIT_INTERVAL.getDefaultValue();
	}

	public void setSplitInterval(double splitInterval) {
		setValue(GPX_COL_SPLIT_INTERVAL, splitInterval);
	}

	public long getFileCreationTime() {
		Long value = (Long) parameters.get(GPX_COL_FILE_CREATION_TIME);
		return value != null ? value : (long) GPX_COL_FILE_CREATION_TIME.getDefaultValue();
	}

	public void setFileCreationTime(long fileCreationTime) {
		setValue(GPX_COL_FILE_CREATION_TIME, fileCreationTime);
	}

	public long getFileLastModifiedTime() {
		Long value = (Long) parameters.get(GPX_COL_FILE_LAST_MODIFIED_TIME);
		return value != null ? value : (long) GPX_COL_FILE_LAST_MODIFIED_TIME.getDefaultValue();
	}

	public void setFileLastModifiedTime(long fileLastModifiedTime) {
		setValue(GPX_COL_FILE_LAST_MODIFIED_TIME, fileLastModifiedTime);
	}

	public long getFileLastUploadedTime() {
		Long value = (Long) parameters.get(GPX_COL_FILE_LAST_UPLOADED_TIME);
		return value != null ? value : (long) GPX_COL_FILE_LAST_UPLOADED_TIME.getDefaultValue();
	}

	public void setFileLastUploadedTime(long fileLastUploadedTime) {
		setValue(GPX_COL_FILE_LAST_UPLOADED_TIME, fileLastUploadedTime);
	}

	public boolean isShowArrows() {
		Boolean value = (Boolean) parameters.get(GPX_COL_SHOW_ARROWS);
		return value != null ? value : (boolean) GPX_COL_SHOW_ARROWS.getDefaultValue();
	}

	public void setShowArrows(boolean showArrows) {
		setValue(GPX_COL_SHOW_ARROWS, showArrows);
	}

	public boolean isShowStartFinish() {
		Boolean value = (Boolean) parameters.get(GPX_COL_SHOW_START_FINISH);
		return value != null ? value : (boolean) GPX_COL_SHOW_START_FINISH.getDefaultValue();
	}

	public void setShowStartFinish(boolean showStartFinish) {
		setValue(GPX_COL_SHOW_START_FINISH, showStartFinish);
	}

	public boolean isJoinSegments() {
		Boolean value = (Boolean) parameters.get(GPX_COL_JOIN_SEGMENTS);
		return value != null ? value : (boolean) GPX_COL_JOIN_SEGMENTS.getDefaultValue();
	}

	public void setJoinSegments(boolean joinSegments) {
		setValue(GPX_COL_JOIN_SEGMENTS, joinSegments);
	}

	public boolean isShowAsMarkers() {
		Boolean value = (Boolean) parameters.get(GPX_COL_SHOW_AS_MARKERS);
		return value != null ? value : (boolean) GPX_COL_SHOW_AS_MARKERS.getDefaultValue();
	}

	public void setShowAsMarkers(boolean showAsMarkers) {
		setValue(GPX_COL_SHOW_AS_MARKERS, showAsMarkers);
	}

	public boolean isImportedByApi() {
		Boolean value = (Boolean) parameters.get(GPX_COL_API_IMPORTED);
		return value != null ? value : (boolean) GPX_COL_API_IMPORTED.getDefaultValue();
	}

	public void setImportedByApi(boolean importedByApi) {
		setValue(GPX_COL_API_IMPORTED, importedByApi);
	}

	public double getMaxFilterHdop() {
		Double value = (Double) parameters.get(GPX_COL_MAX_FILTER_HDOP);
		return value != null ? value : (double) GPX_COL_MAX_FILTER_HDOP.getDefaultValue();
	}

	public double getMinFilterSpeed() {
		Double value = (Double) parameters.get(GPX_COL_MIN_FILTER_SPEED);
		return value != null ? value : (double) GPX_COL_MIN_FILTER_SPEED.getDefaultValue();
	}

	public double getMaxFilterSpeed() {
		Double value = (Double) parameters.get(GPX_COL_MAX_FILTER_SPEED);
		return value != null ? value : (double) GPX_COL_MAX_FILTER_SPEED.getDefaultValue();
	}

	public double getMinFilterAltitude() {
		Double value = (Double) parameters.get(GPX_COL_MIN_FILTER_ALTITUDE);
		return value != null ? value : (double) GPX_COL_MIN_FILTER_ALTITUDE.getDefaultValue();
	}

	public double getMaxFilterAltitude() {
		Double value = (Double) parameters.get(GPX_COL_MAX_FILTER_ALTITUDE);
		return value != null ? value : (double) GPX_COL_MAX_FILTER_ALTITUDE.getDefaultValue();
	}

	public double getSmoothingThreshold() {
		Double value = (Double) parameters.get(GPX_COL_SMOOTHING_THRESHOLD);
		return value != null ? value : (double) GPX_COL_SMOOTHING_THRESHOLD.getDefaultValue();
	}

	public void readGpxParams(@NonNull GPXFile gpxFile) {
		setValue(GPX_COL_COLOR, gpxFile.getColor(0));
		setValue(GPX_COL_WIDTH, gpxFile.getWidth(null));
		setValue(GPX_COL_SHOW_ARROWS, gpxFile.isShowArrows());
		setValue(GPX_COL_SHOW_START_FINISH, gpxFile.isShowStartFinish());

		if (!Algorithms.isEmpty(gpxFile.getSplitType()) && gpxFile.getSplitInterval() > 0) {
			GpxSplitType gpxSplitType = GpxSplitType.getSplitTypeByName(gpxFile.getSplitType());
			setValue(GPX_COL_SPLIT_TYPE, gpxSplitType.getType());
			setValue(GPX_COL_SPLIT_INTERVAL, gpxFile.getSplitInterval());
		}

		if (!Algorithms.isEmpty(gpxFile.getColoringType())) {
			setValue(GPX_COL_COLORING_TYPE, gpxFile.getColoringType());
		} else if (!Algorithms.isEmpty(gpxFile.getGradientScaleType())) {
			GradientScaleType scaleType = GradientScaleType.getGradientTypeByName(gpxFile.getGradientScaleType());
			ColoringType type = ColoringType.fromGradientScaleType(scaleType);
			setValue(GPX_COL_COLORING_TYPE, type == null ? null : type.getName(null));
		}

		Map<String, String> extensions = gpxFile.getExtensionsToRead();
		setValue(GPX_COL_SMOOTHING_THRESHOLD, SmoothingFilter.getSmoothingThreshold(extensions));
		setValue(GPX_COL_MIN_FILTER_SPEED, SpeedFilter.getMinFilterSpeed(extensions));
		setValue(GPX_COL_MAX_FILTER_SPEED, SpeedFilter.getMaxFilterSpeed(extensions));
		setValue(GPX_COL_MIN_FILTER_ALTITUDE, AltitudeFilter.getMinFilterAltitude(extensions));
		setValue(GPX_COL_MAX_FILTER_ALTITUDE, AltitudeFilter.getMaxFilterAltitude(extensions));
		setValue(GPX_COL_MAX_FILTER_HDOP, HdopFilter.getMaxFilterHdop(extensions));
		setValue(GPX_COL_FILE_CREATION_TIME, gpxFile.metadata.time);
	}

	protected void copyData(@NonNull GpxData data) {
		this.parameters.clear();
		this.parameters.putAll(data.parameters);
		setAnalysis(data.analysis);
	}
}