class CalcCollectionMap(map: Map<Double, Double> = emptyMap()) : CalcCollection {

    private val _map: HashMap<Double, Double> = HashMap(map)

    fun asMap() : Map<Double, Double> {
        return _map
    }

    override fun get(key: Double): Double? {
        return _map[key]
    }

    override fun set(key: Double, value: Double?) : Double?{
        val mapKey = getMapKey(key) ?: return null
        return if (value != null) {
            _map[mapKey] = value
            value
        } else {
            _map.remove(mapKey)
            null
        }
    }

    override fun keys(): Sequence<Double> {
        return _map.keys.asSequence()
    }

    override fun values(): Sequence<Double> {
        return _map.values.asSequence()
    }

    override fun size(): Int? {
        return _map.size
    }

    override fun find(value: Double, beginKey: Double?): Double? {
        val beginMapKey = getMapKey(beginKey)
        if (beginMapKey != null) {
            var foundBeginKey = false
            for (entity in _map.entries) {
                if (!foundBeginKey && entity.key == beginMapKey) {
                    foundBeginKey = true
                }
                if (foundBeginKey && entity.value == value) {
                    return entity.key
                }
            }
        }

        return _map.entries.firstOrNull {it.value == value}?.key
    }

    override fun concat(collections: List<CalcCollection>): CalcCollection? {
        val newMap = HashMap(_map)
        for (colleciton in collections) {
            for (key in colleciton.keys()) {
                val value = colleciton.get(key)
                if (value != null) {
                    newMap.put(key, value)
                }
            }
        }
        return CalcCollectionMap(newMap)
    }

    override fun slice(beginKey: Double?, endKey: Double?) : CalcCollection? {
        val beginMapKey = getMapKey(beginKey)
        val endMapKey = getMapKey(endKey)

        if (beginMapKey == null && endMapKey == null) {
            return CalcCollectionMap(HashMap(_map))
        }

        val newMap = HashMap<Double, Double>()

        var foundBeginKey = beginMapKey == null
        for (entity in _map.entries) {
            if (!foundBeginKey && entity.key == beginMapKey) {
                foundBeginKey = true
            }
            if (foundBeginKey) {
                if (entity.key == endMapKey) {
                    break;
                }
                newMap[entity.key] = entity.value
            }
        }
        return CalcCollectionMap(newMap)
    }

    private fun getMapKey(key: Double?) : Double? {
       return CalcNumberHelper.getValidDoubleOrNull(key)
    }
}