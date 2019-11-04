import java.io.BufferedReader
import java.io.BufferedWriter

internal enum class CalcContextArity { Unary, Binary }

internal enum class CalcFunctionArity { UnaryPre, UnaryPost, Binary }

interface CalcFunctionContext {
    fun state(): Any?
    fun inputReader(): BufferedReader?
    fun outputWriter(): BufferedWriter?
    fun operands(skipLast: Boolean = false) : CalcFunctionOperandSequence
    fun data() : CalcFunctionDataContext
    fun comparison() : CalcFunctionComparisonContext
    fun functions() : CalcFunctionFuncsContext
    fun classes() : CalcFunctionClassesContext
}

interface CalcFunctionOperandSequence : Sequence<Double> {
    fun firstOperand() : Double?
    fun lastOperand() : Double?
    override fun iterator() : CalcFunctionOperandIterator
}

interface CalcFunctionOperandIterator : Iterator<Double> {
    fun skipDirect() : Boolean
    fun hasNextDirect() : Boolean
    fun nextRef() : Double
}

interface CalcFunctionComparisonContext {
    fun getLastBinaryComparisonValue() : Double?
    fun setLastBinaryComparisonValue(value: Double?)
}

interface CalcFunctionDataContext {
    fun getLocalContextRef() : Double
    fun getGlobalContextRef() : Double
    fun getArgsRef() : Double
    fun getStringArgsRef() : Double
    fun getValueByRef(ref: Double) : Double?
    fun setValueByRef(ref: Double, value: Double?) : Double?
    fun newCollectionRef(collection: CalcCollection) : Double
    fun getCollectionByRef(collectionRef: Double) : CalcCollection?
    fun newStringRef(string: String) : Double
    fun getStringByRef(ref: Double) : String?
}

interface CalcFunctionClassesContext {
    fun newClassRef(fieldRefs: List<Double>) : Double
    fun newClassConstructorRef(classRef: Double, functionRef: Double) : Double?
    fun newClassMethodRef(memberRef: Double, functionRef: Double) : Double?
    fun extendClassRef(classRef: Double, fieldRefs: List<Double>) : Double?
    fun newObjectRef(classRef: Double, fieldValues: List<Double>) : Double?
    fun getThisObjectRef() : Double
    fun getMemberRef(objectRef: Double, variableRef: Double) : Double?
}

interface CalcFunctionFuncsContext {
    fun newFunctionRef(paramRefs: List<Double>) : Double?
    fun getFunction(functionRef: Double) : CalcFunctionFuncContext?
    fun carryFunctionRef(functionRef: Double, defaults: Map<Double, Double>) : Double?
    fun createLambda() : CalcFunctionLambdaContext?
    fun getLambdaValues() : List<Double>?
    fun returnFunction(values: List<Double> = emptyList())
    fun returnFunctionWithBreakLoop(values: List<Double> = emptyList())
    fun returnFunctionWithContinueLoop(values: List<Double> = emptyList())
}

interface CalcFunctionFuncContext {
    fun callFunction(paramValues: List<Double>) : CalcFunctionCallContext
    fun callFunction(vararg paramValues : Double) : CalcFunctionCallContext
}

interface CalcFunctionLambdaContext {
    fun callFunction(paramValues: List<Double>) : CalcFunctionCallContext
    fun callFunction(vararg paramValues : Double) : CalcFunctionCallContext
    fun operands() : CalcFunctionOperandSequence
}

interface CalcFunctionCallContext {
    fun results() : Sequence<Double>
    fun isBroken() : Boolean
    fun complete()
}
