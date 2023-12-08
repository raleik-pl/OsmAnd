package net.osmand.plus.myplaces.tracks.filters

import com.google.gson.annotations.Expose
import net.osmand.plus.OsmandApplication
import net.osmand.plus.configmap.tracks.TrackItem
import net.osmand.plus.track.helpers.GpxParameter
import net.osmand.util.Algorithms

class OtherTrackFilter(
	val app: OsmandApplication,
	filterType: FilterType,
	filterChangedListener: FilterChangedListener?) :
	BaseTrackFilter(filterType, filterChangedListener) {

	@Expose
	var params = HashMap<GpxParameter, Boolean>()

	init {
		for (param in filterType.propertyList) {
			params[param] = false
		}
	}

	override fun isEnabled(): Boolean {
		for (value in params.values) {
			if (value) {
				return true
			}
		}
		return false
	}

	fun setItemSelected(param: GpxParameter, selected: Boolean) {
		params[param] = selected
		filterChangedListener?.onFilterChanged()
	}

//	@Expose
//	var isVisibleOnMap: Boolean = false
//		set(value) {
//			field = value
//			filterChangedListener?.onFilterChanged()
//		}
//
//	@Expose
//	var hasWaypoints: Boolean = false
//		set(value) {
//			field = value
//			filterChangedListener?.onFilterChanged()
//		}

	override fun isTrackAccepted(trackItem: TrackItem): Boolean {
		for (parameter in params.keys){
			if(params[parameter] == true) {
				if(trackItem.dataItem?.gpxData?.getValue(parameter) == false) {
					return false
				}
			}
		}
//
//		if (isVisibleOnMap) {
//			val selectedGpxHelper = app.selectedGpxHelper
//			if (selectedGpxHelper.getSelectedFileByPath(trackItem.path) == null) {
//				return false
//			}
//		}
//		if (hasWaypoints) {
//			val wptPointsCount = trackItem.dataItem?.gpxData?.analysis?.wptPoints ?: 0
//			if (wptPointsCount == 0) {
//				return false
//			}
//		}
		return true
	}

	fun getSelectedParamsCount(): Int {
		var selectedCount = 0;
		for (value in params.values){
			if(value) {
				selectedCount++
			}
		}
//		if (isVisibleOnMap) selectedCount++
//		if (hasWaypoints) selectedCount++
		return selectedCount
	}

	override fun initWithValue(value: BaseTrackFilter) {
		if (value is OtherTrackFilter) {
			for (parameter in params.keys){
				params[parameter] = value.params[parameter]!!
			}

//			isVisibleOnMap = sourceFilter.isVisibleOnMap
//			hasWaypoints = sourceFilter.hasWaypoints
			filterChangedListener?.onFilterChanged()
		}
	}

	override fun equals(other: Any?): Boolean {
		return super.equals(other) &&
				other is OtherTrackFilter &&
				other.filterType == filterType &&
				Algorithms.mapsEquals(other.params, params)
//				other.isVisibleOnMap == isVisibleOnMap &&
//				other.hasWaypoints == hasWaypoints
	}

}