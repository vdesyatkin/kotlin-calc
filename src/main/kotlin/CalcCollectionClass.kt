class CalcCollectionClass(fields: List<Double>) : CalcCollection {

    private var _constructor: Double? = null
    private val _fields = ArrayList(fields)
    private val _methods = CalcCollectionMap()

    fun getFields() : List<Double> {
        return _fields
    }

    fun getMethods(): Map<Double, Double> {
        return _methods.asMap()
    }

    fun setMethod(variableId: Double, functionRef: Double): Double? {
        return _methods.set(variableId, functionRef)
    }

    fun getConstructor(): Double? {
        return _constructor
    }

    fun setConstructor(functionRef: Double?) {
        _constructor = functionRef
    }

    override fun get(key: Double): Double? {
        return _methods.get(key)
    }

    override fun set(key: Double, value: Double?): Double? {
        return null // methods can be added only by setMethod
    }

    override fun keys(): Sequence<Double> {
        return emptySequence()
    }

    override fun values(): Sequence<Double> {
        return emptySequence()
    }

    override fun size(): Int? {
        return null
    }

    override fun find(value: Double, beginKey: Double?): Double? {
        return null
    }

    override fun concat(collections: List<CalcCollection>): CalcCollection? {
        return this
    }

    override fun slice(beginKey: Double?, endKey: Double?) : CalcCollection? {
        return this
    }

    override fun equalsCollection(collection: CalcCollection) : Boolean {
        return this === collection
    }
}