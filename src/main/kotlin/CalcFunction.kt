enum class CalcAssociativity { Left, Right }

typealias CalcFunctionDelegate = (CalcFunctionContext) -> Double
typealias CalcFunctionSequenceDelegate = (CalcFunctionContext) -> Sequence<Double>

internal data class CalcBinaryOperator(
        val name: String?,
        val alias: String?,
        val unaryPostAlias: String?,
        val associativity: CalcAssociativity,
        val precedence: Int,
        val isComparison: Boolean,
        val isCommutative: Boolean)

internal data class CalcFunction(
    val name: String,
    val alias: String?,
    val binaryOperator: CalcBinaryOperator?,
    val function: CalcFunctionDelegate?,
    val sequenceFunction: CalcFunctionSequenceDelegate?,
    val minOperandCount: Int,
    val maxOperandCount: Int?,
    val returnsRef: Boolean) {
    val isSequence: Boolean = sequenceFunction != null
}