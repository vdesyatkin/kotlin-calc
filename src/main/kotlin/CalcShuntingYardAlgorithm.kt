import java.util.*

internal class CalcShuntingYardAlgorithm(functions: CalcFunctionSet? = null) {
    private val _numberParser = CalcNumberParser()
    private val _stringParser = CalcStringParser()
    private val _functions = functions ?: CalcFunctionSet.default()

    internal fun parseTokens(context: CalcContext,
                             tokens: ArrayList<CalcToken>,
                             strings: HashSet<String>) : CalcErrorInfo? {

        val operatorStack = ArrayDeque<CalcToken>()
        val text = context.text

        var contextArity = CalcContextArity.Unary

        while (context.offset < text.length) {
            val character = text[context.offset]

            if (CalcCharHelper.isEmptyObject(character)) {
                context.offset++
                continue
            }

            val tokenOffset = context.offset

            if (CalcCharHelper.isNumberChar(character) != null) {

                val number = _numberParser.parseNumber(context) ?: return error(CalcErrorCode.InvalidNumber, tokenOffset)
                tokens.add(CalcToken(CalcTokenType.Constant, tokenOffset, number = number ))
                contextArity = CalcContextArity.Binary

                continue
            }

            if (character == CalcCharHelper.StringChar) {

                context.offset++
                val string = _stringParser.parseString(context) ?: return error(CalcErrorCode.InvalidString, tokenOffset)
                strings.add(string)

                tokens.add(CalcToken(CalcTokenType.String, tokenOffset, text = string))
                contextArity = CalcContextArity.Binary
                continue
            }

            if (character == CalcCharHelper.VariableChar) {

                context.offset++
                val variableNameBuilder = StringBuilder()
                while (context.offset < text.length) {
                    val letterChar = CalcCharHelper.isLetterChar(text[context.offset])
                            ?: CalcCharHelper.isNumberChar(text[context.offset])
                            ?: break
                    variableNameBuilder.append(letterChar)
                    context.offset++
                }
                if (variableNameBuilder.isEmpty()) return error(CalcErrorCode.InvalidVariable, tokenOffset)
                val variableName = variableNameBuilder.toString()
                tokens.add(CalcToken(CalcTokenType.Variable, tokenOffset, text = variableName))
                contextArity = CalcContextArity.Binary
                continue
            }

            if (character == CalcCharHelper.CommaChar) {
                if (contextArity == CalcContextArity.Unary) return error(CalcErrorCode.InconsistentComma, tokenOffset)

                var leftBracketIsFound = false
                while (!operatorStack.isEmpty()) {
                    val prevToken = operatorStack.removeFirst()

                    if (prevToken.type == CalcTokenType.LeftBracket) {
                        leftBracketIsFound = true
                        operatorStack.addFirst(prevToken)
                        break
                    }

                    tokens.add(prevToken)
                }

                if (!leftBracketIsFound) {
                    return error(CalcErrorCode.InconsistentComma, tokenOffset)
                }

                operatorStack.addFirst(CalcToken(CalcTokenType.Comma, tokenOffset))

                contextArity = CalcContextArity.Unary
                context.offset++
                continue
            }

            val leftBracket = CalcCharHelper.checkLeftBracket(character)
            if (leftBracket != null) {
                context.offset++
                @Suppress("LiftReturnOrAssignment")
                if (skipEmptyBrackets(context, leftBracket)) {
                    operatorStack.addFirst(CalcToken(CalcTokenType.Empty, tokenOffset))
                    contextArity = CalcContextArity.Binary
                } else {
                    operatorStack.addFirst(CalcToken(CalcTokenType.LeftBracket, tokenOffset, bracket = leftBracket))
                    contextArity = CalcContextArity.Unary
                }

                continue
            }

            val rightBracket = CalcCharHelper.checkRightBracket(character)
            if (rightBracket != null) {
                if (contextArity == CalcContextArity.Unary &&
                        (operatorStack.isEmpty() || operatorStack.peekFirst().type != CalcTokenType.LeftBracket)) {
                    return error(CalcErrorCode.InconsistentBracket, tokenOffset)
                }

                var leftBracketIsFound = false
                while (!operatorStack.isEmpty()) {
                    val prevToken = operatorStack.removeFirst()
                    if (prevToken.type == CalcTokenType.LeftBracket) {
                        if (rightBracket != prevToken.bracket) {
                            return error(CalcErrorCode.InconsistentBracket, tokenOffset)
                        }
                        leftBracketIsFound = true
                        break
                    }

                    tokens.add(prevToken)
                }
                if (!leftBracketIsFound) {
                    return error(CalcErrorCode.InconsistentBracket, tokenOffset)
                }

                if (!operatorStack.isEmpty()) {
                    val functionToken = operatorStack.peekFirst()
                    if (functionToken?.type == CalcTokenType.Function) {
                        operatorStack.removeFirst()
                        tokens.add(functionToken)
                    }
                }

                contextArity = CalcContextArity.Binary
                context.offset++
                continue
            }

            val functionInfo = _functions.getFunctionSearch().find(context, contextArity) ?: return error(CalcErrorCode.InvalidToken, tokenOffset)
            val function = functionInfo.function
            val functionArity = functionInfo.functionArity
            val tokenType = if (functionArity == CalcFunctionArity.Binary) CalcTokenType.BinaryOperator else CalcTokenType.Function
            val token = CalcToken(tokenType, tokenOffset, function = function, text = functionInfo.text)
            val binaryOperator = if (functionArity != CalcFunctionArity.UnaryPre) function.binaryOperator else null

            if (functionArity == CalcFunctionArity.Binary && contextArity == CalcContextArity.Unary) {
                return error(CalcErrorCode.InvalidToken, tokenOffset)
            }

            if (function.maxOperandCount == 0) {
                skipEmptyBrackets(context)
                tokens.add(token)
                contextArity = CalcContextArity.Binary
                continue
            }

            while (binaryOperator != null && !operatorStack.isEmpty()) {
                var prevToken = operatorStack.peekFirst()
                if (prevToken.type == CalcTokenType.LeftBracket || prevToken.type == CalcTokenType.Comma) break

                val prevOperator = prevToken.function?.binaryOperator
                if (prevToken.type == CalcTokenType.BinaryOperator && prevOperator != null) {

                    if (prevOperator.precedence < binaryOperator.precedence ||
                        (prevOperator.precedence == binaryOperator.precedence &&
                                prevOperator.associativity == CalcAssociativity.Right)) {
                        break
                    }
                }

                prevToken = operatorStack.removeFirst()
                tokens.add(prevToken)
            }

            if (functionArity == CalcFunctionArity.UnaryPost)  {
                skipEmptyBrackets(context)
                tokens.add(token)
                contextArity = CalcContextArity.Binary
                continue
            }

            if (function.minOperandCount == 0 && !nextIsLeftBracket(context)) {
                tokens.add(CalcToken(CalcTokenType.Empty, tokenOffset))
                tokens.add(token)
                contextArity = CalcContextArity.Binary
                continue
            }

            operatorStack.addFirst(token)
            contextArity = CalcContextArity.Unary
        }

        while (!operatorStack.isEmpty()) {
            val token = operatorStack.removeFirst()

            if (token.type == CalcTokenType.LeftBracket) error(CalcErrorCode.InconsistentBracket, token.offset)
            tokens.add(token)
        }

        return null
    }

    private fun error(errorCode: CalcErrorCode, offset: Int) : CalcErrorInfo {
        return CalcErrorInfo(errorCode, offset)
    }

    private fun nextIsLeftBracket(context: CalcContext) : Boolean {
        var localOffset = context.offset
        while (localOffset < context.text.length) {
            val character = context.text[localOffset]

            if (CalcCharHelper.isEmptyObject(character)) {
                localOffset++
                continue
            }

            return CalcCharHelper.checkLeftBracket(character) != null
        }

        return false
    }

    private fun skipEmptyBrackets(context: CalcContext, currentBracket: CalcBracket? = null) : Boolean {
        var localOffset = context.offset
        var bracket: CalcBracket? = currentBracket
        while (localOffset < context.text.length) {
            val character = context.text[localOffset]

            if (CalcCharHelper.isEmptyObject(character)) {
                localOffset++
                continue
            }

            val leftBracket = CalcCharHelper.checkLeftBracket(character)
            if (leftBracket != null) {
                if (bracket == null) {
                    bracket = leftBracket
                    localOffset++
                    continue
                } else {
                    return false
                }
            }

            val rightBracket = CalcCharHelper.checkRightBracket(character)
            if (rightBracket != null) {
                return if (rightBracket == bracket) {
                    localOffset++
                    context.offset = localOffset
                    true
                } else {
                    false
                }
            }

            return false
        }

        return false
    }
}