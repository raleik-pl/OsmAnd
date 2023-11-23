package net.osmand.plus.myplaces.tracks.filters

import net.osmand.plus.OsmandApplication
import net.osmand.plus.R
import net.osmand.plus.configmap.tracks.TrackItem
import net.osmand.plus.myplaces.tracks.filters.FilterType.TEXT

class AverageAltitudeTrackFilter(
	minValue: Float,
	maxValue: Float,
	app: OsmandApplication,
	filterChangedListener: FilterChangedListener?) : RangeTrackFilter(
	minValue,
	maxValue,
	app, R.string.average_altitude, TEXT, filterChangedListener) {
	override val unitResId = R.string.m

	override fun isTrackAccepted(trackItem: TrackItem): Boolean {
		val elevation = trackItem.dataItem?.gpxData?.analysis?.avgElevation
		if (elevation == null || (elevation == 0.0)) {
			return false
		}
		return elevation > getValueFrom() && elevation < getValueTo()
				|| elevation < getMinValue() && getValueFrom() == getMinValue()
				|| elevation > getMaxValue() && getValueTo() == getMaxValue()
	}
}