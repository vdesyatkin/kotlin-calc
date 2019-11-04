open class CalcCollectionList(list: List<Double>, defaultValue: Double = 0.0) : CalcCollection {

    private val _list: ArrayList<Double> = ArrayList(list)
    private val _defaultValue: Double = defaultValue

    override fun get(key: Double): Double? {
        val index = getIndex(key) ?: return null
        if (index < 0 || index >= _list.size) {
            return null
        }
        return _list[index]
    }

    override fun set(key: Double, value: Double?) : Double?{
        val index = getIndex(key) ?: return null
        if (index < 0 || index >= _list.size) {
            return null
        }
        _list[index] = value ?: _defaultValue
        return _list[index]
    }

    override fun keys(): Sequence<Double> {
        return (0.._list.size).map{ it.toDouble() }.asSequence()
    }

    override fun values(): Sequence<Double> {
        return _list.asSequence()
    }

    override fun size(): Int? {
        return _list.size
    }

    override fun find(value: Double, beginKey: Double?): Double? {
        val beginIndex = getIndex(beginKey) ?: 0
        for (index in beginIndex until _list.size) {
            if (value == _list[index]) {
                return index.toDouble()
            }
        }
        return null
    }

    override fun concat(collections: List<CalcCollection>): CalcCollection? {
        val newSize = _list.size + collections.sumBy { it.size() ?: 0 }
        val newList = java.util.ArrayList<Double>(newSize)
        newList.addAll(values())
        collections.forEach { newList.addAll(it.values())}
        return CalcCollectionList(newList, _defaultValue)
    }

    override fun slice(beginKey: Double?, endKey: Double?) : CalcCollection? {
        var beginIndex = getIndex(beginKey) ?: 0
        var endIndex = getIndex(endKey) ?: _list.size

        if (beginIndex < 0) {
            beginIndex = 0
        }
        if (endIndex > _list.size) {
            endIndex = _list.size
        }
        if (beginIndex  >= endIndex) {
            return CalcCollectionList(emptyList(), _defaultValue)
        }

        val newArray = _list.slice(beginIndex until endIndex)
        return CalcCollectionList(newArray, _defaultValue)
    }

    private fun getIndex(key: Double?) : Int? {
        return CalcNumberHelper.getInt(key)
    }

    fun add(value: Double) {
        _list.add(value)
    }
}