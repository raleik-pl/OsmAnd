package net.osmand.plus.myplaces.tracks.filters

import android.app.Activity
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import net.osmand.plus.OsmandApplication
import net.osmand.plus.R
import net.osmand.plus.myplaces.tracks.TracksSearchFilter
import net.osmand.plus.myplaces.tracks.filters.viewholders.FilterCityViewHolder
import net.osmand.plus.myplaces.tracks.filters.viewholders.FilterDateViewHolder
import net.osmand.plus.myplaces.tracks.filters.viewholders.FilterDurationViewHolder
import net.osmand.plus.myplaces.tracks.filters.viewholders.FilterNameViewHolder
import net.osmand.plus.myplaces.tracks.filters.viewholders.FilterNameViewHolder.TextChangedListener
import net.osmand.plus.myplaces.tracks.filters.viewholders.FilterOtherViewHolder
import net.osmand.plus.myplaces.tracks.filters.viewholders.FilterRangeViewHolder
import net.osmand.plus.utils.UiUtilities

class FiltersAdapter(
	private val app: OsmandApplication,
	private val activity: Activity,
	private val fragmentManager: FragmentManager,
	private val filter: TracksSearchFilter,
	private val nightMode: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {

	private var items = filter.currentFilters

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		val inflater = UiUtilities.getInflater(parent.context, nightMode)

		return when (FilterDisplayType.values()[viewType]) {
			FilterDisplayType.TEXT -> {
				val view = inflater.inflate(R.layout.filter_name_item, parent, false)
				FilterNameViewHolder(view, nightMode)
			}

			FilterDisplayType.RANGE -> {
				val view = inflater.inflate(R.layout.filter_range_item, parent, false)
				FilterRangeViewHolder(view, nightMode)
			}

			FilterDisplayType.DATE_RANGE -> {
				val view = inflater.inflate(R.layout.filter_date_item, parent, false)
				FilterDateViewHolder(view, nightMode)
			}

			FilterDisplayType.SINGLE_FIELD_LIST -> {
				val view = inflater.inflate(R.layout.filter_single_field_list_item, parent, false)
				FilterCityViewHolder(
					app,
					view,
					nightMode)
			}

			FilterDisplayType.MULTI_FIELD_LIST -> {
				val view = inflater.inflate(R.layout.filter_single_field_list_item, parent, false)
				FilterOtherViewHolder(view, nightMode)
			}

			//todo remove
			else -> {val view = inflater.inflate(R.layout.filter_date_item, parent, false)
				FilterDateViewHolder(view, nightMode)}
		}
	}

	override fun getItemViewType(position: Int): Int {
		val filter = items[position]
		return filter.filterType.filterDisplayType.ordinal
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val item = items[position]
		if (holder is FilterNameViewHolder) {
			holder.bindView((item as TrackNameFilter).value,
				object : TextChangedListener {
					override fun onTextChanged(newText: String) {
						item.value = newText
					}
				})
		} else if (holder is FilterRangeViewHolder) {
			holder.bindView(item as RangeTrackFilter<*>)
		} else if (holder is FilterDateViewHolder) {
			holder.bindView(item as DateCreationTrackFilter, activity)
		} else if (holder is FilterCityViewHolder) {
			holder.bindView(item as ListTrackFilter, fragmentManager)
		} else if (holder is FilterOtherViewHolder) {
			holder.bindView(item as OtherTrackFilter)

		//		} else if (holder is FilterDurationViewHolder) {
//			holder.bindView(item as RangeTrackFilter<*>)
//			if (item.filterType == FilterType.TIME_IN_MOTION) {
//				holder.bindView(item as TimeInMotionTrackFilter)
//			} else if (item.filterType == FilterType.LENGTH) {
//				holder.bindView(item as LengthTrackFilter)
//			} else if (item.filterType == FilterType.AVERAGE_SPEED) {
//				holder.bindView(item as AverageSpeedTrackFilter)
//			} else if (item.filterType == FilterType.MAX_SPEED) {
//				holder.bindView(item as MaxSpeedTrackFilter)
//			} else if (item.filterType == FilterType.AVERAGE_ALTITUDE) {
//				holder.bindView(item as AverageAltitudeTrackFilter)
//			} else if (item.filterType == FilterType.MAX_ALTITUDE) {
//				holder.bindView(item as MaxAltitudeTrackFilter)
//			} else if (item.filterType == FilterType.UPHILL) {
//				holder.bindView(item as UphillTrackFilter)
//			} else if (item.filterType == FilterType.DOWNHILL) {
//				holder.bindView(item as DownhillTrackFilter)
//			}
//		} else if (holder is FilterCityViewHolder) {
//			holder.bindView(item as CityTrackFilter, fragmentManager)
//		} else if (holder is FilterColorViewHolder) {
//			holder.bindView(item as ColorTrackFilter, fragmentManager)
//		} else if (holder is FilterWidthViewHolder) {
//			holder.bindView(item as WidthTrackFilter, fragmentManager)
//		} else if (holder is FilterFolderViewHolder) {
//			holder.bindView(item as TrackFolderFilter, fragmentManager)
//		} else if (holder is FilterOtherViewHolder) {
//			holder.bindView(item as OtherTrackFilter)
		}
	}

	override fun getItemCount(): Int {
		return items.size
	}

	override fun getFilter(): Filter {
		return filter
	}

	fun onTracksFilteringComplete() {
		for (i in 0 until items.size) {
			if (items[i].filterType.updateOnOtherFiltersChangeNeeded) {
				notifyItemChanged(i)
			}
		}
	}

	private fun updateItem(item: Any) {
		val index = items.indexOf(item)
		if (index != -1) {
			notifyItemChanged(index)
		}
	}

	fun onItemsSelected(items: Set<Any>) {
		for (item in items) {
			updateItem(item)
		}
	}

	fun updateItems() {
		items = filter.currentFilters
	}
}