package net.osmand.plus.myplaces.tracks.filters

import com.google.gson.annotations.Expose
import net.osmand.CollatorStringMatcher
import net.osmand.plus.R
import net.osmand.plus.configmap.tracks.TrackItem
import net.osmand.plus.myplaces.tracks.filters.FilterType.TEXT
import net.osmand.search.core.SearchPhrase
import net.osmand.util.Algorithms

class TrackNameFilter(filterChangedListener: FilterChangedListener?)
	: BaseTrackFilter(R.string.shared_string_name, TEXT, filterChangedListener) {

//	private var nameMatcher = createMatcher()
//
//	private fun createMatcher(): SearchPhrase.NameStringMatcher {
//		return SearchPhrase.NameStringMatcher(
//			value.trim { it <= ' ' },
//			CollatorStringMatcher.StringMatcherMode.CHECK_CONTAINS)
//	}
//
//	override fun isTrackAccepted(trackItem: TrackItem): Boolean {
//		if (nameMatcher == null) {
//			updateMatcher()
//		}
//		return nameMatcher.matches(trackItem.name)
//	}

	override fun isEnabled(): Boolean {
		return !Algorithms.isEmpty(getValue() as String)
	}

	override fun initWithValue(value: BaseTrackFilter) {
		if (value is TrackNameFilter) {
			this.setValue(value.getValue())
//			updateMatcher()
		}
	}

	override fun equals(other: Any?): Boolean {
		return super.equals(other) &&
				other is TrackNameFilter &&
				Algorithms.stringsEqual(other.getValue() as String, getValue() as String)
	}
}