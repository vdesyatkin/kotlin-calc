@Suppress("unused")
internal enum class CalcBracket(val left: Char, val right: Char) {
    Round('(',')'),
    Square('[',']'),
    Figure('{','}');
}

internal object CalcCharHelper {

    const val CommaChar = ','
    const val VariableChar = '@'
    const val StringChar = '\''

    fun isEmptyObject(character: Char) : Boolean {
        return character == ' ' || character == '\t' || character == '\r' || character == '\n'
    }

    fun isLetterChar(character: Char) : Char? {
        val lowerChar = character.toLowerCase()
        return if (lowerChar in 'a'..'z' || lowerChar == '_') lowerChar else null
    }

    fun isNumberChar(character: Char) : Char? {
        return if (character in '0'..'9') character else null
    }

    fun checkLeftBracket(character: Char) : CalcBracket? {
        return CalcBracket.values().firstOrNull { it.left == character }
    }

    fun checkRightBracket(character: Char) : CalcBracket? {
        return CalcBracket.values().firstOrNull { it.right == character }
    }
}