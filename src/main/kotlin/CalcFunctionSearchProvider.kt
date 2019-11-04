internal data class CalcFunctionSearchResult(val function: CalcFunction,
                                             val functionArity: CalcFunctionArity,
                                             val text: String)

internal interface CalcFunctionSearchProvider {
    fun find(context: CalcContext, contextArity: CalcContextArity) : CalcFunctionSearchResult?
}