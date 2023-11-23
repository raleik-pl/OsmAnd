package net.osmand.plus.myplaces.tracks.filters

import net.osmand.plus.OsmandApplication
import net.osmand.plus.R
import net.osmand.plus.configmap.tracks.TrackItem
import net.osmand.plus.myplaces.tracks.filters.FilterType.TEXT

class MaxAltitudeTrackFilter(
	minValue: Float,
	maxValue: Float,
	app: OsmandApplication, filterChangedListener: FilterChangedListener?) : RangeTrackFilter(
	minValue,
	maxValue,
	app, R.string.max_altitude, TEXT, filterChangedListener) {
	override val unitResId = R.string.m

	override fun isTrackAccepted(trackItem: TrackItem): Boolean {
		val elevation = trackItem.dataItem?.gpxData?.analysis?.maxElevation
		return if (elevation == null)
			false
		else
			true
//			elevation > valueFrom && elevation < valueTo
//					|| elevation < minValue && valueFrom == minValue
//					|| elevation > maxValue && valueTo == maxValue
	}
}