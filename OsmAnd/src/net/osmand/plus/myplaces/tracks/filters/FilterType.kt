package net.osmand.plus.myplaces.tracks.filters

import androidx.annotation.StringRes
import net.osmand.plus.R
import net.osmand.plus.track.helpers.GpxParameter
import java.util.Collections

import net.osmand.plus.myplaces.tracks.filters.NonDbTrackParam.VISIBLE_ON_MAP
import net.osmand.plus.myplaces.tracks.filters.NonDbTrackParam.WITH_WAYPOINTS

enum class FilterType(
	@StringRes val nameResId: Int,
	val filterDisplayType: FilterDisplayType,
	val propertyList: List<GpxParameter>,
	val measureUnitType: MeasureUnitType,
	val defaultParams: List<Any>?,
	val updateOnOtherFiltersChangeNeeded: Boolean,
	val additionalData: Any? = null) {
	NAME(
		R.string.shared_string_name,
		FilterDisplayType.TEXT,
		Collections.singletonList(GpxParameter.FILE_NAME),
		MeasureUnitType.NONE,
		null,
		false),
	DURATION(
		R.string.duration,
		FilterDisplayType.RANGE,
		Collections.singletonList(GpxParameter.TIME_SPAN),
		MeasureUnitType.TIME_DURATION,
		listOf(0L, TrackFiltersConstants.DEFAULT_MAX_VALUE.toLong()),
		false),
	TIME_IN_MOTION(
		R.string.moving_time,
		FilterDisplayType.RANGE,
		Collections.singletonList(GpxParameter.TIME_MOVING),
		MeasureUnitType.TIME_DURATION,
		listOf(0L, TrackFiltersConstants.DEFAULT_MAX_VALUE.toLong()),
		false),
	LENGTH(
		R.string.routing_attr_length_name,
		FilterDisplayType.RANGE,
		Collections.singletonList(GpxParameter.TOTAL_DISTANCE),
		MeasureUnitType.DISTANCE,
		listOf(0.0, TrackFiltersConstants.LENGTH_MAX_VALUE.toDouble()),
		false),
	AVERAGE_SPEED(
		R.string.average_speed,
		FilterDisplayType.RANGE,
		Collections.singletonList(GpxParameter.AVG_SPEED),
		MeasureUnitType.SPEED,
		listOf(0.0, TrackFiltersConstants.SPEED_MAX_VALUE.toDouble()),
		false),
	MAX_SPEED(
		R.string.max_speed,
		FilterDisplayType.RANGE,
		Collections.singletonList(GpxParameter.MAX_SPEED),
		MeasureUnitType.SPEED,
		listOf(0.0, TrackFiltersConstants.SPEED_MAX_VALUE.toDouble()),
		false),
	UPHILL(
		R.string.shared_string_uphill,
		FilterDisplayType.RANGE,
		Collections.singletonList(GpxParameter.DIFF_ELEVATION_UP),
		MeasureUnitType.ALTITUDE,
		listOf(0.0, TrackFiltersConstants.ALTITUDE_MAX_VALUE.toDouble()),
		false),
	DOWNHILL(
		R.string.shared_string_downhill,
		FilterDisplayType.RANGE,
		Collections.singletonList(GpxParameter.DIFF_ELEVATION_DOWN),
		MeasureUnitType.ALTITUDE,
		listOf(0.0, TrackFiltersConstants.ALTITUDE_MAX_VALUE.toDouble()),
		false),
	AVERAGE_ALTITUDE(
		R.string.average_altitude,
		FilterDisplayType.RANGE,
		Collections.singletonList(GpxParameter.AVG_ELEVATION),
		MeasureUnitType.ALTITUDE,
		listOf(0.0, TrackFiltersConstants.ALTITUDE_MAX_VALUE.toDouble()),
		false),
	MAX_ALTITUDE(
		R.string.max_altitude,
		FilterDisplayType.RANGE,
		Collections.singletonList(GpxParameter.MAX_ELEVATION),
		MeasureUnitType.ALTITUDE,
		listOf(0.0, TrackFiltersConstants.ALTITUDE_MAX_VALUE.toDouble()),
		false),
	DATE_CREATION(
		R.string.date_of_creation,
		FilterDisplayType.DATE_RANGE,
		Collections.singletonList(GpxParameter.FILE_CREATION_TIME),
		MeasureUnitType.NONE,
		null,
		false),
	FOLDER(
		R.string.folder,
		FilterDisplayType.SINGLE_FIELD_LIST,
		Collections.singletonList(GpxParameter.FILE_DIR),
		MeasureUnitType.NONE,
		null,
		true,
		FolderCollectionTrackFilterParams()),
	CITY(
		R.string.nearest_cities,
		FilterDisplayType.SINGLE_FIELD_LIST,
		Collections.singletonList(GpxParameter.NEAREST_CITY_NAME),
		MeasureUnitType.NONE,
		null,
		false,
		CollectionTrackFilterParams()),
	COLOR(
		R.string.shared_string_color,
		FilterDisplayType.SINGLE_FIELD_LIST,
		Collections.singletonList(GpxParameter.COLOR),
		MeasureUnitType.NONE,
		null,
		false,
		ColorCollectionTrackFilterParams()),
	WIDTH(
		R.string.shared_string_width,
		FilterDisplayType.SINGLE_FIELD_LIST,
		Collections.singletonList(GpxParameter.WIDTH),
		MeasureUnitType.NONE,
		null,
		false,
		WidthCollectionTrackFilterParams()),
	OTHER(
		R.string.shared_string_other,
		FilterDisplayType.MULTI_FIELD_LIST,
		Collections.emptyList(),
		MeasureUnitType.NONE,
		null,
		false,
		listOf(VISIBLE_ON_MAP, WITH_WAYPOINTS));

}