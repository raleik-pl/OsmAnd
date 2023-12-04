package net.osmand.plus.myplaces.tracks.filters

import com.google.gson.annotations.Expose
import net.osmand.plus.OsmandApplication
import net.osmand.plus.configmap.tracks.TrackItem
import net.osmand.plus.settings.enums.MetricsConstants
import net.osmand.plus.track.helpers.GpxParameter
import net.osmand.plus.utils.OsmAndFormatter
import net.osmand.plus.utils.OsmAndFormatter.FormattedValue
import java.lang.IllegalArgumentException
import kotlin.math.ceil
import kotlin.math.floor

open class RangeTrackFilter<T : Comparable<T>>(
	minValue: T,
	maxValue: T,
	val app: OsmandApplication,
	filterType: FilterType,
	filterChangedListener: FilterChangedListener?)
	: BaseTrackFilter(filterType, filterChangedListener) {

	@Expose
	var minValue: T

	@Expose
	var maxValue: T
		private set

	@Expose
	var valueFrom: T

	@Expose
	var valueTo: T

	init {
		this.minValue = minValue
		this.maxValue = maxValue
		this.valueFrom = minValue
		this.valueTo = maxValue
	}

	open fun setValueFrom(from: Comparable<*>, updateListeners: Boolean = true) {
		check(from)?.let {
			valueFrom = maxOf(minValue, getComparableValue(it))
			valueFrom = minOf(valueFrom, valueTo)
			if (updateListeners) {
				filterChangedListener?.onFilterChanged()
			}
		}
	}

//	private fun max(value1: T, value2: T): T {
//		return if(getComparableValue(value1) >= value2) {
//			value1
//		} else {
//			value2
//		}
//		val propertyType = filterType.propertyList[0].typeClass
//		return when (propertyType){
//			java.lang.Double::class.java -> {
//				maxOf(value1 as Double, value2 as Double) as T
//			}
//
//			java.lang.Float::class.java -> {
//				maxOf(value1 as Float, value2 as Float) as T
//			}
//
//			java.lang.Integer::class.java -> {
//				maxOf(value1 as Int, value2 as Int) as T
//			}
//
//			java.lang.Long::class.java -> {
//				maxOf(value1 as Long, value2 as Long) as T
//			}
//			else -> {
//				throw IllegalArgumentException("Unsupported type $propertyType for max")
//			}
//		}
//	}


	fun setValueTo(to: String, updateListeners: Boolean = true) {
		when (filterType.propertyList[0].typeClass) {
			java.lang.Double::class.java -> {
				setValueTo(to.toDouble() as java.lang.Double, updateListeners)
			}

			java.lang.Float::class.java -> {
				setValueTo(to.toFloat() as java.lang.Float, updateListeners)
			}

			java.lang.Integer::class.java -> {
				setValueTo(to.toInt() as java.lang.Integer, updateListeners)
			}

			java.lang.Long::class.java -> {
				setValueTo(to.toLong() as java.lang.Long, updateListeners)
			}
		}
	}

	private fun setValueTo(to: Comparable<*>, updateListeners: Boolean = true) {
		check(to)?.let {
			valueTo = it
			if (valueTo > maxValue) {
				maxValue = valueTo
			}
			valueTo = maxOf(valueFrom, valueTo)
			if (updateListeners) {
				filterChangedListener?.onFilterChanged()
			}
		}
	}

	override fun isEnabled(): Boolean {
		return valueFrom > minValue || valueTo < maxValue
	}

	override fun isTrackAccepted(trackItem: TrackItem): Boolean {
		val value = trackItem.dataItem?.gpxData?.getValue(filterType.propertyList[0]) as T
		if (value == null) {
			return false
		}
		return value > valueFrom && value < valueTo
				|| value < minValue && valueFrom == minValue
				|| value > maxValue && valueTo == maxValue
	}

	override fun initWithValue(value: BaseTrackFilter) {
		if (value is RangeTrackFilter<*>) {
			check(value.minValue)?.let { minValue = it }
			check(value.maxValue)?.let { maxValue = it }
			check(value.valueFrom)?.let { valueFrom = it }
			check(value.valueTo)?.let { valueTo = it }
			super.initWithValue(value)
		}
	}

	@Suppress("UNCHECKED_CAST")
	private fun check(value: Comparable<*>): T? {
		return try {
			value as T
		} catch (err: ClassCastException) {
			null
		}
	}

	fun setMaxValue(value: T) {
		maxValue = value
		valueTo = value
	}

	override fun equals(other: Any?): Boolean {
		return super.equals(other) &&
				other is RangeTrackFilter<*> &&
				other.minValue == minValue &&
				other.maxValue == maxValue &&
				other.valueFrom == valueFrom &&
				other.valueTo == valueTo
	}

	open fun getDisplayMinValue(): Int {
		val formattedValue = getFormattedValue(flor(minValue))
		return formattedValue.valueSrc.toInt()

//		return flor(minValue)
	}

	private fun flor(value: T): String {
		return when (value) {
			is Float -> {
				floor(value as Float).toString()
			}

			is Double -> {
				floor(value as Double).toString()
			}

			else -> {
				value.toString()
			}
		}
	}

	private fun ceil(value: T): String {
		return when (value) {
			is Float -> {
				ceil(value as Float).toString()
			}

			is Double -> {
				ceil(value as Double).toString()
			}

			else -> {
				value.toString()
			}
		}
	}

	open fun getDisplayMaxValue(): Int {
		val formattedValue = getFormattedValue(ceil(maxValue))
		return formattedValue.valueSrc.toInt()
	}

	open fun getDisplayValueFrom(): String {
		return flor(valueFrom)
	}

	open fun getDisplayValueTo(): String {
		val formattedValue = getFormattedValue(ceil(valueTo))
		return formattedValue.valueSrc.toString()
	}

	private fun getFormattedValue(value: String): FormattedValue {
		val metricsConstants: MetricsConstants = app.settings.METRIC_SYSTEM.get()
		return when (filterType.measureUnitType) {
			MeasureUnitType.SPEED -> OsmAndFormatter.getFormattedSpeedValue(value.toFloat(), app)
			MeasureUnitType.ALTITUDE -> OsmAndFormatter.getFormattedAltitudeValue(
				value.toDouble(),
				app,
				metricsConstants)

			MeasureUnitType.DISTANCE -> OsmAndFormatter.getFormattedDistanceValue(
				value.toFloat(),
				app,
				true,
				metricsConstants)

			else -> FormattedValue(value.toFloat(), value.toString(), "")
		}
	}

	private fun getProperty(): GpxParameter {
		return filterType.propertyList[0]
	}

	private fun getComparableValue(value: T): T {
		if(value is Number) {
			return if (getProperty().typeClass == java.lang.Integer::class.java) {
				check(value.toInt()) as T
			} else if (getProperty().typeClass == java.lang.Double::class.java) {
				check(value.toDouble()) as T
			} else if (getProperty().typeClass == java.lang.Long::class.java) {
				check(value.toLong()) as T
			} else if (getProperty().typeClass == java.lang.Float::class.java) {
				check(value.toFloat()) as T
			} else {
				throw IllegalArgumentException("Can not cast $value to " + getProperty().typeClass)
			}
		}
		throw IllegalArgumentException("$value is not a number")
	}


	inline fun <reified T> getComparableValue(value: String): T {
		return if (T::class.java == java.lang.Integer::class.java) {
			value.toInt() as T
		} else if (T::class.java == java.lang.Double::class.java) {
			value.toDouble() as T
		} else if (T::class.java == java.lang.Long::class.java) {
			value.toLong() as T
		} else if (T::class.java == java.lang.Float::class.java) {
			value.toFloat() as T
		} else {
			throw IllegalArgumentException("Can not cast $value to " + T::class.java)
		}
	}
//
//	inline fun <reified T>  multiplyBy(value: String, checkValue: T): Boolean {
//		return if (T::class.java == java.lang.Integer::class.java) {
//			value.toInt() < checkValue as Int
//		} else if (T::class.java == java.lang.Double::class.java) {
//			value.toDouble() < checkValue as Double
//		} else if (T::class.java == java.lang.Long::class.java) {
//			value.toLong() < checkValue as Long
//		} else if (T::class.java == java.lang.Float::class.java) {
//			value.toFloat() < checkValue as Float
//		} else {
//			throw IllegalArgumentException("Can not cast $value to " + T::class.java)
//		}
//	}

	fun isLessThen(value: String, checkValue: Comparable<*>): Boolean {
		val paramType = filterType.propertyList[0].typeClass
		return if (paramType == java.lang.Integer::class.java) {
			value.toInt() < checkValue as Int
		} else if (paramType == java.lang.Double::class.java) {
			value.toDouble() < checkValue as Double
		} else if (paramType == java.lang.Long::class.java) {
			value.toLong() < checkValue as Long
		} else if (paramType == java.lang.Float::class.java) {
			value.toFloat() < checkValue as Float
		} else {
			throw IllegalArgumentException("Can not cast $value to $paramType")
		}
	}

	fun isMoreThen(value: String, checkValue: Comparable<*>): Boolean {
		val paramType = filterType.propertyList[0].typeClass
		return if (paramType == java.lang.Integer::class.java) {
			value.toInt() > checkValue as Int
		} else if (paramType == java.lang.Double::class.java) {
			value.toDouble() > checkValue as Double
		} else if (paramType == java.lang.Long::class.java) {
			value.toLong() > checkValue as Long
		} else if (paramType == java.lang.Float::class.java) {
			value.toFloat() > checkValue as Float
		} else {
			throw IllegalArgumentException("Can not cast $value to $paramType")
		}
	}
}