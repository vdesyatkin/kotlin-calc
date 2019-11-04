internal data class CalcExpressionFuncParam(val paramId: Double, val defaultValue: Double? = null)

internal class CalcExpressionFunc(
    val call: (CalcExpressionFunc, List<Double>) -> CalcFunctionCallContext,
    val body: CalcExpressionTreeNode,
    val isLambda: Boolean,
    val params: List<CalcExpressionFuncParam>,
    val callContextOperands: CalcFunctionOperandSequence,
    val objectRef: Double? = null) : CalcFunctionFuncContext, CalcFunctionLambdaContext {

    fun getCallParamValues(paramValues: List<Double>): List<Double> {
        if (params.isEmpty()) return emptyList()

        val callParamValues = ArrayList<Double>(params.size)
        val valuesIterator = paramValues.iterator()

        for (param in params) {
            when {
                param.defaultValue != null -> callParamValues.add(param.defaultValue)
                valuesIterator.hasNext() -> callParamValues.add(valuesIterator.next())
                else -> callParamValues.add(Double.NaN)
            }
        }

        return callParamValues
    }

    override fun callFunction(vararg paramValues: Double): CalcFunctionCallContext {
        return callFunction(paramValues.toList())
    }

    override fun callFunction(paramValues: List<Double>): CalcFunctionCallContext {
        return call.invoke(this, paramValues)
    }

    override fun operands() : CalcFunctionOperandSequence {
        return callContextOperands
    }

    fun withObjectRef(objectRef: Double?): CalcExpressionFunc {
        return CalcExpressionFunc(this.call, this.body, this.isLambda,
            this.params, this.callContextOperands, objectRef)
    }
}