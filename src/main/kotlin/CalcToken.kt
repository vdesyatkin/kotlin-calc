internal enum class CalcTokenType
{
    Empty,
    Constant,
    Variable,
    String,
    Function,
    BinaryOperator,
    Comma,
    LeftBracket
}

internal data class CalcToken(
    val type: CalcTokenType,
    val offset: Int,
    val number: Double = 0.0,
    val text: String? = null,
    val function: CalcFunction? = null,
    val bracket: CalcBracket? = null
)