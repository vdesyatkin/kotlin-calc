class CalcExpression internal constructor(tree: CalcExpressionTree) {

    private val _tree : CalcExpressionTree = tree

    fun evaluate(input: CalcInput) : Double? {
        return  _tree.evaluateSingle(input)
    }

    fun evaluateBoolean(input: CalcInput) : Boolean? {
        return  _tree.evaluateBoolean(input)
    }

    fun evaluateSequence(input: CalcInput) : Sequence<Double> {
        return  _tree.evaluateSequence(input)
    }

    fun evaluateString(input: CalcInput) : String? {
        return  _tree.evaluateString(input)
    }

    fun evaluateStringSequence(input: CalcInput) : Sequence<String> {
        return  _tree.evaluateStringSequence(input)
    }
}