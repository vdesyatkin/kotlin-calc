class CalcCollectionObject(fields: Map<Double, Double>) : CalcCollection {

    private val _fields = CalcCollectionMap(fields)
    private val _methods = CalcCollectionMap()

    override fun get(key: Double): Double? {
        return _fields.get(key) ?: _methods.get(key)
    }

    override fun set(key: Double, value: Double?): Double? {
        return _fields.set(key, value)
    }

    override fun keys(): Sequence<Double> {
        return _fields.keys()
    }

    override fun values(): Sequence<Double> {
        return _fields.values()
    }

    override fun size(): Int? {
        return _fields.size()
    }

    override fun find(value: Double, beginKey: Double?): Double? {
        return _fields.find(value, beginKey)
    }

    override fun concat(collections: List<CalcCollection>): CalcCollection? {
        return this
    }

    override fun slice(beginKey: Double?, endKey: Double?) : CalcCollection? {
        return this
    }

    fun setMethod(variableId: Double, functionRef: Double?) : Double? {
        return _methods.set(variableId, functionRef)
    }
}