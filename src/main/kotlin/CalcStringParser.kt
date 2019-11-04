internal class CalcStringParser {

    fun parseString(context: CalcContext) : String?  {
        val text = context.text
        val beginIndex = context.offset
        val endIndex = text.indexOf(CalcCharHelper.StringChar, beginIndex)
        if (endIndex < 0) {
            return null
        }
        context.offset = endIndex + 1
        return text.substring(beginIndex, endIndex)
    }
}