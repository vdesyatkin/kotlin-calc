class CalcCollectionProxy(val collection: () -> CalcCollection) : CalcCollection {

    override fun get(key: Double): Double? {
        return collection().get(key)
    }

    override fun set(key: Double, value: Double?) : Double?{
        return collection().set(key, value)
    }

    override fun keys(): Sequence<Double> {
        return collection().keys()
    }

    override fun values(): Sequence<Double> {
        return collection().values()
    }

    override fun size(): Int? {
        return collection().size()
    }

    override fun find(value: Double, beginKey: Double?): Double? {
        return collection().find(value, beginKey)
    }

    override fun concat(collections: List<CalcCollection>): CalcCollection? {
        return this
    }

    override fun slice(beginKey: Double?, endKey: Double?): CalcCollection? {
        return this
    }

    override fun equalsCollection(collection: CalcCollection): Boolean {
        return collection().equalsCollection(collection)
    }
}