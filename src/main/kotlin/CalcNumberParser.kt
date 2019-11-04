internal class CalcNumberParser {

    fun parseNumber(context: CalcContext) : Double?  {
        val text = context.text
        var localOffset = context.offset
        var validOffset = context.offset
        var result = 0.0

        var hasPoint = false
        var fractionalMultiplier = 0.1

        while (localOffset < text.length)  {
            val character = text[localOffset]
            if (CalcCharHelper.isEmptyObject(character)) {
                localOffset++
                continue
            }

            if (character == '.') {
                if (hasPoint) break

                if (localOffset + 1 < text.length && CalcCharHelper.isNumberChar(text[localOffset + 1]) == null) {
                    break
                }

                hasPoint = true
                localOffset++
                continue
            }

            val digit = character - '0'
            if (digit < 0 || digit > 9) break

            if (hasPoint) {
                result += digit * fractionalMultiplier
                fractionalMultiplier *= 0.1
            }
            else {
                result = result * 10 + digit
            }

            localOffset++
            validOffset = localOffset
        }

        if (result.isInfinite() || result.isNaN()) return null

        context.offset = validOffset
        return result
    }
}
