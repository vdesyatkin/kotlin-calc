import java.util.*

class CalcCollectionArray : CalcCollection {
    private val _array: DoubleArray
    private val _defaultValue: Double

    constructor(size: Int, defaultValue: Double = 0.0) {
        _array = DoubleArray(size)
        _array.fill(defaultValue)
        _defaultValue = defaultValue
    }

    constructor(array: DoubleArray, defaultValue: Double = 0.0) {
        _array = array
        _defaultValue = defaultValue
    }

    override fun get(key: Double): Double? {
        val index = getIndex(key) ?: return null
        if (index < 0 || index >= _array.size) {
            return null
        }
        return _array[index]
    }

    override fun set(key: Double, value: Double?) : Double? {
        val index = getIndex(key) ?: return null
        if (index < 0 || index >= _array.size) {
            return null
        }

        _array[index] = value ?: _defaultValue
        return _array[index]
    }

    override fun keys(): Sequence<Double> {
        return (0.._array.size).map{ it.toDouble() }.asSequence()
    }

    override fun values(): Sequence<Double> {
        return _array.asSequence()
    }

    override fun size(): Int? {
        return _array.size
    }

    override fun find(value: Double, beginKey: Double?): Double? {
        val beginIndex = getIndex(beginKey) ?: 0
        for (index in beginIndex until _array.size) {
            if (value == _array[index]) {
                return index.toDouble()
            }
        }
        return null
    }

    override fun concat(collections: List<CalcCollection>): CalcCollection? {
        val newSize = _array.size + collections.sumBy { it.size() ?: 0 }
        val newList = ArrayList<Double>(newSize)
        newList.addAll(values())
        collections.forEach { newList.addAll(it.values())}
        return CalcCollectionArray(newList.toDoubleArray(), _defaultValue)
    }

    override fun slice(beginKey: Double?, endKey: Double?) : CalcCollection? {
        var beginIndex = getIndex(beginKey) ?: 0
        var endIndex = getIndex(endKey) ?: _array.size

        if (beginIndex < 0) {
            beginIndex = 0
        }
        if (endIndex > _array.size) {
            endIndex = _array.size
        }
        if (beginIndex  >= endIndex) {
            return CalcCollectionArray(DoubleArray(0), _defaultValue)
        }

        val newArray = _array.sliceArray(beginIndex until endIndex)
        return CalcCollectionArray(newArray, _defaultValue)
    }

    private fun getIndex(key: Double?) : Int? {
        return CalcNumberHelper.getInt(key)
    }
}