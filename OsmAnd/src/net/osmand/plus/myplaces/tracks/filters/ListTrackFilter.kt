package net.osmand.plus.myplaces.tracks.filters

import android.graphics.drawable.Drawable
import android.util.Pair
import com.google.gson.annotations.Expose
import net.osmand.plus.OsmandApplication
import net.osmand.plus.configmap.tracks.TrackItem
import net.osmand.plus.track.data.TrackFolder
import net.osmand.util.Algorithms
import java.lang.IllegalArgumentException

open class ListTrackFilter(
	val app: OsmandApplication,
	filterType: FilterType,
	filterChangedListener: FilterChangedListener?) :
	BaseTrackFilter(filterType, filterChangedListener) {

	var collectionFilterParams: CollectionTrackFilterParams

	init {
		val additionalData = filterType.additionalData
		if(additionalData == null || additionalData !is CollectionTrackFilterParams) {
			throw IllegalArgumentException("additionalData in $filterType filter should be valid instance of CollectionTrackFilterParams")
		}
		collectionFilterParams = additionalData
	}

	var currentFolder: TrackFolder? = null
		set(value) {
			field = value
			value?.let {
				setSelectedItems(arrayListOf(it.getDirName()))
			}
		}

	fun updateFullCollection(items: List<TrackItem>?) {
		if (Algorithms.isEmpty(items)) {
			allItemsCollection = HashMap()
		} else {
			val newCollection = HashMap<String, Int>()
			for (item in items!!) {
				val folderName = item.dataItem?.gpxData?.containingFolder ?: ""
				val count = newCollection[folderName] ?: 0
				newCollection[folderName] = count + 1
			}
			allItemsCollection = newCollection
		}
	}

	override fun isEnabled(): Boolean {
		return !Algorithms.isEmpty(selectedItems)
	}

	@Expose
	var selectedItems = ArrayList<String>()
		protected set
	var allItems: MutableList<String> = arrayListOf()
		private set
	var allItemsCollection: HashMap<String, Int> = hashMapOf()

	fun setFullItemsCollection(collection: HashMap<String, Int>) {
		allItems = ArrayList(collection.keys)
		allItemsCollection = collection
	}

	@Expose
	var isSelectAllItemsSelected = false
		set(value) {
			field = value
			filterChangedListener?.onFilterChanged()
		}

	fun setFullItemsCollection(collection: List<Pair<String, Int>>) {
		val tmpAllItems = ArrayList<String>()
		val tmpAllItemsCollection = HashMap<String, Int>()
		for (pair in collection) {
			tmpAllItems.add(pair.first)
			tmpAllItemsCollection[pair.first] = pair.second
		}
		allItems = tmpAllItems
		allItemsCollection = tmpAllItemsCollection
	}

	fun setSelectedItems(selectedItems: List<String>) {
		this.selectedItems = ArrayList(selectedItems)
		filterChangedListener?.onFilterChanged()
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

	override fun initWithValue(value: BaseTrackFilter) {
		if (value is ListTrackFilter) {
			setSelectedItems(
				if (value.selectedItems == null) {
					ArrayList()
				} else {
					ArrayList(value.selectedItems)
				})
			for (item in value.selectedItems) {
				if (!allItems.contains(item)) {
					allItems.add(item)
					allItemsCollection[item] = 0
				}
			}
			filterChangedListener?.onFilterChanged()
		}
	}

	fun areAllItemsSelected(items: List<String>): Boolean {
		for (item in items) {
			if (!isItemSelected(item)) {
				return false
			}
		}
		return true
	}

	open fun getItemText(itemName: String): String {
		return itemName
	}

	open fun getItemIcon(itemName: String): Drawable? {
		return null
	}

	open fun getSelectAllItemIcon(isChecked: Boolean, nightMode: Boolean): Drawable? {
		return null
	}

	fun getTracksCountForItem(itemName: String): Int {
		return allItemsCollection[itemName] ?: 0
	}

	open fun hasSelectAllVariant(): Boolean {
		return false
	}

	fun addSelectedItems(selectedItems: List<String>) {
		this.selectedItems.addAll(selectedItems)
	}

	fun clearSelectedItems() {
		selectedItems = ArrayList()
	}



//	@Expose
//	private var selectedCities = ArrayList<String>()

	override fun isTrackAccepted(trackItem: TrackItem): Boolean {
		val trackItemPropertyValue = getTrackPropertyValue(trackItem)
		for (item in selectedItems) {
			if (Algorithms.stringsEqual(trackItemPropertyValue, item)) {
				return true
			}
		}
		return false
	}

	private fun getTrackPropertyValue(trackItem: TrackItem): String {
		val value = trackItem.dataItem?.gpxData?.getValue(filterType.propertyList[0])
		return if(value != null) value as String else ""
	}

	override fun equals(other: Any?): Boolean {
		return super.equals(other) &&
				other is ListTrackFilter &&
				other.selectedItems.size == selectedItems.size &&
				areAllItemsSelected(other.selectedItems)
	}

//	override fun initWithValue(value: BaseTrackFilter) {
//		if(value is CityTrackFilter) {
//			if(!Algorithms.isEmpty(value.selectedCities) || Algorithms.isEmpty(value.selectedItems)){
//				value.setSelectedItems(ArrayList(value.selectedCities))
//			}
//		}
//		super.initWithValue(value)
//	}



}