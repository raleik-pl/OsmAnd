package net.osmand.plus.myplaces.tracks.filters.viewholders

import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.osmand.plus.OsmandApplication
import net.osmand.plus.R
import net.osmand.plus.helpers.AndroidUiHelper
import net.osmand.plus.myplaces.tracks.filters.FilterChangedListener
import net.osmand.plus.myplaces.tracks.filters.ListFilterAdapter
import net.osmand.plus.myplaces.tracks.filters.OtherFilterAdapter
import net.osmand.plus.myplaces.tracks.filters.OtherTrackFilter
import net.osmand.plus.utils.UiUtilities
import net.osmand.plus.widgets.TextViewEx

class FilterOtherViewHolder(itemView: View, nightMode: Boolean) :
	RecyclerView.ViewHolder(itemView) {
	private val app: OsmandApplication
	private val nightMode: Boolean
	private var expanded = false
	private val title: TextViewEx
	private val selectedValue: TextViewEx
	private val titleContainer: View
//	private val paramsContainer: View
	private val explicitIndicator: ImageView
	private lateinit var filter: OtherTrackFilter
//	private var isVisibleOnMapCheckBox: AppCompatCheckBox
//	private var hasWaypointsCheckBox: AppCompatCheckBox
//	private var visibleOnMapRow: View
//	private var waypointsRow: View
	private val divider: View

	private val recycler: RecyclerView
	private val adapter:OtherFilterAdapter
	private val filterChangedListener = object: FilterChangedListener{
		override fun onFilterChanged() {
			updateValues()
		}
	}


	init {
		app = itemView.context.applicationContext as OsmandApplication
		this.nightMode = nightMode
		adapter = OtherFilterAdapter(app, nightMode, filterChangedListener)
		title = itemView.findViewById(R.id.title)
		selectedValue = itemView.findViewById(R.id.selected_value)
		explicitIndicator = itemView.findViewById(R.id.explicit_indicator)
//		paramsContainer = itemView.findViewById(R.id.params_container)
		titleContainer = itemView.findViewById(R.id.title_container)
		divider = itemView.findViewById(R.id.divider)
		titleContainer.setOnClickListener { v: View? ->
			expanded = !expanded
			updateExpandState()
		}
//		isVisibleOnMapCheckBox = itemView.findViewById(R.id.visible_check)
//		UiUtilities.setupCompoundButton(
//			nightMode,
//			net.osmand.plus.utils.ColorUtilities.getActiveColor(app, nightMode),
//			isVisibleOnMapCheckBox)
//		hasWaypointsCheckBox = itemView.findViewById(R.id.waypoint_check)
//		UiUtilities.setupCompoundButton(
//			nightMode,
//			net.osmand.plus.utils.ColorUtilities.getActiveColor(app, nightMode),
//			hasWaypointsCheckBox)
//		visibleOnMapRow = itemView.findViewById(R.id.visible_on_map_row)
//		waypointsRow = itemView.findViewById(R.id.waypoints_row)
		recycler = itemView.findViewById(R.id.variants)
	}

	fun bindView(filter: OtherTrackFilter) {
		this.filter = filter
		adapter.filter = filter
		title.setText(filter.filterType.nameResId)
//		visibleOnMapRow.setOnClickListener {
//			filter.isVisibleOnMap = !filter.isVisibleOnMap
//			updateValues()
//		}
//		waypointsRow.setOnClickListener {
//			filter.hasWaypoints = !filter.hasWaypoints
//			updateValues()
//		}
		updateExpandState()
		updateValues()
	}

	private fun updateExpandState() {
		val iconRes =
			if (expanded) R.drawable.ic_action_arrow_up else R.drawable.ic_action_arrow_down
		explicitIndicator.setImageDrawable(app.uiUtilities.getIcon(iconRes, !nightMode))
//		AndroidUiHelper.updateVisibility(paramsContainer, expanded)
		AndroidUiHelper.updateVisibility(recycler, expanded)
	}

	private fun updateValues() {
//		filter?.let {
			val selectedParamsCount = filter.getSelectedParamsCount()
			AndroidUiHelper.updateVisibility(selectedValue, selectedParamsCount > 0)
			selectedValue.text = "$selectedParamsCount"
//			isVisibleOnMapCheckBox.isChecked = it.isVisibleOnMap
//			hasWaypointsCheckBox.isChecked = it.hasWaypoints
//		}
		adapter.items = ArrayList(filter.parameters)
//		val trackFolder = filter.currentFolder
//		trackFolder?.let { currentTrackFolder ->
//			val currentFolderName = currentTrackFolder.getDirName()
//			adapter.items.remove(currentFolderName)
//			adapter.items.add(0, currentFolderName)
//		}
		recycler.adapter = adapter
		recycler.layoutManager = LinearLayoutManager(app)
		recycler.itemAnimator = null
		selectedValue.text = "${filter.selectedParams.size}"
		AndroidUiHelper.updateVisibility(selectedValue, filter.selectedParams.size > 0)



	}
}