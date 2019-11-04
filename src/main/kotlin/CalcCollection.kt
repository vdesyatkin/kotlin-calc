interface CalcCollection {
    fun get(key: Double) : Double?
    fun set(key: Double, value: Double?) : Double?
    fun keys() : Sequence<Double>
    fun values() : Sequence<Double>
    fun size(): Int?

    fun find(value: Double, beginKey: Double?) : Double?
    fun concat(collections: List<CalcCollection>) : CalcCollection?
    fun slice(beginKey: Double?, endKey: Double?) : CalcCollection?

    fun equalsCollection(collection: CalcCollection) : Boolean {
        if (this === collection) return true
        return this.size() == collection.size() &&
                this.keys().toList() == collection.keys().toList() &&
                this.values().toList() == collection.values().toList()
    }
}