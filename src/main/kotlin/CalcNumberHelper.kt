import kotlin.math.roundToInt

object CalcNumberHelper {

    const val TRUE = 1.0
    const val FALSE = 0.0
    val NULL = Double.NaN

    fun isTrue(value: Double) : Boolean = value >= 0.5
    fun isTrue(value: Double?) : Boolean = value != null && value >= 0.5
    fun isFalse(value: Double) : Boolean = value < 0.5
    fun isNull(value: Double?) : Boolean = value == null || value.isNaN() || value.isInfinite()
    fun isNotNull(value: Double?) : Boolean = !isNull(value)

    fun isValidDouble(value: Double?) : Boolean {
        return value != null && !value.isNaN() && value.isFinite()
    }

    fun getValidDoubleOrNull(value: Double?) : Double? {
        return if (isValidDouble(value)) value else null
    }

    fun getInt(value: Double?) : Int? {
        return getValidDoubleOrNull(value)?.roundToInt()
    }
}