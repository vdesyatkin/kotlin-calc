import java.util.ArrayDeque

class CalcParseResult {

    val expression: CalcExpression?
    val error: CalcErrorInfo?

    constructor(expression: CalcExpression) {
        this.expression = expression
        this.error = null
    }

    constructor(error: CalcErrorInfo) {
        this.expression = null
        this.error = error
    }
}

data class CalcErrorInfo(val errorCode: CalcErrorCode, val offset: Int)

enum class CalcErrorCode {
    EmptyString,
    InvalidExpression,
    InvalidToken,
    InvalidNumber,
    InvalidString,
    InvalidVariable,
    InvalidOperandCount,
    InconsistentBracket,
    InconsistentComma
}

internal data class CalcContext(val text: String, var offset: Int)

class CalcParser(functions: CalcFunctionSet? = null) {

    private val _shuntingYardAlgorithm = CalcShuntingYardAlgorithm(functions)

    fun parse(text: String) : CalcParseResult {
        if (text.isEmpty()) return error(CalcErrorCode.EmptyString, 0)

        val context = CalcContext(text, 0)
        val tokenQueue = ArrayList<CalcToken>()
        val strings = HashSet<String>()

        val shuntingYardError = _shuntingYardAlgorithm.parseTokens(context, tokenQueue, strings)
        if (shuntingYardError != null) return error(shuntingYardError)

        val treeStack = ArrayDeque<CalcExpressionTreeNode>()

        for (token in tokenQueue) {

            if (token.type == CalcTokenType.Constant || token.type == CalcTokenType.Comma ||
                token.type == CalcTokenType.Variable || token.type == CalcTokenType.String ||
                token.type == CalcTokenType.Empty) {

                treeStack.addFirst(CalcExpressionTreeNode(token))
                continue
            }

            if (token.type == CalcTokenType.Function || token.type == CalcTokenType.BinaryOperator) {
                val function = token.function ?: continue
                val functionType = token.type
                val isBinaryOperator = functionType == CalcTokenType.BinaryOperator

                var operandExpectedCount = if (isBinaryOperator) 2 else 1

                val functionNode = CalcExpressionTreeNode(token)
                if (function.maxOperandCount == 0) {
                    treeStack.addFirst(functionNode)
                    continue
                }

                var operandCount = 0
                var prevOperand : CalcExpressionTreeNode? = null
                while (treeStack.isNotEmpty() && operandCount < operandExpectedCount)  {
                    val operand = treeStack.removeFirst() ?: continue

                    // it's available for binary operators too
                    if (operand.token.type == CalcTokenType.Comma) {
                        operandExpectedCount++
                        continue
                    }

                    operandCount++

                    if (operand.token.type == CalcTokenType.Empty) {
                        continue //todo check?
                    }

                    if (isBinaryCompaction(functionNode, operand) && operand.firstChild != null) {
                        val firstChild = operand.firstChild
                        firstChild?.parent = functionNode

                        var lastChild = firstChild
                        while (lastChild?.nextSibling != null) {
                            lastChild = lastChild.nextSibling
                            lastChild?.parent = functionNode
                        }
                        lastChild?.nextSibling = prevOperand
                        prevOperand = firstChild
                        functionNode.firstChild = firstChild
                        operand.parent = null
                        operand.nextSibling = null
                        operand.firstChild = null

                    } else {
                        operand.parent = functionNode
                        operand.nextSibling = prevOperand
                        functionNode.firstChild = operand
                        prevOperand = operand
                    }
                }

                if (operandCount < function.minOperandCount ||
                        (function.maxOperandCount != null && operandCount > function.maxOperandCount)) {
                    return error(CalcErrorCode.InvalidOperandCount, token.offset)
                }

                treeStack.addFirst(functionNode)
            }
        }

        if (treeStack.count() != 1) return error(CalcErrorCode.InvalidExpression, 0)
        val rootNode = treeStack.removeFirst() ?: return error(CalcErrorCode.InvalidExpression, 0)
        val expressionTree = CalcExpressionTree(rootNode, strings)

        val expression = CalcExpression(expressionTree)
        return CalcParseResult(expression)
    }

    private fun isBinaryCompaction(parent : CalcExpressionTreeNode, child : CalcExpressionTreeNode) : Boolean {
        return parent.token.function?.binaryOperator != null &&
                                    parent.token.function.maxOperandCount == null &&
                                    parent.token.function.binaryOperator.isCommutative &&
                                    child.token.function != null &&
                                    child.token.function.binaryOperator == parent.token.function.binaryOperator
    }

    private fun error(errorCode: CalcErrorCode, offset: Int) : CalcParseResult {
        return CalcParseResult(CalcErrorInfo(errorCode, offset))
    }

    private fun error(errorInfo: CalcErrorInfo) : CalcParseResult {
        return CalcParseResult(errorInfo)
    }
}