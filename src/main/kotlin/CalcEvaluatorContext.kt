import java.io.*
import java.util.*
import kotlin.collections.HashMap

internal data class CalcEvaluatorStackFrame(
        val function: CalcExpressionFunc?,
        val paramValues: List<Double>,
        val localVariables: CalcCollectionMap,
        val objectRef: Double? = null,
        var lastBinaryComparisonValue: Double? = null
)

internal class CalcEvaluatorContext(input: CalcInput,
                                    strings: Collection<String>) :
        CalcFunctionDataContext,
        CalcFunctionComparisonContext,
        CalcFunctionClassesContext {

    private val _state: Any? = input.state
    private val _inputReader: Lazy<BufferedReader?> = lazy { if (input.inputStream != null ) BufferedReader(InputStreamReader(input.inputStream)) else null }
    private val _outputReader: Lazy<BufferedWriter?> = lazy { if (input.outputStream != null ) BufferedWriter(OutputStreamWriter(input.outputStream)) else null }

    private val _idMask : Long = 1L.shl(62)
    private val _idMaskInverted : Long = _idMask.inv()
    private var _collectionIdSequence: Int = 1
    private var _varIdSequence: Int = 1

    private val _callStack = ArrayDeque<CalcEvaluatorStackFrame>()
    private val _collections = HashMap<Int, CalcCollection>()
    private val _functions = HashMap<Int, CalcExpressionFunc>()
    private val _stringRefs = HashMap<String, Double>()
    private val _variableRefs = HashMap<String, Double>()

    private val _localContextId = _collectionIdSequence
    private val _localContextVarRef = VariableRef(_localContextId, 0)
    private val _localContextRef = newCollectionRef(CalcCollectionProxy(::getLocalVars))

    private val _globalContext = CalcCollectionMap()
    private val _globalContextRef = newCollectionRef(_globalContext)

    private val _argsRef = newCollectionRef(CalcCollectionList(input.args))

    private val _stringArgs = CalcCollectionArray(input.stringArgs.size)
    private val _stringArgsRef = newCollectionRef(_stringArgs)

    init {
        for ((index, stringArg) in input.stringArgs.withIndex()) {
            _stringArgs.set(index.toDouble(), newStringRef(stringArg))
        }
        for (string in strings) {
            _stringRefs[string] = newStringRef(string)
        }

        val rootStackFrame = CalcEvaluatorStackFrame(null, input.args, _globalContext)
        _callStack.addFirst(rootStackFrame)
    }

    fun getState() : Any? = _state
    fun getInputReader() : BufferedReader? = _inputReader.value
    fun getOutputWriter() : BufferedWriter? = _outputReader.value
    fun flush() { getOutputWriter()?.flush() }

    fun createStackFrame(function: CalcExpressionFunc, paramValues : List<Double>, closure: Boolean) : CalcEvaluatorStackFrame {
        val callParams = function.params
        val localVariables = if (closure) getLocalVars() else CalcCollectionMap(getLocalVars().asMap())

        val callParamValues = function.getCallParamValues(paramValues)
        for ((index, value) in callParamValues.withIndex()) {
            localVariables.set(callParams[index].paramId, value)
        }
        var objectRef = function.objectRef
        if (objectRef == null && closure) {
            objectRef = getCurrentStackFrame().objectRef
        }
        return CalcEvaluatorStackFrame(function, paramValues, localVariables, objectRef)
    }

    fun pushStackFrame(stackFrame: CalcEvaluatorStackFrame) {
        _callStack.addFirst(stackFrame)
    }

    fun popStackFrame(stackFrame: CalcEvaluatorStackFrame) {
        if (_callStack.size > 1 && _callStack.peekFirst() == stackFrame) {
            _callStack.removeFirst()
        }
    }

    private fun getCurrentStackFrame() : CalcEvaluatorStackFrame {
        return _callStack.peekFirst()
    }

    private fun getLocalVars() : CalcCollectionMap {
        return getCurrentStackFrame().localVariables
    }

    fun getCurrentCallParamValues() : List<Double> {
        return getCurrentStackFrame().paramValues
    }

    override fun getLastBinaryComparisonValue(): Double? {
        return getCurrentStackFrame().lastBinaryComparisonValue
    }
    override fun setLastBinaryComparisonValue(value: Double?) {
        getCurrentStackFrame().lastBinaryComparisonValue = value
    }

    override fun newCollectionRef(collection: CalcCollection): Double {
        return createEntityRef(collection, _collections)
    }

    override fun getCollectionByRef(collectionRef: Double): CalcCollection? {
        return getEntityByRef(collectionRef, _collections)
    }

    override fun newStringRef(string: String) : Double {
        return newCollectionRef(CalcCollectionString(string))
    }

    override fun getStringByRef(ref: Double) : String? {
        val collection = getCollectionByRef(ref) ?: return null
        return (collection as? CalcCollectionString)?.asString()
    }

    fun getStringRef(string: String) : Double? {
        return _stringRefs[string]
    }

    fun resetBinaryComparison() {
        setLastBinaryComparisonValue(null)
    }

    fun newFunctionRef(function: CalcExpressionFunc) : Double {
        return createEntityRef(function, _functions)
    }

    fun getFunctionByRef(functionRef: Double) : CalcExpressionFunc? {
        return getEntityByRef(functionRef, _functions)
    }

    override fun getLocalContextRef() : Double {
        return _localContextRef
    }

    override fun getGlobalContextRef() : Double {
        return _globalContextRef
    }

    override fun getArgsRef() : Double {
        return _argsRef
    }

    override fun getStringArgsRef() : Double {
        return _stringArgsRef
    }

    override fun getThisObjectRef() : Double {
        return getCurrentStackFrame().objectRef ?: _localContextRef
    }

    fun getLocalVariableRef(variableName: String) : Double? {
        val ref = _variableRefs[variableName]
        if (ref != null) {
            return ref
        }

        val variableId = _varIdSequence++
        _localContextVarRef.variableId = variableId
        val newRef = _localContextVarRef.toDouble()
        _variableRefs[variableName] = newRef
        return newRef
    }

    fun getLocalVariableIdByRef(variableRef: Double) : Double {
        return VariableRef(variableRef).variableId.toDouble()
    }

    override fun newClassRef(fieldRefs: List<Double>): Double {
        val fieldIds = ArrayList<Double>(fieldRefs.size)
        for (fieldRef in fieldRefs) {
            val fieldId = getLocalVariableIdByRef(fieldRef)
            if (!fieldIds.contains(fieldId)) {
                fieldIds.add(fieldId)
            }
        }
        val classCollection = CalcCollectionClass(fieldIds)
        return newCollectionRef(classCollection)
    }

    override fun extendClassRef(classRef: Double, fieldRefs: List<Double>) : Double? {
        val classCollection = (getCollectionByRef(classRef) ?: return null)
                as? CalcCollectionClass ?: return null

        val extendsClassFieldIds = ArrayList<Double>(classCollection.getFields())
        for (fieldRef in fieldRefs) {
            val fieldId = getLocalVariableIdByRef(fieldRef)
            if (!extendsClassFieldIds.contains(fieldId)) {
                extendsClassFieldIds.add(fieldId)
            }
        }

        val extendClass = CalcCollectionClass(extendsClassFieldIds)
        for (method in classCollection.getMethods().entries) {
            extendClass.setMethod(method.key, method.value)
        }
        return newCollectionRef(extendClass)
    }

    override fun newClassMethodRef(memberRef: Double, functionRef: Double): Double? {
        val varRef = VariableRef(memberRef)
        val classId = varRef.collectionId
        val classCollection = (_collections[classId] ?: return null)
                as? CalcCollectionClass ?: return null

        val functionId = getEntityIdByRef(functionRef) ?: return null
        val variableId = varRef.variableId.toDouble()
        classCollection.setMethod(variableId, getEntityRef(functionId))
        return getEntityRef(classId)
    }

    override fun newClassConstructorRef(classRef: Double, functionRef: Double): Double? {
        val classCollection = getCollectionByRef(classRef) as? CalcCollectionClass ?: return null
        classCollection.setConstructor(functionRef)
        return classRef
    }

    override fun newObjectRef(classRef: Double, fieldValues: List<Double>): Double? {
        val classCollection = (getEntityByRef(classRef, _collections) ?: return null)
                as? CalcCollectionClass ?: return null

        val classFields = classCollection.getFields()
        val instanceFields = ArrayList<Double>(fieldValues)
        while (instanceFields.size < classFields.size) {
            instanceFields.add(0.0)
        }

        val instanceFieldsMap = classCollection.getFields().zip(instanceFields).toMap()
        val classMethods = classCollection.getMethods()
        val instance = CalcCollectionObject(instanceFieldsMap)
        val instanceRef = newCollectionRef(instance)
        val constructorRef = classCollection.getConstructor()
        for (classMethod in classMethods) {
            val function = getFunctionByRef(classMethod.value) ?: continue
            val method = function.withObjectRef(instanceRef)
            instance.setMethod(classMethod.key, newFunctionRef(method))
        }

        if (constructorRef != null) {
            val function = getFunctionByRef(constructorRef) ?: return null
            val method = function.withObjectRef(instanceRef)
            method.callFunction().complete()
        }

        return instanceRef
    }

    override fun getMemberRef(objectRef: Double, variableRef: Double): Double? {
        val objectId = getEntityIdByRef(objectRef) ?: return null
        val ref = VariableRef(variableRef)
        ref.collectionId = objectId
        return ref.toDouble()
    }

    private fun getEntityRef(entityId: Int) : Double {
        val ref = _idMask or entityId.toLong()
        return Double.fromBits(ref)
    }

    private fun <T> createEntityRef(entity: T, entities: HashMap<Int, T>) : Double {
        if (entity is CalcCollectionString) {
            val stringRef = _stringRefs[entity.asString()]
            if (stringRef != null) {
                return stringRef
            }
        }
        val id = _collectionIdSequence++
        entities[id] = entity
        val entityRef = getEntityRef(id)
        if (entity is CalcCollectionString) {
            _stringRefs[entity.asString()] = entityRef
        }
        return entityRef
    }

    override fun getValueByRef(ref: Double) : Double? {
        if (ref.toBits() and _idMask == _idMask) {
            return ref
        }
        val varRef = VariableRef(ref)
        val objectCollection = _collections[varRef.collectionId] ?: return null
        return objectCollection.get(varRef.variableId.toDouble())
    }

    override fun setValueByRef(ref: Double, value: Double?): Double? {
        if (ref.toBits() and _idMask == _idMask) {
            return null // setting directly (instead of variables) in memory is not available
        }
        val varRef = VariableRef(ref)
        val objectCollection = _collections[varRef.collectionId] ?: return null
        return objectCollection.set(varRef.variableId.toDouble(), value)
    }

    private fun getEntityIdByRef(entityRef: Double) : Int? {
        var ref = entityRef
        var iterationLimit = 10
        do {
            if (ref.toBits() and _idMask == _idMask) {
                return (ref.toBits() and _idMaskInverted).toInt()
            }
            ref = getValueByRef(ref) ?: return null
        } while (iterationLimit-- > 0)

        return null
    }

    private fun <T> getEntityByRef(entityRef: Double, entities: Map<Int, T>) : T? {
        val entityId = getEntityIdByRef(entityRef) ?: return null
        return entities[entityId]
    }

    private class VariableRef {
        var collectionId : Int
        var variableId: Int

        constructor(collectionId: Int, variableId: Int) {
            this.collectionId = collectionId
            this.variableId = variableId
        }

        constructor (valueRef: Double){
            val bits = valueRef.toBits()
            collectionId = bits.shr(32).toInt()
            variableId = bits.shl(32).shr(32).toInt()
        }

        fun toDouble() : Double {
            val bits = collectionId.toLong().shl(32) + variableId
            return Double.fromBits(bits)
        }
    }
}