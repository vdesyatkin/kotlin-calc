
internal class CalcFunctionTrie : CalcFunctionSearchProvider {

    private val _unaryNodes = HashMap<Char, CalcFunctionPrefixTreeNode>()
    private val _binaryNodes = HashMap<Char, CalcFunctionPrefixTreeNode>()

    fun add(function: CalcFunction) {

        add(function.name, function, CalcFunctionArity.UnaryPre)
        if (function.alias != null && function.alias.isNotEmpty()) {
            add(function.alias, function, CalcFunctionArity.UnaryPre)
        }

        if (function.binaryOperator != null) {
            if (function.binaryOperator.name != null) {
                add(function.binaryOperator.name, function, CalcFunctionArity.Binary)
            }
            if (function.binaryOperator.alias != null && function.binaryOperator.alias.isNotEmpty()) {
                add(function.binaryOperator.alias, function, CalcFunctionArity.Binary)
            }
            if (function.binaryOperator.unaryPostAlias != null && function.binaryOperator.unaryPostAlias.isNotEmpty()) {
                add(function.binaryOperator.unaryPostAlias, function, CalcFunctionArity.UnaryPost)
            }
        }
    }

    private fun add(functionText: String, function: CalcFunction, functionArity: CalcFunctionArity) {
        val text = functionText.toLowerCase()
        var currentSet = if (functionArity == CalcFunctionArity.UnaryPre) _unaryNodes else _binaryNodes

        for (index in text.indices) {

            val character = text[index]

            var node = currentSet[character]

            if (node == null) {
                node = CalcFunctionPrefixTreeNode()
                currentSet[character] = node
            }
            currentSet = node.children

            if (index == text.length - 1) {
                if (node.function == null) {
                    node.function = function
                    node.functionArity = functionArity
                    node.text = text
                }
            }
        }
    }

    override fun find(context: CalcContext, contextArity: CalcContextArity) : CalcFunctionSearchResult? {

        var localOffset = context.offset
        val text = context.text

        if (localOffset < 0 ||localOffset >= text.length) {
            return null
        }

        var currentSet = if (contextArity == CalcContextArity.Unary) _unaryNodes else _binaryNodes
        var currentFunction: CalcFunction? = null
        var currentFunctionArity: CalcFunctionArity? = null
        var currentFunctionText: String? = null
        var skipWhitespaces = true

        while (currentSet.isNotEmpty() && localOffset < text.length) {

            val character = text[localOffset]

            if (CalcCharHelper.isEmptyObject(character)) {
                if (!skipWhitespaces) break

                localOffset++
                continue
            }
            skipWhitespaces = false

            val node = currentSet[character.toLowerCase()] ?: break

            if (node.function != null) {
                currentFunction = node.function
                currentFunctionArity = node.functionArity
                currentFunctionText = node.text
            }

            localOffset++
            currentSet = node.children
        }
        if (currentFunction == null || currentFunctionArity == null || currentFunctionText == null) {
            return null
        }

        context.offset = localOffset
        return CalcFunctionSearchResult(currentFunction, currentFunctionArity, currentFunctionText)
    }

    private class CalcFunctionPrefixTreeNode(
            var function: CalcFunction? = null,
            var functionArity: CalcFunctionArity? = null,
            var text: String? = null) {
        val children = HashMap<Char, CalcFunctionPrefixTreeNode>()
    }
}