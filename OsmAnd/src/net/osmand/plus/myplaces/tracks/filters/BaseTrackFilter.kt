package net.osmand.plus.myplaces.tracks.filters

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import net.osmand.CollatorStringMatcher
import net.osmand.plus.configmap.tracks.TrackItem
import net.osmand.plus.track.helpers.GpxParameter
import net.osmand.search.core.SearchPhrase
import net.osmand.util.Algorithms
import java.util.Collections
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

open class BaseTrackFilter(
	val displayNameId: Int,
	@Expose
	@SerializedName("filterType") val filterType: FilterType,
	@Expose val measureUnitType: MeasureUnitType,
	var filterChangedListener: FilterChangedListener?,
	val trackParam: GpxParameter,
	var defaultParams: List<Any>) {


	@Deprecated("delete while refactoring")
	constructor(
		displayNameId: Int,
		filterType: FilterType,
		filterChangedListener: FilterChangedListener?) : this(
		displayNameId,
		filterType,
		MeasureUnitType.NONE,
		filterChangedListener,
		GpxParameter.GPX_COL_NAME,
		Collections.emptyList()
	)


	@Expose
	lateinit var params: MutableList<Any>

	@Expose
	var selectedItems = ArrayList<String>()
		private set

	@Expose
	var isSelectAllItemsSelected = false
		set(value) {
			field = value
			filterChangedListener?.onFilterChanged()
		}

	private var nameMatcher: SearchPhrase.NameStringMatcher

	init {
		if (Algorithms.isEmpty(defaultParams)) {
			throw IllegalArgumentException("Default params should not be empty")
		}
		params = ArrayList(defaultParams)
		nameMatcher = createMatcher()
	}


	private fun updateMatcher() {
		nameMatcher = createMatcher()
	}

	private fun createMatcher(): SearchPhrase.NameStringMatcher {
		var value = params[0]
		if (value !is String) {
			value = ""
		}
		return SearchPhrase.NameStringMatcher(
			value.trim { it <= ' ' },
			CollatorStringMatcher.StringMatcherMode.CHECK_CONTAINS)
	}

	fun getSelectedItems(): List<String> {
		return ArrayList(selectedItems)
	}

	var collectionFilterParams: CollectionTrackFilterParams? = null

	var allItemsCollection: HashMap<String, Int> = hashMapOf()


//	var value = ""
//		set(value) {
//			if (!Algorithms.stringsEqual(field, value)) {
//				field = value
//				updateMatcher()
//			}
//			filterChangedListener?.onFilterChanged()
//		}

	fun setValue(newValue: Any) {
		if (!Algorithms.isEmpty(params)) {
			val currentValue = params[0]
			if (newValue::class == currentValue::class && !Algorithms.objectEquals(
					currentValue,
					newValue)) {
				params[0] = newValue
				updateMatcher()
				filterChangedListener?.onFilterChanged()
			}
//			if (!Algorithms.stringsEqual(field, value)) {
//				field = value
//				updateMatcher()
//			}
		}
	}

	fun getValue(): Any {
		return params[0]
	}


	fun setSelectedItems(selectedItems: List<String>) {
		this.selectedItems = ArrayList(selectedItems)
		filterChangedListener?.onFilterChanged()
	}

	fun setFullItemsCollection(collection: HashMap<String, Int>) {
//		allItems = ArrayList(collection.keys)
		allItemsCollection = collection
	}

	open fun isEnabled(): Boolean {
		return !Algorithms.objectEquals(defaultParams, params)
	}

	open fun isTrackAccepted(trackItem: TrackItem): Boolean {
		return when (filterType) {
			FilterType.TEXT -> {
				if (nameMatcher == null) {
					updateMatcher()
				}
				nameMatcher.matches(trackItem.name)

			}
			FilterType.RANGE -> {
				val trackValue = trackItem.dataItem?.gpxData?.getValue(trackParam) as Float

				trackValue?.let { trackValue ->
					trackValue > getValueFrom() && trackValue < getValueTo()
				|| trackValue < getMinValue() && getValueFrom() == getMinValue()
				|| trackValue > getMaxValue() && getValueTo() == getMaxValue()
				}

				val duration = trackItem.dataItem?.gpxData?.analysis?.timeSpan
						if (duration == null || (duration == 0L)) {
							return false
						}
				val durationMinutes = duration.toDouble() / 1000 / 60
				return true
//		durationMinutes > valueFrom && durationMinutes < valueTo
//				|| durationMinutes < minValue && valueFrom == minValue
//				|| durationMinutes > maxValue && valueTo == maxValue
			}

			else -> true
		}
	}

	open fun initWithValue(value: BaseTrackFilter) {
		defaultParams = ArrayList(value.defaultParams)
		params = ArrayList(value.params)
		filterChangedListener?.onFilterChanged()
	}

	open fun initFilter() {}

	override fun equals(other: Any?): Boolean {
		return other is BaseTrackFilter && other.filterType == filterType
	}

	open fun updateOnOtherFiltersChangeNeeded(): Boolean {
		return false
	}

	fun clone(): BaseTrackFilter {
		val newFilter = BaseTrackFilter(
			displayNameId,
			filterType,
			measureUnitType,
			null,
			trackParam,
			ArrayList(defaultParams))
		newFilter.params = ArrayList(params)
		return newFilter
	}

	fun addSelectedItems(selectedItems: List<String>) {
		this.selectedItems.addAll(selectedItems)
	}

	fun clearSelectedItems() {
		setSelectedItems(ArrayList())
	}


	fun setItemSelected(item: String, selected: Boolean) {
		if (selected) {
			selectedItems.add(item)
		} else {
			selectedItems.remove(item)
		}
		filterChangedListener?.onFilterChanged()
	}

	fun isItemSelected(item: String): Boolean {
		return selectedItems.contains(item)
	}

	fun getTracksCountForItem(itemName: String): Int {
		return allItemsCollection[itemName] ?: 0
	}

	fun getMinValue(): Float {
		return params[0] as Float
	}

	fun getMaxValue(): Float {
		return params[1] as Float
	}

	fun setMaxValue(value: Float) {
		if(value > getMinValue()) {
			params[1] = value
			setValueTo(value, false)
			filterChangedListener?.onFilterChanged()
		}
	}


	fun getValueFrom(): Float {
		return params[2] as Float
	}

	fun getValueTo(): Float {
		return params[3] as Float
	}


	open fun setValueFrom(from: Float, updateListeners: Boolean = true) {
		params[2] = max(getMinValue(), from)
		params[2] = min(getValueFrom(), getValueTo())
		if (updateListeners) {
			filterChangedListener?.onFilterChanged()
		}
	}

	open fun setValueTo(to: Float, updateListeners: Boolean = true) {
		params[3] = to
		if (getValueTo() > getMaxValue()) {
			params[1] = getValueTo()
		}
		params[3] = max(getValueFrom(), getValueTo())
		if (updateListeners) {
			filterChangedListener?.onFilterChanged()
		}
	}


	open fun getDisplayMinValue(): Int {
		return floor(getMinValue()).toInt()
	}

	open fun getDisplayMaxValue(): Int {
		return ceil(getMaxValue()).toInt()
	}

	open fun getDisplayValueFrom(): Int {
		return floor(getValueFrom()).toInt()
	}

	open fun getDisplayValueTo(): Int {
		return ceil(getValueTo()).toInt()
	}


}