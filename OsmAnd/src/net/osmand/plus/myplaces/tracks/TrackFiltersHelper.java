package net.osmand.plus.myplaces.tracks;

import androidx.annotation.NonNull;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.myplaces.tracks.filters.BaseTrackFilter;
import net.osmand.plus.myplaces.tracks.filters.DateCreationTrackFilter;
import net.osmand.plus.myplaces.tracks.filters.FilterChangedListener;
import net.osmand.plus.myplaces.tracks.filters.FilterType;
import net.osmand.plus.myplaces.tracks.filters.ListTrackFilter;
import net.osmand.plus.myplaces.tracks.filters.OtherTrackFilter;
import net.osmand.plus.myplaces.tracks.filters.RangeTrackFilter;
import net.osmand.plus.myplaces.tracks.filters.TrackNameFilter;
import net.osmand.plus.track.helpers.GpxParameter;

import java.util.Date;

public class TrackFiltersHelper {

	public static BaseTrackFilter createNameFilter(FilterType filterType, FilterChangedListener listener) {
		return new TrackNameFilter(filterType, listener);
	}

	public static BaseTrackFilter createDateFilter(FilterType filterType, Long minDate, FilterChangedListener listener) {
		return new DateCreationTrackFilter(filterType, minDate, listener);
	}

	public static BaseTrackFilter createOtherFilter(OsmandApplication app, FilterType filterType, FilterChangedListener listener) {
		return new OtherTrackFilter(app, filterType, listener);
	}

	public static BaseTrackFilter createSingleListFilter(OsmandApplication app, FilterType filterType, FilterChangedListener listener) {
		return new ListTrackFilter(app, filterType, listener);
	}

	public static BaseTrackFilter createRangeFilter(OsmandApplication app, FilterType filterType, FilterChangedListener listener) {
		if (filterType.getDefaultParams().size() < 2) {
			throw new IllegalArgumentException("RangeTrackFilter needs 2 default params: minValue, maxValue");
		}
		Object minValue = filterType.getDefaultParams().get(0);
		Object maxValue = filterType.getDefaultParams().get(1);
		Class<?> parameterClass = filterType.getPropertyList().get(0).getTypeClass();
		if (parameterClass == Double.class) {
			return new RangeTrackFilter<>((Double)minValue, (Double)maxValue, app, filterType, listener);
		} else if(parameterClass == Float.class) {
			return new RangeTrackFilter<>((Float)minValue, (Float)maxValue, app, filterType, listener);
		} else if(parameterClass == Integer.class) {
			return new RangeTrackFilter<>((Integer)minValue, (Integer)maxValue, app, filterType, listener);
		} else if(parameterClass == Long.class) {
			return new RangeTrackFilter<>((Long)minValue, (Long)maxValue, app, filterType, listener);
		}
		throw new IllegalArgumentException("Unsupported gpxParameter type class");
	}

	public static Comparable<?> getComparableValueForFilter(@NonNull String value, @NonNull GpxParameter gpxParam){
		if (gpxParam.getTypeClass().equals(Double.class)) {
			return Double.parseDouble(value);
		} else if(gpxParam.getTypeClass().equals(Float.class)) {
			return Float.parseFloat(value);
		} else if(gpxParam.getTypeClass().equals(Integer.class)) {
			return Integer.parseInt(value);
		} else if(gpxParam.getTypeClass().equals(Long.class)) {
			return Long.parseLong(value);
		}
		throw new IllegalArgumentException("Unsupported gpxParameter type class");
	}

	public static BaseTrackFilter createFilter(OsmandApplication app, FilterType filterType, FilterChangedListener filterChangedListener) {
		BaseTrackFilter newFilter;

		switch (filterType.getFilterDisplayType()) {
			case TEXT: {
				newFilter = createNameFilter(filterType, filterChangedListener);
				break;
			}
			case RANGE: {
				newFilter = createRangeFilter(app, filterType, filterChangedListener);
				break;
			}
			case DATE_RANGE: {
				newFilter = createDateFilter(filterType, (new Date()).getTime(), filterChangedListener);
				break;
			}

			case MULTI_FIELD_LIST: {
				newFilter = createOtherFilter(app, filterType, filterChangedListener);
				break;
			}

			case SINGLE_FIELD_LIST: {
				newFilter = createSingleListFilter(app, filterType, filterChangedListener);
				break;
			}

//			case DURATION: {
//				newFilter = new DurationTrackFilter(
//						0f,
//						TrackFiltersConstants.DEFAULT_MAX_VALUE,
//						app, filterChangedListener);
//				break;
//			}
//			case TIME_IN_MOTION: {
//				newFilter = new TimeInMotionTrackFilter(
//						0f,
//						TrackFiltersConstants.DEFAULT_MAX_VALUE,
//						app, filterChangedListener);
//				break;
//			}
//
//			case LENGTH: {
//				newFilter = new LengthTrackFilter(
//						0f,
//						TrackFiltersConstants.LENGTH_MAX_VALUE,
//						app, filterChangedListener);
//				break;
//			}
//
//			case AVERAGE_SPEED: {
//				newFilter = new AverageSpeedTrackFilter(
//						0f,
//						TrackFiltersConstants.DEFAULT_MAX_VALUE,
//						app, filterChangedListener);
//				break;
//			}
//
//			case MAX_SPEED: {
//				newFilter = new MaxSpeedTrackFilter(
//						0f,
//						TrackFiltersConstants.DEFAULT_MAX_VALUE,
//						app, filterChangedListener);
//				break;
//			}
//
//			case AVERAGE_ALTITUDE: {
//				newFilter = new AverageAltitudeTrackFilter(
//						0f,
//						TrackFiltersConstants.DEFAULT_MAX_VALUE,
//						app, filterChangedListener);
//				break;
//			}
//
//			case MAX_ALTITUDE: {
//				newFilter = new MaxAltitudeTrackFilter(
//						0f,
//						TrackFiltersConstants.ALTITUDE_MAX_VALUE,
//						app, filterChangedListener);
//				break;
//			}
//
//			case UPHILL: {
//				newFilter = new UphillTrackFilter(
//						0f,
//						TrackFiltersConstants.DEFAULT_MAX_VALUE,
//						app, filterChangedListener);
//				break;
//			}
//
//			case DOWNHILL: {
//				newFilter = new DownhillTrackFilter(
//						0f,
//						TrackFiltersConstants.DEFAULT_MAX_VALUE,
//						app, filterChangedListener);
//				break;
//			}
//
//			case DATE_CREATION: {
//				newFilter = new DateCreationTrackFilter(filterChangedListener);
//				break;
//			}
//			case CITY: {
//				newFilter = new CityTrackFilter(app, filterChangedListener);
//				break;
//			}
//			case FOLDER: {
//				newFilter = new TrackFolderFilter(app, filterChangedListener);
//				break;
//			}
//			case OTHER: {
//				newFilter = new OtherTrackFilter(app, filterChangedListener);
//				break;
//			}
//			case COLOR: {
//				newFilter = new ColorTrackFilter(app, filterChangedListener);
//				break;
//			}
//			case WIDTH: {
//				newFilter = new WidthTrackFilter(app, filterChangedListener);
//				break;
//			}
			default:
				throw new IllegalArgumentException("Unknown filterType " + filterType);
		}
		return newFilter;
	}

	@NonNull
	public static Class<? extends BaseTrackFilter> getFilterClass(FilterType filterType) {
		Class<? extends BaseTrackFilter> filterClass = null;
		switch (filterType) {
			case NAME: {
				filterClass = TrackNameFilter.class;
				break;
			}
//			case FOLDER: {
//				filterClass = TrackFolderFilter.class;
//				break;
//			}
//			case DURATION: {
//				filterClass = DurationTrackFilter.class;
//				break;
//			}
//			case TIME_IN_MOTION: {
//				filterClass = TimeInMotionTrackFilter.class;
//				break;
//			}
//			case LENGTH: {
//				filterClass = LengthTrackFilter.class;
//				break;
//			}
//			case AVERAGE_SPEED: {
//				filterClass = AverageSpeedTrackFilter.class;
//				break;
//			}
//			case MAX_SPEED: {
//				filterClass = MaxSpeedTrackFilter.class;
//				break;
//			}
//			case AVERAGE_ALTITUDE: {
//				filterClass = AverageAltitudeTrackFilter.class;
//				break;
//			}
//			case MAX_ALTITUDE: {
//				filterClass = MaxAltitudeTrackFilter.class;
//				break;
//			}
//			case UPHILL: {
//				filterClass = UphillTrackFilter.class;
//				break;
//			}
//			case DOWNHILL: {
//				filterClass = DownhillTrackFilter.class;
//				break;
//			}
//			case DATE_CREATION: {
//				filterClass = DateCreationTrackFilter.class;
//				break;
//			}
//			case CITY: {
//				filterClass = CityTrackFilter.class;
//				break;
//			}
//			case OTHER: {
//				filterClass = OtherTrackFilter.class;
//				break;
//			}
//			case COLOR: {
//				filterClass = ColorTrackFilter.class;
//				break;
//			}
//			case WIDTH: {
//				filterClass = WidthTrackFilter.class;
//				break;
//			}
			default:
				throw new IllegalArgumentException("Unknown filterType " + filterType);
		}
		return filterClass;
	}
}