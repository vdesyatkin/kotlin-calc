internal data class CalcExpressionTreeNode(val token: CalcToken) {
    var parent: CalcExpressionTreeNode? = null
    var nextSibling: CalcExpressionTreeNode? = null
    var firstChild: CalcExpressionTreeNode? = null

    fun isSequence() : Boolean {
        return token.function?.isSequence == true
    }
    fun isBinaryComparison() : Boolean {
        return token.type == CalcTokenType.BinaryOperator && token.function?.binaryOperator?.isComparison == true
    }
}

internal class CalcExpressionTree(private val rootNode: CalcExpressionTreeNode,
                                  private val strings: Collection<String>) {

    fun evaluateSingle(input: CalcInput) : Double? {
        return createEvaluator(input).evaluateSingle()
    }

    fun evaluateBoolean(input: CalcInput) : Boolean? {
        return createEvaluator(input).evaluateBoolean()
    }

    fun evaluateSequence(input: CalcInput) : Sequence<Double> {
        return createEvaluator(input).evaluateSequence()
    }

    fun evaluateString(input: CalcInput) : String? {
        return createEvaluator(input).evaluateString()
    }

    fun evaluateStringSequence(input: CalcInput) : Sequence<String> {
        return createEvaluator(input).evaluateStringSequence()
    }

    private fun createEvaluator(input: CalcInput) : CalcEvaluator {
        val context = CalcEvaluatorContext(input, strings)
        return CalcEvaluator(rootNode, context)
    }
}