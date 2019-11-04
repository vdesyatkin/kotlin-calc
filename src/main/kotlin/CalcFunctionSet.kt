class CalcFunctionSet {
    private val _functionSearchTree = CalcFunctionTrie()

    internal fun getFunctionSearch() : CalcFunctionSearchProvider {
        return _functionSearchTree
    }

    companion object {
        fun empty(): CalcFunctionSet = CalcFunctionSet()
        fun default(): CalcFunctionSet = CalcDefaultFunctions.defaultFunctionSet()
    }

    fun addParameterlessFunction(name: String, function : CalcFunctionDelegate,
                                 unaryAlias: String? = null, returnsRef: Boolean = false) : Boolean {
        if (name.isEmpty()) return false

        _functionSearchTree.add(CalcFunction(
            name = name,
            alias = unaryAlias,
            binaryOperator = null,
            function = function,
            sequenceFunction = null,
            minOperandCount = 0,
            maxOperandCount = 0,
            returnsRef = returnsRef))
        return true
    }

    fun addFunction(name: String, function: CalcFunctionDelegate,
                    minOperandCount: Int, maxOperandCount: Int?,
                    unaryAlias: String? = null, returnsRef: Boolean = false) : Boolean {

        if (name.isEmpty()) return false
        if (minOperandCount < 0)  return false
        if (maxOperandCount != null && (minOperandCount > maxOperandCount)) return false


        _functionSearchTree.add(CalcFunction(
                name = name,
                alias = unaryAlias,
                binaryOperator = null,
                function = function,
                sequenceFunction = null,
                minOperandCount = minOperandCount,
                maxOperandCount = maxOperandCount,
                returnsRef = returnsRef))
        return true
    }

    fun addOperatorFunction(name: String, function: CalcFunctionDelegate,
                            binaryName: String?, associativity: CalcAssociativity, precedence: Int,
                            minOperandCount: Int, maxOperandCount: Int?, isCommutative: Boolean = false,
                            unaryAlias: String? = null, unaryPostAlias: String? = null, binaryAlias: String? = null,
                            returnsRef: Boolean = false) : Boolean {

        if (name.isEmpty()) return false
        if (minOperandCount <= 0)  return false
        if (maxOperandCount != null && (minOperandCount > maxOperandCount)) return false

        _functionSearchTree.add(CalcFunction(
                name = name,
                alias = unaryAlias,
                binaryOperator = CalcBinaryOperator(
                        name = binaryName,
                        alias = binaryAlias,
                        unaryPostAlias = unaryPostAlias,
                        associativity = associativity,
                        precedence = precedence,
                        isComparison = false,
                        isCommutative = isCommutative
                ),
                function = function,
                sequenceFunction = null,
                minOperandCount = minOperandCount,
                maxOperandCount = maxOperandCount,
                returnsRef = returnsRef))
        return true
    }

    fun addComparisonOperatorFunction(name: String, function: CalcFunctionDelegate,
                                      binaryName: String, associativity: CalcAssociativity, precedence: Int,
                                      minOperandCount: Int, maxOperandCount: Int?, isCommutative: Boolean = false,
                                      unaryAlias: String? = null, unaryPostAlias: String? = null, binaryAlias: String? = null) : Boolean {

        if (name.isEmpty()) return false
        if (minOperandCount <= 0)  return false
        if (maxOperandCount != null && (minOperandCount > maxOperandCount)) return false

        _functionSearchTree.add(CalcFunction(
                name = name,
                alias = unaryAlias,
                binaryOperator = CalcBinaryOperator(
                        name = binaryName,
                        alias = binaryAlias,
                        unaryPostAlias = unaryPostAlias,
                        associativity = associativity,
                        precedence = precedence,
                        isComparison = true,
                        isCommutative = isCommutative
                ),
                function = function,
                sequenceFunction = null,
                minOperandCount = minOperandCount,
                maxOperandCount = maxOperandCount,
                returnsRef = false))
        return true
    }

    fun addParameterlessSequenceFunction(name: String, function : CalcFunctionSequenceDelegate,
                                         unaryAlias: String? = null, returnsRef: Boolean = false) : Boolean {

        if (name.isEmpty()) return false

        _functionSearchTree.add(CalcFunction(
                name = name,
                alias = unaryAlias,
                binaryOperator = null,
                function = null,
                sequenceFunction = function,
                minOperandCount = 0,
                maxOperandCount = 0,
                returnsRef = returnsRef))
        return true
    }

    fun addSequenceFunction(name: String, function: CalcFunctionSequenceDelegate,
                            minOperandCount: Int, maxOperandCount: Int?,
                            unaryAlias: String? = null) : Boolean {

        if (name.isEmpty()) return false
        if (minOperandCount < 0)  return false
        if (maxOperandCount != null && (minOperandCount > maxOperandCount)) return false

        _functionSearchTree.add(CalcFunction(
                name = name,
                alias = unaryAlias,
                binaryOperator = null,
                function = null,
                sequenceFunction = function,
                minOperandCount = minOperandCount,
                maxOperandCount = maxOperandCount,
                returnsRef = false))
        return true
    }

    fun addSequenceOperatorFunction(name: String, function: CalcFunctionSequenceDelegate,
                                    binaryName: String?, associativity: CalcAssociativity, precedence: Int,
                                    minOperandCount: Int, maxOperandCount: Int?, isCommutative: Boolean = false,
                                    unaryAlias: String? = null, unaryPostAlias: String? = null, binaryAlias: String? = null) : Boolean {

        if (name.isEmpty()) return false
        if (minOperandCount <= 0)  return false
        if (maxOperandCount != null && (minOperandCount > maxOperandCount)) return false

        _functionSearchTree.add(CalcFunction(
                name = name,
                alias = unaryAlias,
                binaryOperator = CalcBinaryOperator(
                        name = binaryName,
                        alias = binaryAlias,
                        unaryPostAlias = unaryPostAlias,
                        associativity = associativity,
                        precedence = precedence,
                        isComparison = false,
                        isCommutative = isCommutative
                ),
                function = null,
                sequenceFunction = function,
                minOperandCount = minOperandCount,
                maxOperandCount = maxOperandCount,
                returnsRef = false))
        return true
    }
}