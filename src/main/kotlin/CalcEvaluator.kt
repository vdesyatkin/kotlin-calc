import CalcNumberHelper.NULL
import CalcNumberHelper.getValidDoubleOrNull
import CalcNumberHelper.isValidDouble
import java.io.BufferedReader
import java.io.BufferedWriter

internal class CalcEvaluator(rootNode: CalcExpressionTreeNode?, context: CalcEvaluatorContext)
      : CalcFunctionContext,
        CalcFunctionOperandSequence,
        CalcFunctionOperandIterator,
        CalcFunctionFuncsContext {

    private val _context: CalcEvaluatorContext = context
    private val _rootNode = rootNode
    private var _currentTreeNode : CalcExpressionTreeNode? = rootNode
    private var _currentValueIterator: CalcExpressionSequenceIterator? = null

    fun evaluateSingle() : Double? {
        reset()
        return try {
            if (hasNext()) getValidDoubleOrNull(next()) else null
        } catch (result: CalcExpressionReturnCommand) {
            getValidDoubleOrNull(result.returnValues.firstOrNull())
        } finally {
            _context.flush()
        }
    }

    fun evaluateBoolean() : Boolean? {
        val singleResult = evaluateSingle()
        return CalcNumberHelper.isTrue(singleResult)
    }

    fun evaluateString() : String? {
        val singleResult = evaluateSingle() ?: return null
        val collection = _context.getCollectionByRef(singleResult) ?: return null
        return if (collection is CalcCollectionString) {
            collection.asString()
        } else {
            null
        }
    }

    fun evaluateSequence() : Sequence<Double> {
        reset()
        return sequence {
            try {
                while (hasNext()) {
                    val nextValue = next()
                    if (isValidDouble(nextValue)) {
                        yield(nextValue)
                    }
                }
                _context.flush()
            } catch (command: CalcExpressionReturnCommand) {
                _context.flush()
                yieldAll(command.returnValues.filter { isValidDouble(it) })
            }
        }
    }

    fun evaluateStringSequence() : Sequence<String> {
        reset()
        return sequence {
            try {
                while (hasNext()) {
                    val nextString = _context.getStringByRef(next()) ?: continue
                    yield(nextString)
                }
                _context.flush()
            } catch (command: CalcExpressionReturnCommand) {
                _context.flush()
                for (value in command.returnValues) {
                    val string = _context.getStringByRef(value)
                    if (string != null) {
                        @Suppress("USELESS_ELVIS")
                        yield(string ?: "")
                    }
                }
            }
        }
    }

    private fun reset() {
        _currentTreeNode = _rootNode
        _currentValueIterator = null
    }

    override fun operands(skipLast: Boolean): CalcFunctionOperandSequence {
        return if (skipLast) {
            CalcEvaluatorSkipLastWrapper(this)
        } else {
            this
        }
    }

    // CalcFunctionContext
    override fun state(): Any? = _context.getState()
    override fun inputReader(): BufferedReader? = _context.getInputReader()
    override fun outputWriter(): BufferedWriter? = _context.getOutputWriter()
    override fun iterator(): CalcFunctionOperandIterator = this
    override fun data(): CalcFunctionDataContext = _context
    override fun comparison(): CalcFunctionComparisonContext = _context
    override fun functions(): CalcFunctionFuncsContext = this
    override fun classes(): CalcFunctionClassesContext = _context

    override fun returnFunction(values: List<Double>) {
        throw CalcExpressionReturnCommand(values)
    }

    override fun returnFunctionWithBreakLoop(values: List<Double>) {
        throw CalcExpressionBreakCommand(values)
    }

    override fun returnFunctionWithContinueLoop(values: List<Double>) {
        throw CalcExpressionContinueCommand(values)
    }

    override fun firstOperand() : Double? {
        val firstNode = _currentTreeNode?.parent?.firstChild ?: return null
        if (firstNode.isSequence()) {
            val iterator = evaluateSequenceNode(firstNode)
            return if (iterator.hasNext()) iterator.next() else null
        }
        return evaluateNode(firstNode, false)
    }

    override fun lastOperand() : Double? {
        return lastOperand(excludeLastDirect = false)
    }

    fun lastOperand(excludeLastDirect: Boolean) : Double? {
        return evaluateNode(getLastNode(excludeLastDirect), false)
    }

    override fun hasNext() : Boolean {
        return hasNext(direct = false, excludeLastDirect = false)
    }

    override fun hasNextDirect() : Boolean {
        return hasNext(direct = true, excludeLastDirect = false)
    }

    fun hasNext(direct: Boolean, excludeLastDirect: Boolean) : Boolean {
        var currentTreeNode = _currentTreeNode ?: return false
        var currentValueIterator = _currentValueIterator

        if (direct) {
            return if (currentValueIterator != null) {
                if (excludeLastDirect) currentTreeNode.nextSibling?.nextSibling != null else currentTreeNode.nextSibling != null
            } else {
                if (excludeLastDirect) currentTreeNode.nextSibling != null else true
            }
        }

        if (excludeLastDirect && currentTreeNode.nextSibling == null) {
            return false
        }

        if (currentTreeNode.isSequence() && currentValueIterator == null) {
            currentValueIterator = evaluateSequenceNode(_currentTreeNode)
            _currentValueIterator = currentValueIterator
        }

        if (currentValueIterator != null) {
            if (currentValueIterator.hasNext()) {
                return true
            }

            do {
                val nextNode = currentTreeNode.nextSibling ?: return false
                _currentTreeNode = nextNode
                currentValueIterator = null

                if (excludeLastDirect && nextNode.nextSibling == null) {
                    break
                }

                if (_currentTreeNode?.isSequence() == true) {
                    currentValueIterator = evaluateSequenceNode(_currentTreeNode)
                    if (currentValueIterator.hasNext()) {
                        _currentValueIterator = currentValueIterator
                        return true
                    }
                }
            } while (currentValueIterator != null)

            _currentValueIterator = null
            currentTreeNode = _currentTreeNode ?: return false
        }

        if (excludeLastDirect && currentTreeNode.nextSibling == null) {
            return false
        }

        return true
    }

    override fun next(): Double {
        return next(byRef = false, excludeLastDirect = false)
    }

    override fun nextRef(): Double {
        return next(byRef = true, excludeLastDirect = false)
    }

    fun next(byRef: Boolean, excludeLastDirect: Boolean): Double {
        var currentTreeNode = _currentTreeNode ?: return NULL
        var currentValueIterator = _currentValueIterator

        if (excludeLastDirect && currentTreeNode.nextSibling == null) {
            return NULL
        }

        if (currentTreeNode.isSequence() && currentValueIterator == null) {
            currentValueIterator = evaluateSequenceNode(_currentTreeNode)
            _currentValueIterator = currentValueIterator
        }

        if (currentValueIterator != null) {
            if (currentValueIterator.hasNext()) {
                return currentValueIterator.next()
            }

            do {
                val nextNode = currentTreeNode.nextSibling ?: return NULL
                _currentTreeNode = nextNode
                currentValueIterator = null

                if (excludeLastDirect && nextNode.nextSibling == null) {
                    break
                }

                if (_currentTreeNode?.isSequence() == true) {
                    currentValueIterator = evaluateSequenceNode(_currentTreeNode)
                    if (currentValueIterator.hasNext()) {
                        _currentValueIterator = currentValueIterator
                        return currentValueIterator.next()
                    }
                }
            } while (currentValueIterator != null)

            _currentValueIterator = null
            currentTreeNode = _currentTreeNode ?: return NULL
        }

        if (excludeLastDirect && currentTreeNode.nextSibling == null) {
            return NULL
        }

        val result = evaluateNode(currentTreeNode, byRef)
        _currentTreeNode = currentTreeNode.nextSibling
        return result
    }

    override fun skipDirect() : Boolean  {
        return skipDirect(false)
    }

    fun skipDirect(excludeLastDirect: Boolean) : Boolean {
        val currentTreeNode = _currentTreeNode ?: return false
        if (excludeLastDirect && currentTreeNode.nextSibling == null) {
            return false
        }

        _currentValueIterator = null
        _currentTreeNode = currentTreeNode.nextSibling
        return true
    }

    private fun evaluateNode(node: CalcExpressionTreeNode?, byRef: Boolean) : Double {
        if (node == null) return NULL

        if (node.token.type == CalcTokenType.Constant) {
            return node.token.number
        }

        if (node.token.type == CalcTokenType.Variable) {
            val variableName = node.token.text ?: return NULL
            val variableRef = _context.getLocalVariableRef(variableName) ?: return NULL
            return if (byRef) {
                variableRef
            } else {
                _context.getValueByRef(variableRef) ?: 0.0
            }
        }
        if (node.token.type == CalcTokenType.String) {
            val string = node.token.text ?: return NULL
            return _context.getStringRef(string) ?: NULL
        }

        val nodeFunction = node.token.function?.function ?: return NULL

        val currentNode = _currentTreeNode
        val currentValueIterator = _currentValueIterator

        _currentTreeNode = node.firstChild
        _currentValueIterator = null

        try {
            var result = nodeFunction.invoke(this)
            if (!byRef && node.token.function.returnsRef) {
                result = if (isValidDouble(result)) {
                    _context.getValueByRef(result) ?: NULL
                } else {
                    NULL
                }
            }
            return result
        } finally {
            // available only for connection of first child and parent
            // in structures of sequent comparision a>=b>=c
            if (!(node.nextSibling != null && node.isBinaryComparison() && node.parent?.isBinaryComparison() == true))
            {
                _context.resetBinaryComparison()
            }

            _currentTreeNode = currentNode
            _currentValueIterator = currentValueIterator
        }
    }

    private fun evaluateSequenceNode(node: CalcExpressionTreeNode?) : CalcExpressionSequenceIterator {

        val nodeSequenceFunction = node?.token?.function?.sequenceFunction
            ?: return CalcExpressionSequenceIterator(emptySequence())

        val evaluator = CalcEvaluator(node.firstChild, _context)
        return CalcExpressionSequenceIterator(nodeSequenceFunction.invoke(evaluator))
    }

    override fun newFunctionRef(paramRefs: List<Double>) : Double? {
        val functionBody = _currentTreeNode ?: return null
        val functionParams = ArrayList<CalcExpressionFuncParam>(paramRefs.size)
        for (paramRef in paramRefs) {
            val paramId = _context.getLocalVariableIdByRef(paramRef)
            val param = CalcExpressionFuncParam(paramId)
            functionParams.add(param)
        }
        val function = CalcExpressionFunc(::callFunction, functionBody, false, functionParams, this)
        return _context.newFunctionRef(function)
    }

    override fun createLambda() : CalcFunctionLambdaContext? {
        val functionBody = getLastNode(excludeLastDirect = false) ?: return null
        return CalcExpressionFunc(::callFunction, functionBody, true, emptyList(), CalcEvaluatorSkipLastWrapper(this))
    }

    private fun getLastNode(excludeLastDirect: Boolean) : CalcExpressionTreeNode? {
        var currentNode = _currentTreeNode
        @Suppress("LiftReturnOrAssignment")
        if (excludeLastDirect) {
            while (currentNode?.nextSibling?.nextSibling != null) {
                currentNode = currentNode.nextSibling
            }
            return if (currentNode?.nextSibling != null) currentNode else null
        } else {
            while (currentNode?.nextSibling != null) {
                currentNode = currentNode.nextSibling
            }
            return currentNode
        }
    }

    override fun getFunction(functionRef: Double) : CalcFunctionFuncContext? {
        return _context.getFunctionByRef(functionRef)
    }

    override fun getLambdaValues() : List<Double>? {
        return _context.getCurrentCallParamValues()
    }

    override fun carryFunctionRef(functionRef: Double, defaults: Map<Double, Double>): Double? {
        val function = _context.getFunctionByRef(functionRef) ?: return null
        val localDefaults = defaults.map { entry -> _context.getLocalVariableIdByRef(entry.key) to entry.value}.toMap()
        val newParams = ArrayList<CalcExpressionFuncParam>(function.params.size)
        for (param in function.params) {
            val newDefault = localDefaults[param.paramId] ?: param.defaultValue
            newParams.add(CalcExpressionFuncParam(param.paramId, newDefault))
        }

        val carriedFunc = CalcExpressionFunc(::callFunction, function.body, function.isLambda,
                newParams, function.callContextOperands, function.objectRef)
        return _context.newFunctionRef(carriedFunc)
    }

    private fun callFunction(function: CalcExpressionFunc, paramValues: List<Double>) : CalcFunctionCallContext {
        val callContext = CalcExpressionFuncCallContext()

        val functionNode = function.body
        val stackFrame = _context.createStackFrame(function, paramValues, function.isLambda)

        callContext.results = sequence {

            try {
                if (functionNode.isSequence()) {
                    _context.pushStackFrame(stackFrame)
                    val iterator = evaluateSequenceNode(functionNode)
                    _context.popStackFrame(stackFrame)

                    var hasNext: Boolean
                    do {
                        _context.pushStackFrame(stackFrame)
                        hasNext = iterator.hasNext()
                        if (hasNext) {
                            val nextValue = iterator.next()
                            _context.popStackFrame(stackFrame)
                            yield(nextValue)
                        }
                    } while (hasNext)
                    _context.popStackFrame(stackFrame)
                } else {
                    _context.pushStackFrame(stackFrame)
                    val singleValue = evaluateNode(functionNode, false)
                    _context.popStackFrame(stackFrame)
                    yield(singleValue)
                }
            } catch (command: CalcExpressionReturnCommand) {
                _context.popStackFrame(stackFrame)
                callContext.setBroken()
                if (function.isLambda) {
                    throw command
                } else {
                    yieldAll(command.returnValues)
                }
            } catch (command: CalcExpressionBreakCommand) {
                _context.popStackFrame(stackFrame)
                callContext.setBroken()
                yieldAll(command.returnValues)
            } catch (command: CalcExpressionContinueCommand) {
                _context.popStackFrame(stackFrame)
                yieldAll(command.returnValues)
            }
        }

        return callContext
    }

    private open class CalcExpressionCommand : Throwable()
    private class CalcExpressionReturnCommand(val returnValues: List<Double> = emptyList()) : CalcExpressionCommand()
    private class CalcExpressionBreakCommand(val returnValues: List<Double> = emptyList()) : CalcExpressionCommand()
    private class CalcExpressionContinueCommand(val returnValues: List<Double> = emptyList()) : CalcExpressionCommand()

    private class CalcExpressionSequenceIterator(val sequence: Sequence<Double>) : Iterator<Double> {
        var sequenceIterator: Iterator<Double>? = null
        var pendingCommand: CalcExpressionCommand? = null

        fun getIterator() : Iterator<Double> {
            var iterator = sequenceIterator
            if (iterator == null) {
                iterator = sequence.iterator()
                sequenceIterator = iterator
            }
            return iterator
        }

        override fun hasNext(): Boolean {
            @Suppress("LiftReturnOrAssignment")
            if (pendingCommand != null) {
                return true
            }
            try {
                return getIterator().hasNext()
            } catch (command: CalcExpressionCommand) {
                pendingCommand = command
                return true
            }
        }

        override fun next(): Double {
            val command = pendingCommand
            if (command != null) {
                throw command
            }
            return getIterator().next()
        }
    }

    private class CalcExpressionFuncCallContext(var results: Sequence<Double> = emptySequence(),
                                                var broken: Boolean = false)
        : CalcFunctionCallContext {

        override fun results(): Sequence<Double> {
            return results
        }

        override fun isBroken(): Boolean {
            return broken
        }

        override fun complete() {
            results.last()
        }

        fun setBroken() {
            broken = true
        }
    }

    private class CalcEvaluatorSkipLastWrapper(val evaluator: CalcEvaluator)
        : CalcFunctionOperandSequence, CalcFunctionOperandIterator {

        override fun iterator(): CalcFunctionOperandIterator = this

        override fun skipDirect(): Boolean {
            return evaluator.skipDirect(true)
        }

        override fun hasNextDirect(): Boolean {
            return evaluator.hasNext(direct = true, excludeLastDirect = true)
        }

        override fun hasNext(): Boolean {
            return evaluator.hasNext(direct = false, excludeLastDirect = true)
        }

        override fun nextRef(): Double {
            return evaluator.next(byRef = true, excludeLastDirect = true)
        }

        override fun next(): Double {
            return evaluator.next(byRef = false, excludeLastDirect = true)
        }

        override fun firstOperand(): Double? {
            return evaluator.firstOperand()
        }

        override fun lastOperand(): Double? {
            return evaluator.lastOperand(excludeLastDirect = true)
        }
    }
}