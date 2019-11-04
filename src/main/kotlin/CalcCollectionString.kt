import java.lang.StringBuilder

class CalcCollectionString(string: String) : CalcCollection {

    private val _string: String = string

    fun asString() : String {
        return _string
    }

    override fun get(key: Double): Double? {
        val index = getIndex(key) ?: return null
        if (index < 0 || index >= _string.length) {
            return null
        }
        return _string[index].toDouble()
    }

    override fun set(key: Double, value: Double?) : Double?{
        return null
    }

    override fun keys(): Sequence<Double> {
        return (0.._string.length).map{ it.toDouble() }.asSequence()
    }

    override fun values(): Sequence<Double> {
        return _string.toCharArray().map{ it.toDouble() }.asSequence()
    }

    override fun size(): Int? {
        return _string.length
    }

    override fun find(value: Double, beginKey: Double?): Double? {
        val char = CalcNumberHelper.getValidDoubleOrNull(value)?.toChar() ?: return null
        val beginIndex = getIndex(beginKey) ?: 0
        val foundIndex = _string.indexOf(char, beginIndex)
        return if (foundIndex >= 0) foundIndex.toDouble() else null
    }

    override fun concat(collections: List<CalcCollection>): CalcCollection? {
        val builder = StringBuilder(_string)
        for (collection in collections) {
            if (collection is CalcCollectionString) {
                builder.append(collection.asString())
            }
        }
        return CalcCollectionString(builder.toString())
    }

    override fun slice(beginKey: Double?, endKey: Double?) : CalcCollection? {
        var beginIndex = getIndex(beginKey) ?: 0
        var endIndex = getIndex(endKey) ?: _string.length

        if (beginIndex < 0) {
            beginIndex = 0
        }
        if (endIndex > _string.length) {
            endIndex = _string.length
        }
        if (beginIndex  >= endIndex) {
            return CalcCollectionString("")
        }

        val newString = _string.substring(beginIndex, endIndex)
        return CalcCollectionString(newString)
    }

    private fun getIndex(key: Double?) : Int? {
        return CalcNumberHelper.getInt(key)
    }

    override fun equalsCollection(collection: CalcCollection) : Boolean {
        if (collection !is CalcCollectionString) {
            return false
        }

        return _string == collection.asString()
    }
}