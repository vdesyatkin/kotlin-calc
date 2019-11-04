import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import CalcNumberHelper.TRUE
import CalcNumberHelper.FALSE
import CalcNumberHelper.NULL
import CalcNumberHelper.isTrue
import CalcNumberHelper.isFalse
import CalcNumberHelper.isNull
import CalcNumberHelper.isNotNull
import CalcNumberHelper.isValidDouble
import java.util.*
import kotlin.math.floor

internal object CalcDefaultFunctions {

    fun defaultFunctionSet() : CalcFunctionSet{

        val set = CalcFunctionSet.empty()

        set.addSequenceOperatorFunction(";", ::funcSemicolon,
                ";", CalcAssociativity.Left, precedence = 0,
                minOperandCount = 1, maxOperandCount = null, isCommutative = true)

        set.addFunction("return", ::funcReturn,
                minOperandCount = 1, maxOperandCount = 1)

        set.addParameterlessFunction("break", ::funcBreak)

        set.addFunction("break.yield", ::funcYieldBreak,
                minOperandCount = 1, maxOperandCount = 1)

        set.addParameterlessFunction("continue", ::funcContinue)

        set.addFunction("continue.yield", ::funcYieldContinue,
                minOperandCount = 1, maxOperandCount = 1)

        set.addFunction("array", ::funcArray,
                minOperandCount = 1, maxOperandCount = 2, returnsRef = true)

        set.addOperatorFunction("list", ::funcList,
                null, CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = null, returnsRef = true,
                unaryPostAlias = ".toList")

        set.addOperatorFunction("add", ::funcAdd,
                ".add", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = null, returnsRef = true)

        set.addParameterlessFunction("hashMap", ::funcHashMap,
                returnsRef = true)

        set.addSequenceOperatorFunction("values", ::funcValues,
                null, CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = 1,
                unaryPostAlias = ".values")

        set.addSequenceOperatorFunction("keys", ::funcKeys,
                null, CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = 1,
                unaryPostAlias = ".keys")

        set.addOperatorFunction("get", ::funcGet,
                ".get", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = null)

        set.addOperatorFunction("set", ::funcSet,
                ".set", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 3, maxOperandCount = 3, returnsRef = true)

        set.addOperatorFunction("size", ::funcSize,
                null, CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = 1,
                unaryPostAlias = ".size")

        set.addOperatorFunction("length", ::funcSize,
                null, CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = 1,
                unaryPostAlias = ".length")

        set.addOperatorFunction("equals", ::funcEquals,
                ".equals", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = null)

        set.addOperatorFunction("find", ::funcFind,
                ".find", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = 3)

        set.addOperatorFunction("findFrom", ::funcFindFrom,
                ".findFrom", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 3, maxOperandCount = 3)

        set.addOperatorFunction("slice", ::funcSlice,
                ".slice", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = 3,
                binaryAlias = ".part", unaryAlias = "part", returnsRef = true)

        set.addOperatorFunction("sliceFrom", ::funcSliceFrom,
                ".sliceFrom", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = 2, returnsRef = true)

        set.addOperatorFunction("sliceTo", ::funcSliceTo,
                ".sliceTo", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = 2, returnsRef = true)

        set.addOperatorFunction("concat", ::funcConcat,
                ".concat", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = null, isCommutative = true,
                binaryAlias = ".+", returnsRef = true)

        set.addParameterlessFunction("true", { TRUE })

        set.addParameterlessFunction("false", { FALSE })

        set.addParameterlessFunction("null", { NULL })

        set.addOperatorFunction("assign", ::funcAssign,
                "=", CalcAssociativity.Left, precedence = 2,
                minOperandCount = 2, maxOperandCount = null,
                binaryAlias = ":=")

        set.addSequenceOperatorFunction("lambda", ::funcLambda,
                "->", CalcAssociativity.Left, precedence = 1,
                minOperandCount = 2, maxOperandCount = null,
                binaryAlias = "=>")

        set.addParameterlessFunction("it", ::funcIt)

        set.addFunction("function", ::funcFunction,
                minOperandCount = 1, maxOperandCount = null,
                unaryAlias = "func", returnsRef = true)

        set.addOperatorFunction("carry", ::funcCarry,
                ".carry", CalcAssociativity.Left, precedence = 100,
                minOperandCount = 3, maxOperandCount = null, returnsRef = true)

        set.addSequenceOperatorFunction("call", ::funcCall,
                ".call", CalcAssociativity.Left, precedence = 100,
                minOperandCount = 1, maxOperandCount = null)

        set.addSequenceOperatorFunction("callLambda", ::funcCallLambda,
                null, CalcAssociativity.Left, precedence = 100,
                minOperandCount = 1, maxOperandCount = 1,
                unaryPostAlias = ".lambda", unaryAlias = "::")

        set.addFunction("class", ::funcClass,
                minOperandCount = 0, maxOperandCount = null,
                returnsRef = true)

        set.addOperatorFunction("extends", ::funcExtends,
                ".extends", CalcAssociativity.Left, precedence = 200,
                minOperandCount = 1, maxOperandCount = null,
                returnsRef = true)

        set.addOperatorFunction("constructor", ::funcConstructor,
            ".constructor", CalcAssociativity.Left, precedence = 200,
            minOperandCount = 2, maxOperandCount = 2, returnsRef = true,
            binaryAlias = ".ctor", unaryAlias = "ctor")

        set.addOperatorFunction("method", ::funcMethod,
                ".method", CalcAssociativity.Left, precedence = 200,
                minOperandCount = 2, maxOperandCount = null,
                returnsRef = true)

        set.addOperatorFunction("methodRef", ::funcMethodRef,
                ".methodref", CalcAssociativity.Left, precedence = 200,
                minOperandCount = 2, maxOperandCount = 2,
                returnsRef = true)

        set.addOperatorFunction("new", ::funcNew,
                ".new", CalcAssociativity.Left, precedence = 200,
                minOperandCount = 1, maxOperandCount = null,
                returnsRef = true)

        set.addOperatorFunction("member", ::funcMember,
                ".", CalcAssociativity.Left, precedence = 200,
                minOperandCount = 2, maxOperandCount = 2,
                returnsRef = true)

        set.addParameterlessFunction("this", ::funcThis,
                returnsRef = true)

        set.addParameterlessFunction("global", ::funcGlobal,
                returnsRef = true)

        set.addParameterlessFunction("local", ::funcLocal,
                returnsRef = true)

        set.addParameterlessFunction("args", ::funcArgs,
                returnsRef = true)

        set.addParameterlessFunction("string.args", ::funcStringArgs,
                returnsRef = true)

        set.addOperatorFunction("check", ::funcCheck,
                "?:", CalcAssociativity.Left, precedence = 5,
                minOperandCount = 2, maxOperandCount = 2,
                binaryAlias = "??")

        set.addFunction("floor", ::funcFloor,
                minOperandCount = 1, maxOperandCount = 1)

        set.addFunction("ceiling", ::funcCeiling,
                minOperandCount = 1, maxOperandCount = 1)

        set.addFunction("round", ::funcRound,
                minOperandCount = 1, maxOperandCount = 1)

        set.addFunction("not", ::funcNot,
                minOperandCount = 1, maxOperandCount = 1,
                unaryAlias = "!")

        set.addOperatorFunction("or", ::funcOr,
                "or", CalcAssociativity.Left, precedence = 10,
                minOperandCount = 1, maxOperandCount = null, isCommutative = true,
                unaryAlias = "any", binaryAlias = "|")

        set.addOperatorFunction("xor", ::funcXor,
                "xor", CalcAssociativity.Left, precedence = 11,
                 minOperandCount = 1, maxOperandCount = null, isCommutative = true,
                 unaryAlias = "single")

        set.addOperatorFunction("and", ::funcAnd,
                "and", CalcAssociativity.Left, precedence = 12,
                minOperandCount = 1, maxOperandCount = null, isCommutative = true,
                unaryAlias = "all", binaryAlias = "&")

        set.addOperatorFunction("sum", ::funcSum,
                "+", CalcAssociativity.Left, precedence = 20,
                minOperandCount = 1, maxOperandCount = null, isCommutative = true,
                binaryAlias = ".sum")

        set.addOperatorFunction("sub", ::funcSub,
                "-", CalcAssociativity.Left, precedence = 20,
                minOperandCount = 1, maxOperandCount = null,
                unaryAlias = "-")

        set.addOperatorFunction("mult", ::funcMul,
                "*", CalcAssociativity.Left, precedence = 30,
                minOperandCount = 1, maxOperandCount = null, isCommutative = true)

        set.addOperatorFunction("div", ::funcDiv,
                "/", CalcAssociativity.Left, precedence = 30,
                minOperandCount = 1, maxOperandCount = null)

        set.addOperatorFunction("mod", ::funcMod,
                "%", CalcAssociativity.Left, precedence = 30,
                minOperandCount = 1, maxOperandCount = null)

        set.addOperatorFunction("pow", ::funcPow,
                "^", CalcAssociativity.Right, precedence = 40,
                minOperandCount = 1, maxOperandCount = null)

        set.addOperatorFunction("inc", ::funcInc,
                "+=", CalcAssociativity.Left, precedence = 100,
                minOperandCount = 1, maxOperandCount = 2,
                unaryAlias = "++")

        set.addOperatorFunction("incpost", ::funcIncPost,
                "++=", CalcAssociativity.Left, precedence = 100,
                minOperandCount = 1, maxOperandCount = 2,
                unaryPostAlias = "++")

        set.addOperatorFunction("dec", ::funcDec,
                "-=", CalcAssociativity.Left, precedence = 100,
                minOperandCount = 1, maxOperandCount = 2,
                unaryAlias = "--")

        set.addOperatorFunction("decpost", ::funcDecPost,
                "--=", CalcAssociativity.Left, precedence = 100,
                minOperandCount = 1, maxOperandCount = 2,
                unaryPostAlias = "--")

        set.addFunction("sqr", ::funcSqr,
                minOperandCount = 1, maxOperandCount = 1)

        set.addFunction("sqrt", ::funcSqrt,
                minOperandCount = 1, maxOperandCount = 1)

        set.addOperatorFunction("min", ::funcMin,
                ".min", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = null)

        set.addOperatorFunction("max", ::funcMax,
                ".max", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = null)

        set.addOperatorFunction("avg", ::funcAvg,
                ".avg", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = null)

        set.addOperatorFunction("med", ::funcMed,
                ".med", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = null)

        set.addOperatorFunction("count", ::funcCount,
                ".count", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = null)

        set.addFunction("abs", ::funcAbs,
                minOperandCount = 1, maxOperandCount = 1)

        set.addSequenceFunction("if", ::funcIf,
                minOperandCount = 2, maxOperandCount = 3)

        set.addComparisonOperatorFunction("eq", ::funcEq,
                "==", CalcAssociativity.Left, precedence = 15,
                minOperandCount = 1, maxOperandCount = null, isCommutative = true,
                binaryAlias = "is")

        set.addComparisonOperatorFunction("neq", ::funcNeq,
                "!=", CalcAssociativity.Left, precedence = 15,
                minOperandCount = 1, maxOperandCount = null, isCommutative = true,
                binaryAlias = "<>")

        set.addComparisonOperatorFunction("gt", ::funcGt,
                ">", CalcAssociativity.Left, precedence = 15,
                minOperandCount = 1, maxOperandCount = null)

        set.addComparisonOperatorFunction("gteq", ::funcGteq,
                ">=", CalcAssociativity.Left, precedence = 15,
                minOperandCount = 1, maxOperandCount = null)

        set.addComparisonOperatorFunction("lt", ::funcLt,
                "<", CalcAssociativity.Left, precedence = 15,
                minOperandCount = 1, maxOperandCount = null)

        set.addComparisonOperatorFunction("lteq", ::funcLteq,
                "<=", CalcAssociativity.Left, precedence = 15,
                minOperandCount = 1, maxOperandCount = null)

        set.addParameterlessFunction("pi", ::funcPi)

        set.addParameterlessFunction("e", ::funcE)

        set.addSequenceOperatorFunction("range", ::funcRange,
                "..", CalcAssociativity.Left, precedence = 3,
                minOperandCount = 2, maxOperandCount = 3)

        set.addSequenceOperatorFunction("map", ::funcMap,
                ".map", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = null)

        set.addSequenceOperatorFunction("flatmap", ::funcFlatMap,
                ".flatmap", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = null,
                unaryAlias = "fmap", binaryAlias = ".fmap")

        set.addSequenceOperatorFunction("filter", ::funcFilter,
                ".filter", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = null)

        set.addSequenceOperatorFunction("skipWhile", ::funcSkipWhile,
            ".skipWhile", CalcAssociativity.Left, precedence = 80,
            minOperandCount = 2, maxOperandCount = null,
            binaryAlias = ".dropWhile", unaryAlias = "dropWhile")

        set.addSequenceOperatorFunction("takeWhile", ::funcTakeWhile,
            ".takeWhile", CalcAssociativity.Left, precedence = 80,
            minOperandCount = 2, maxOperandCount = null)

        set.addSequenceOperatorFunction("union", ::funcUnion,
            ".union", CalcAssociativity.Left, precedence = 80,
            minOperandCount = 2, maxOperandCount = null)

        set.addOperatorFunction("foreach", ::funcForeach,
                ".foreach", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = null)

        set.addFunction("while", ::funcWhile,
                minOperandCount = 2, maxOperandCount = 2)

        set.addFunction("dowhile", ::funcDoWhile,
            minOperandCount = 2, maxOperandCount = 2)

        set.addSequenceFunction("while.map", ::funcWhileMap,
            minOperandCount = 2, maxOperandCount = 2)

        set.addSequenceFunction("while.flatmap", ::funcWhileFlatMap,
            minOperandCount = 2, maxOperandCount = 2,
            unaryAlias = "while.fmap")

        set.addOperatorFunction("any", ::funcAny,
            ".any", CalcAssociativity.Left, precedence = 80,
            minOperandCount = 2, maxOperandCount = null)

        set.addOperatorFunction("all", ::funcAll,
            ".all", CalcAssociativity.Left, precedence = 80,
            minOperandCount = 2, maxOperandCount = null)

        set.addOperatorFunction("reduce", ::funcReduce,
                ".reduce", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = null)

        set.addOperatorFunction("fold", ::funcFold,
                ".fold", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 3, maxOperandCount = null)

        set.addSequenceOperatorFunction("distinct", ::funcDistinct,
                ".distinct", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = null)

        set.addSequenceOperatorFunction("sortAsc", ::funcSortAsc,
                ".sortAsc", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = null,
                unaryAlias = "sort", binaryAlias = ".sort")

        set.addSequenceOperatorFunction("sortDesc", ::funcSortDesc,
                ".sortDesc", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = null)

        set.addSequenceOperatorFunction("sortByAsc", ::funcSortByAsc,
                ".sortByAsc", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = null,
                unaryAlias = "sortBy", binaryAlias = ".sortBy")

        set.addSequenceOperatorFunction("sortByDesc", ::funcSortByDesc,
                ".sortByDesc", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = null)

        set.addSequenceOperatorFunction("group", ::funcGroup,
                ".group", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = null)

        set.addSequenceOperatorFunction("groupBy", ::funcGroupBy,
                ".groupBy", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = null)

        set.addOperatorFunction("groupKey", ::funcGroupKey,
                null, CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = null,
                unaryPostAlias = ".groupKey")

        set.addSequenceOperatorFunction("skip", ::funcSkip,
                ".skip", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = null)

        set.addSequenceOperatorFunction("limit", ::funcLimit,
                ".limit", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = null,
                unaryAlias = "take", binaryAlias = ".take")

        set.addOperatorFunction("elementAt", ::funcElementAt,
                ".elementAt", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = null)

        set.addOperatorFunction("first", ::funcFirst,
                ".first", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = null)

        set.addOperatorFunction("last", ::funcLast,
                ".last", CalcAssociativity.Left, precedence = 0,
                minOperandCount = 1, maxOperandCount = null)

        set.addSequenceOperatorFunction("entries.map", ::funcEntriesMap,
                ".entries.map", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = 2)

        set.addSequenceOperatorFunction("entries.flatmap", ::funcEntriesFlatMap,
                ".entries.flatmap", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = 2,
                unaryAlias = "entries.fmap", binaryAlias = ".entries.fmap")

        set.addSequenceOperatorFunction("zip.map", ::funcZipMap,
                ".zip.map", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 3, maxOperandCount = null)

        set.addSequenceOperatorFunction("zip.flatmap", ::funcZipFlatMap,
                ".zip.flatmap", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 3, maxOperandCount = null,
                unaryAlias = "zip.fmap", binaryAlias = ".zip.fmap")

        set.addSequenceFunction("sequence", ::funcSequence,
                minOperandCount = 0, maxOperandCount = null,
                unaryAlias = "seq")

        set.addOperatorFunction("indexOf", ::funcIndexOf,
                ".indexOf", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = 3)

        set.addOperatorFunction("replace", ::funcReplace,
                ".replace", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 3, maxOperandCount = 3, returnsRef = true)

        set.addOperatorFunction("uppercase", ::funcUppercase,
                null, CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = 1, returnsRef = true,
                unaryPostAlias = ".uppercase")

        set.addOperatorFunction("lowercase", ::funcLowercase,
                null, CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = 1, returnsRef = true,
                unaryPostAlias = ".lowercase")

        set.addOperatorFunction("substring", ::funcSubstring,
                ".substring", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 2, maxOperandCount = 3, returnsRef = true)

        set.addSequenceOperatorFunction("split", ::funcSplit,
                ".split", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = 5)

        set.addFunction("string.join", ::funcStringJoin,
                minOperandCount = 1, maxOperandCount = null, returnsRef = true)

        set.addFunction("string.format", ::funcStringFormat,
                minOperandCount = 1, maxOperandCount = null, returnsRef = true)

        set.addOperatorFunction("toString", ::funcToString,
                ".toString", CalcAssociativity.Left, precedence = 80,
                minOperandCount = 1, maxOperandCount = 2, returnsRef = true)

        set.addOperatorFunction("toNumber", ::funcToNumber,
            null, CalcAssociativity.Left, precedence = 80,
            minOperandCount = 1, maxOperandCount = 1,
            unaryPostAlias = ".toNumber")

        set.addParameterlessFunction("string.newLine", ::funcStringNewLine,
            returnsRef = true)

        set.addParameterlessFunction("in.readLine", ::funcInReadLine,
            returnsRef = true)

        set.addParameterlessSequenceFunction("in.readLines", ::funcInReadLines,
            returnsRef = true)

        set.addFunction("out.write", ::funcOutWrite,
            minOperandCount = 1, maxOperandCount = null)

        set.addFunction("out.writeAll", ::funcOutWriteAll,
            minOperandCount = 1, maxOperandCount = null)

        set.addFunction("out.writeLine", ::funcOutWriteLine,
            minOperandCount = 1, maxOperandCount = 1)

        set.addFunction("out.writeLines", ::funcOutWriteLines,
            minOperandCount = 1, maxOperandCount = null)

        return set
    }

    private fun funcSemicolon(context: CalcFunctionContext) : Sequence<Double> {
        return sequence {
            val iterator = context.operands().iterator()
            val skipIterator = context.operands(true).iterator()

            while (skipIterator.hasNext()) {
                skipIterator.next()
            }

            while (iterator.hasNext()) {
                yield(iterator.next())
            }
        }
    }

    private fun funcReturn(context: CalcFunctionContext) : Double {
        context.functions().returnFunction(context.operands().toList())
        return NULL
    }

    private fun funcBreak(context: CalcFunctionContext) : Double {
        context.functions().returnFunctionWithBreakLoop()
        return NULL
    }

    private fun funcYieldBreak(context: CalcFunctionContext) : Double {
        context.functions().returnFunctionWithBreakLoop(context.operands().toList())
        return NULL
    }

    private fun funcContinue(context: CalcFunctionContext) : Double {
        context.functions().returnFunctionWithContinueLoop()
        return NULL
    }

    private fun funcYieldContinue(context: CalcFunctionContext) : Double {
        context.functions().returnFunctionWithContinueLoop(context.operands().toList())
        return NULL
    }

    private fun funcKeys(context: CalcFunctionContext): Sequence<Double> {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return emptySequence()
        val collectionRef = iterator.nextRef()
        val collection = context.data().getCollectionByRef(collectionRef) ?: return emptySequence()

        return collection.keys()
    }

    private fun funcValues(context: CalcFunctionContext): Sequence<Double> {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return emptySequence()
        val collectionRef = iterator.nextRef()
        val collection = context.data().getCollectionByRef(collectionRef) ?: return emptySequence()

        return collection.values()
    }

    private fun funcArray(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()
        if (!iterator.hasNext()) return NULL

        val size = getInt(iterator.next()) ?: 0
        val value = if (iterator.hasNext()) iterator.next() else 0.0
        return context.data().newCollectionRef(CalcCollectionArray(size, value))
    }

    private fun funcList(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        val list = ArrayList<Double>()
        while (iterator.hasNext()) {
            list.add(iterator.next())
        }

        return context.data().newCollectionRef(CalcCollectionList(list))
    }

    private fun funcAdd(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val collectionRef = iterator.nextRef()
        val collection = context.data().getCollectionByRef(collectionRef) ?: return NULL

        if (collection is CalcCollectionList) {
            while (iterator.hasNext()) {
                collection.add(iterator.next())
            }
        }

        return collectionRef
    }

    private fun funcHashMap(context: CalcFunctionContext) : Double {
        return context.data().newCollectionRef(CalcCollectionMap())
    }

    private fun funcGet(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val collectionRef = iterator.nextRef()
        var collection = context.data().getCollectionByRef(collectionRef) ?: return NULL

        if (!iterator.hasNext()) return NULL
        var key = iterator.next()

        while (iterator.hasNext()) {
            val innerCollectionRef = collection.get(key) ?: NULL
            collection =  context.data().getCollectionByRef(innerCollectionRef) ?: return NULL
            key = iterator.next()
        }

        return collection.get(key) ?: NULL
    }

    private fun funcSet(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val collectionRef = iterator.nextRef()
        val collection = context.data().getCollectionByRef(collectionRef) ?: return NULL

        if (!iterator.hasNext()) return NULL
        val key = iterator.next()

        if (!iterator.hasNext()) return NULL
        val value = iterator.next()

        collection.set(key, value)
        return collectionRef
    }

    private fun funcSize(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val collectionRef = iterator.nextRef()
        val collection = context.data().getCollectionByRef(collectionRef) ?: return NULL

        return collection.size()?.toDouble() ?: NULL
    }

    private fun funcEquals(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val collectionRef = iterator.nextRef()
        val collection = context.data().getCollectionByRef(collectionRef) ?: return NULL

        while (iterator.hasNext()) {
            val checkCollectionRef = iterator.nextRef()
            val checkCollection = context.data().getCollectionByRef(checkCollectionRef) ?: return NULL
            if (!collection.equalsCollection(checkCollection)) return FALSE
        }

        return TRUE
    }

    private fun funcFind(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val collectionRef = iterator.nextRef()
        val collection = context.data().getCollectionByRef(collectionRef) ?: return NULL

        if (!iterator.hasNext()) return NULL
        val findingValue = iterator.next()
        val beginKey = if (iterator.hasNext()) iterator.next() else null

        return collection.find(findingValue, beginKey) ?: NULL
    }

    private fun funcFindFrom(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val collectionRef = iterator.nextRef()
        val collection = context.data().getCollectionByRef(collectionRef) ?: return NULL

        if (!iterator.hasNext()) return NULL
        val findingValue = iterator.next()
        if (!iterator.hasNext()) return NULL
        val beginKey = iterator.next()

        return collection.find(findingValue, beginKey) ?: NULL
    }

    private fun funcSlice(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val collectionRef = iterator.nextRef()
        val collection = context.data().getCollectionByRef(collectionRef) ?: return NULL

        if (!iterator.hasNext()) return NULL
        val sliceFrom = iterator.next()
        val sliceTo = if (iterator.hasNext()) iterator.next() else null

        val newCollection = collection.slice(sliceFrom, sliceTo) ?: return NULL
        return context.data().newCollectionRef(newCollection)
    }

    private fun funcSliceFrom(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val collectionRef = iterator.nextRef()
        val collection = context.data().getCollectionByRef(collectionRef) ?: return NULL

        if (!iterator.hasNext()) return NULL
        val sliceFrom = iterator.next()

        val newCollection = collection.slice(sliceFrom, null) ?: return NULL
        return context.data().newCollectionRef(newCollection)
    }

    private fun funcSliceTo(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val collectionRef = iterator.nextRef()
        val collection = context.data().getCollectionByRef(collectionRef) ?: return NULL

        if (!iterator.hasNext()) return NULL
        val sliceTo = iterator.next()

        val newCollection = collection.slice(null, sliceTo) ?: return NULL
        return context.data().newCollectionRef(newCollection)
    }

    private fun funcConcat(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val collectionRef = iterator.nextRef()
        val collection = context.data().getCollectionByRef(collectionRef) ?: return NULL

        val concatCollections = ArrayList<CalcCollection>()
        while (iterator.hasNext()) {
            val concatCollectionRef = iterator.nextRef()
            val concatCollection = context.data().getCollectionByRef(concatCollectionRef) ?: continue
            concatCollections.add(concatCollection)
        }

        val newCollection = collection.concat(concatCollections) ?: return NULL
        return context.data().newCollectionRef(newCollection)
    }

    private fun funcLast(context: CalcFunctionContext) : Double {
        var result = NULL
        for (operand in context.operands()) {
            result = operand
        }
        return result
    }

    private fun funcFirst(context: CalcFunctionContext) : Double {
        return context.operands().firstOperand() ?: NULL
    }

    private fun funcAssign(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val variableRef = iterator.nextRef()

        if (!iterator.hasNext()) return NULL
        val variableValue = iterator.next()

        if (!iterator.hasNext()) {
            return context.data().setValueByRef(variableRef, variableValue) ?: NULL
        }

        val data = ArrayList<Double>()
        data.add(variableValue)
        do {
            data.add(iterator.next())
        } while (iterator.hasNext())

        val collectionRef = context.data().newCollectionRef(CalcCollectionList(data))
        context.data().setValueByRef(variableRef, collectionRef)
        return variableRef
    }

    private fun funcLambda(context: CalcFunctionContext) : Sequence<Double> {
        return sequence {
            val lambdaValues = context.functions().getLambdaValues()?.iterator()
            val skipIterator = context.operands(true).iterator()

            while (skipIterator.hasNext()) {
                val variableRef = skipIterator.nextRef()
                val lambdaValue = if (lambdaValues?.hasNext() == true) lambdaValues.next() else 0.0
                context.data().setValueByRef(variableRef, lambdaValue)
            }

            val iterator = context.operands().iterator()
            while (iterator.hasNext()) {
                yield(iterator.next())
            }
        }
    }

    private fun funcIt(context: CalcFunctionContext) : Double {
        return context.functions().getLambdaValues()?.firstOrNull() ?: NULL
    }

    private fun funcFunction(context: CalcFunctionContext) : Double{
        val skipIterator = context.operands(true).iterator()

        val funcParams = ArrayList<Double>()
        while (skipIterator.hasNext()) {
            val variableRef = skipIterator.nextRef()
            funcParams.add(variableRef)
        }

        return context.functions().newFunctionRef(funcParams) ?: NULL
    }

    private fun funcCarry(context: CalcFunctionContext) : Double{
        val iterator = context.operands().iterator()
        if (!iterator.hasNext()) return NULL

        val functionRef = iterator.nextRef()
        if (!iterator.hasNext()) return functionRef

        val paramMap = HashMap<Double, Double>()
        do {
            val paramRef = iterator.nextRef()
            if (!iterator.hasNext()) break
            val paramValue = iterator.next()
            paramMap[paramRef] = paramValue
        } while (iterator.hasNext())

        return context.functions().carryFunctionRef(functionRef, paramMap) ?: NULL
    }

    private fun funcCall(context: CalcFunctionContext) : Sequence<Double>{
        return sequence {
            val iterator = context.operands().iterator()
            if (!iterator.hasNext()) return@sequence

            val functionRef = iterator.nextRef()
            val function = context.functions().getFunction(functionRef) ?: return@sequence

            val params = ArrayList<Double>()
            while (iterator.hasNext()) {
                params.add(iterator.next())
            }

            val callContext = function.callFunction(params)
            for (result in callContext.results()) {
                yield(result)
                if (callContext.isBroken()) {
                    break
                }
            }
        }
    }

    private fun funcCallLambda(context: CalcFunctionContext) : Sequence<Double>{
        return sequence {
            val iterator = context.operands().iterator()

            if (!iterator.hasNext()) return@sequence
            val functionRef = iterator.nextRef()
            val function = context.functions().getFunction(functionRef) ?: return@sequence
            val lambdaValues = context.functions().getLambdaValues() ?: emptyList()

            val callContext = function.callFunction(lambdaValues)
            for (result in callContext.results()) {
                yield(result)
                if (callContext.isBroken()) {
                    break
                }
            }
        }
    }

    private fun funcClass(context: CalcFunctionContext) : Double{
        val iterator = context.operands().iterator()

        val fields = ArrayList<Double>()
        while (iterator.hasNext()) {
            fields.add(iterator.nextRef())
        }

        return context.classes().newClassRef(fields)
    }

    private fun funcExtends(context: CalcFunctionContext) : Double{
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val classRef = iterator.nextRef()

        val fields = ArrayList<Double>()
        while (iterator.hasNext()) {
            fields.add(iterator.nextRef())
        }

        return context.classes().extendClassRef(classRef, fields) ?: NULL
    }

    private fun funcMethod(context: CalcFunctionContext) : Double{
        val skipIterator = context.operands(true).iterator()

        if (!skipIterator.hasNext()) return NULL
        val memberRef = skipIterator.nextRef()

        val funcParams = ArrayList<Double>()
        while (skipIterator.hasNext()) {
            val variableRef = skipIterator.nextRef()
            funcParams.add(variableRef)
        }

        val functionRef = context.functions().newFunctionRef(funcParams) ?: return NULL
        return context.classes().newClassMethodRef(memberRef, functionRef) ?: NULL
    }

    private fun funcConstructor(context: CalcFunctionContext) : Double{
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val classRef = iterator.nextRef()

        if (!iterator.hasNext()) return NULL
        val functionRef = context.functions().newFunctionRef(emptyList()) ?: return NULL

        return context.classes().newClassConstructorRef(classRef, functionRef) ?: NULL
    }

    private fun funcMethodRef(context: CalcFunctionContext) : Double{
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val memberRef = iterator.nextRef()

        if (!iterator.hasNext()) return NULL
        val functionRef = iterator.nextRef()

        return context.classes().newClassMethodRef(memberRef, functionRef) ?: NULL
    }

    private fun funcNew(context: CalcFunctionContext) : Double{
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val classRef = iterator.nextRef()

        val fieldValues = ArrayList<Double>()
        while (iterator.hasNext()) {
            fieldValues.add(iterator.next())
        }

        return context.classes().newObjectRef(classRef, fieldValues) ?: NULL
    }

    private fun funcMember(context: CalcFunctionContext) : Double{
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val objectRef = iterator.nextRef()

        if (!iterator.hasNext()) return NULL
        val memberRef = iterator.nextRef()

        return context.classes().getMemberRef(objectRef, memberRef) ?: NULL
    }

    private fun funcThis(context: CalcFunctionContext) : Double{
        return context.classes().getThisObjectRef()
    }

    private fun funcGlobal(context: CalcFunctionContext) : Double{
        return context.data().getGlobalContextRef()
    }

    private fun funcLocal(context: CalcFunctionContext) : Double{
        return context.data().getLocalContextRef()
    }

    private fun funcArgs(context: CalcFunctionContext) : Double{
        return context.data().getArgsRef()
    }

    private fun funcStringArgs(context: CalcFunctionContext) : Double{
        return context.data().getStringArgsRef()
    }

    private fun funcCheck(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()
        if (!iterator.hasNext()) return NULL

        val operand = iterator.next()
        if (isNotNull(operand)) {
            return operand
        }

        if (!iterator.hasNext()) return NULL
        return iterator.next()
    }

    private fun funcFloor(context: CalcFunctionContext) : Double {
        val operand = context.operands().firstOperand()
        return if (operand != null) floor(operand) else NULL
    }

    private fun funcCeiling(context: CalcFunctionContext) : Double {
        val operand = context.operands().firstOperand()
        return if (operand != null) floor(operand) else NULL
    }

    private fun funcRound(context: CalcFunctionContext) : Double {
        val operand = context.operands().firstOperand()
        return if (operand != null) floor(operand) else NULL
    }

    private fun funcNot(context: CalcFunctionContext) : Double {
        return if (isTrue(context.operands().firstOperand())) FALSE else TRUE
    }

    private fun funcAnd(context: CalcFunctionContext): Double {
        for (operand in context.operands()) {
            if (isFalse(operand)) return FALSE
        }

        return TRUE
    }

    private fun funcOr(context: CalcFunctionContext): Double {
        for (operand in context.operands()) {
            if (isTrue(operand)) return TRUE
        }

        return FALSE
    }

    private fun funcXor(context: CalcFunctionContext): Double {
        var hasTrue = false

        for (operand in context.operands()) {
            if (isTrue(operand)) {
                if (hasTrue) return FALSE

                hasTrue = true
            }
        }
        return if (hasTrue) TRUE else FALSE
    }

    private fun funcSum(context: CalcFunctionContext) : Double {
        return context.operands().sum()
    }

    private fun funcSub(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()
        if (!iterator.hasNext()) return NULL

        var result = iterator.next()
        if (!iterator.hasNext()) {
            return - result
        }

        do {
            result -= iterator.next()
        } while(iterator.hasNext())
        return result
    }

    private fun funcMul(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()
        if (!iterator.hasNext()) return NULL

        var result = iterator.next()

        while(iterator.hasNext()) {
            result *= iterator.next()
        }

        return result
    }

    private fun funcDiv(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()
        if (!iterator.hasNext()) return NULL

        var result = iterator.next()

        while(iterator.hasNext()) {
            result /= iterator.next()
        }

        return result
    }

    private fun funcMod(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()
        if (!iterator.hasNext()) return NULL

        var result = iterator.next()

        while(iterator.hasNext()) {
            result %= iterator.next()
        }

        return result
    }

    private fun funcPow(context: CalcFunctionContext) : Double {
        val operandArray = context.operands().toList()
        val count = operandArray.count()
        if (count < 2) return NULL

        var result = operandArray[count - 2].pow(operandArray[count - 1])

        for (i in count - 3 downTo 0) {
            result = operandArray[i].pow(result)
        }

        return result
    }

    private fun funcSqr(context: CalcFunctionContext) : Double {
        val operand = context.operands().firstOperand() ?: return NULL
        return operand.pow(2)
    }

    private fun funcSqrt(context: CalcFunctionContext) : Double {
        val operand = context.operands().firstOperand() ?: return NULL
        return sqrt(operand)
    }

    private fun funcMin(context: CalcFunctionContext) : Double {
        return context.operands().min() ?: NULL
    }

    private fun funcMax(context: CalcFunctionContext) : Double {
        return context.operands().max() ?: NULL
    }

    private fun funcAvg(context: CalcFunctionContext) : Double {
        return context.operands().average()
    }

    private fun funcMed(context: CalcFunctionContext) : Double {
        val operandArray = context.operands().toList()
        val count = operandArray.count()
        val avg = operandArray.average()

        var minDelta = Double.MAX_VALUE
        var minDeltaIndex = 0
        for (i in 0 until count) {
            val delta = abs(operandArray[i] - avg)
            if (delta < minDelta) {
                minDelta = delta
                minDeltaIndex = i
            }
        }

        if (count % 2 == 1) return operandArray[minDeltaIndex]

        minDelta = Double.MAX_VALUE
        var minDeltaIndex2 = 0
        for (i in 0..count) {
            if (i == minDeltaIndex) continue

            val delta = abs(operandArray[i] - avg)
            if (delta < minDelta) {
                minDelta = delta
                minDeltaIndex2 = i
            }
        }

        return (operandArray[minDeltaIndex] + operandArray[minDeltaIndex2]) / 2.0
    }

    private fun funcAbs(context: CalcFunctionContext) : Double {
        val operand = context.operands().firstOperand() ?: return NULL
        return abs(operand)
    }

    private fun funcIf(context: CalcFunctionContext) : Sequence<Double> {
        return sequence {
            val iterator = context.operands().iterator()

            if (!iterator.hasNext()) return@sequence
            val condition = iterator.next()

            if (isTrue(condition)) {
                var skipElseIterator = context.operands(true).iterator()
                if (!skipElseIterator.hasNextDirect()) {
                    skipElseIterator = iterator // no else found
                }
                while (skipElseIterator.hasNext()) {
                    yield(skipElseIterator.next())
                }
            } else {
                if (!iterator.skipDirect()) return@sequence
                while (iterator.hasNext()) {
                    yield(iterator.next())
                }
            }
        }
    }

    private fun funcEq(context: CalcFunctionContext) : Double {
        var lastOperand: Double? = null

        for (operand in context.operands()) {
            if (lastOperand == null) {
                val lastComparisonOperand = context.comparison().getLastBinaryComparisonValue()
                lastOperand = if (lastComparisonOperand != null) {
                    if (isFalse(operand)) return FALSE
                    lastComparisonOperand
                } else {
                    operand
                }
                continue
            }

            if (operand != lastOperand && !(isNull(operand) && isNull(lastOperand))) return FALSE

            lastOperand = operand
        }

        context.comparison().setLastBinaryComparisonValue(lastOperand)

        return if (lastOperand != null) TRUE else FALSE
    }

    private fun funcNeq(context: CalcFunctionContext) : Double {
        var lastOperand: Double? = null

        for (operand in context.operands()) {
            if (lastOperand == null) {
                val lastComparisonOperand = context.comparison().getLastBinaryComparisonValue()
                lastOperand = if (lastComparisonOperand != null) {
                    if (isFalse(operand)) return FALSE
                    lastComparisonOperand
                } else {
                    operand
                }
                continue
            }

            if (operand == lastOperand) return FALSE
            if (isNull(operand) && isNull(lastOperand)) return FALSE

            lastOperand = operand
        }

        context.comparison().setLastBinaryComparisonValue(lastOperand)

        return if (lastOperand != null) TRUE else FALSE
    }

    private fun funcGt(context: CalcFunctionContext) : Double {
        var lastOperand: Double? = null

        for (operand in context.operands()) {
            if (lastOperand == null) {
                val lastComparisonOperand = context.comparison().getLastBinaryComparisonValue()
                lastOperand = if (lastComparisonOperand != null) {
                    if (isFalse(operand)) return FALSE
                    lastComparisonOperand
                } else {
                    operand
                }
                continue
            }

            if (lastOperand <= operand) return FALSE

            lastOperand = operand
        }

        context.comparison().setLastBinaryComparisonValue(lastOperand)

        return if (lastOperand != null) TRUE else FALSE
    }

    private fun funcGteq(context: CalcFunctionContext) : Double {
        var lastOperand: Double? = null

        for (operand in context.operands()) {
            if (lastOperand == null) {
                val lastComparisonOperand = context.comparison().getLastBinaryComparisonValue()
                lastOperand = if (lastComparisonOperand != null) {
                    if (isFalse(operand)) return FALSE
                    lastComparisonOperand
                } else {
                    operand
                }
                continue
            }

            if (lastOperand < operand) return FALSE

            lastOperand = operand
        }

        context.comparison().setLastBinaryComparisonValue(lastOperand)

        return if (lastOperand != null) TRUE else FALSE
    }

    private fun funcLt(context: CalcFunctionContext) : Double {
        var lastOperand: Double? = null

        for (operand in context.operands()) {
            if (lastOperand == null) {
                val lastComparisonOperand = context.comparison().getLastBinaryComparisonValue()
                lastOperand = if (lastComparisonOperand != null) {
                    if (isFalse(operand)) return FALSE
                    lastComparisonOperand
                } else {
                    operand
                }
                continue
            }

            if (lastOperand >= operand) return FALSE

            lastOperand = operand
        }

        context.comparison().setLastBinaryComparisonValue(lastOperand)

        return if (lastOperand != null) TRUE else FALSE
    }

    private fun funcLteq(context: CalcFunctionContext) : Double
    {
        var lastOperand: Double? = null

        for (operand in context.operands()) {
            if (lastOperand == null) {
                val lastComparisonOperand = context.comparison().getLastBinaryComparisonValue()
                lastOperand = if (lastComparisonOperand != null) {
                    if (isFalse(operand)) return FALSE
                    lastComparisonOperand
                } else {
                    operand
                }
                continue
            }

            if (lastOperand > operand) return FALSE

            lastOperand = operand
        }

        context.comparison().setLastBinaryComparisonValue(lastOperand)

        return if (lastOperand != null) TRUE else FALSE
    }

    @Suppress("UNUSED_PARAMETER")
    private fun funcPi(context: CalcFunctionContext) : Double {
        return Math.PI
    }

    @Suppress("UNUSED_PARAMETER")
    private fun funcE(context: CalcFunctionContext) : Double {
        return Math.E
    }

    private fun funcRange(context: CalcFunctionContext) : Sequence<Double> {
        return sequence {
            val iterator = context.operands().iterator()

            if (!iterator.hasNext()) return@sequence
            val rangeBegin = getInt(iterator.next()) ?: 0

            if (!iterator.hasNext()) return@sequence
            val rangeEnd = getInt(iterator.next()) ?: 0

            val stepValue = if (iterator.hasNext())
                getInt(iterator.next()) ?: 1
            else 1

            if (rangeBegin <= rangeEnd) {
                for (value in rangeBegin..rangeEnd step stepValue) yield(value.toDouble())
            } else {
                for (value in rangeEnd downTo rangeBegin step stepValue) yield(value.toDouble())
            }
        }
    }

    private fun funcMap(context: CalcFunctionContext) : Sequence<Double> {
        return sequence{
            val lambda = context.functions().createLambda() ?: return@sequence
            val iterator = lambda.operands().iterator()

            while (iterator.hasNext()) {
                val rawValue = iterator.next()
                val callContext = lambda.callFunction(rawValue)
                val singleValue = getSingleValue(context, callContext.results())
                yield(singleValue)
                if (callContext.isBroken()) {
                    return@sequence
                }
            }
        }
    }

    private fun funcFlatMap(context: CalcFunctionContext) : Sequence<Double> {
        return sequence{
            val lambda = context.functions().createLambda() ?: return@sequence
            val iterator = lambda.operands().iterator()

            while (iterator.hasNext()) {
                val rawValue = iterator.next()
                val callContext = lambda.callFunction(rawValue)
                for (result in callContext.results()) {
                    if (isValidDouble(result)) {
                        yield(result)
                    }
                }
                if (callContext.isBroken()) {
                    return@sequence
                }
            }
        }
    }

    private fun funcFilter(context: CalcFunctionContext) : Sequence<Double> {
        return sequence{
            val lambda = context.functions().createLambda() ?: return@sequence
            val iterator = lambda.operands().iterator()
            while (iterator.hasNext()) {
                val checkingOperand = iterator.next()
                val callContext = lambda.callFunction(checkingOperand)
                val filterResult = callContext.results().firstOrNull()
                if (isTrue(filterResult)) {
                    yield(checkingOperand)
                }
                if (callContext.isBroken()) {
                    return@sequence
                }
            }
        }
    }

    private fun funcSkipWhile(context: CalcFunctionContext) : Sequence<Double> {
        return sequence{
            val lambda = context.functions().createLambda() ?: return@sequence
            val iterator = lambda.operands().iterator()
            while (iterator.hasNext()) {
                val checkingOperand = iterator.next()
                val callContext = lambda.callFunction(checkingOperand)
                val filterResult = callContext.results().firstOrNull()
                if (!isTrue(filterResult)) {
                    yield(checkingOperand)
                    break
                }
                if (callContext.isBroken()) {
                    return@sequence
                }
            }
            while (iterator.hasNext()) {
                yield(iterator.next())
            }
        }
    }

    private fun funcTakeWhile(context: CalcFunctionContext) : Sequence<Double> {
        return sequence{
            val lambda = context.functions().createLambda() ?: return@sequence
            val iterator = lambda.operands().iterator()
            while (iterator.hasNext()) {
                val checkingOperand = iterator.next()
                val callContext = lambda.callFunction(checkingOperand)
                val filterResult = callContext.results().firstOrNull()
                if (isTrue(filterResult)) {
                    yield(checkingOperand)
                } else {
                    return@sequence
                }
                if (callContext.isBroken()) {
                    return@sequence
                }
            }
        }
    }

    private fun funcUnion(context: CalcFunctionContext) : Sequence<Double> {
        return sequence{
            val iterator = context.operands().iterator()
            while (iterator.hasNext()) {
                yield(iterator.next())
            }
        }
    }

    private fun funcForeach(context: CalcFunctionContext) : Double {
        val lambda = context.functions().createLambda() ?: return NULL
        val iterator = lambda.operands().iterator()
        var result = NULL

        while (iterator.hasNext()) {
            val item = iterator.next()
            val callContext = lambda.callFunction(item)
            val iterationResult = callContext.results().lastOrNull() ?: NULL
            if (isValidDouble(iterationResult)) {
                result = iterationResult
            }
            if (callContext.isBroken()) {
                break
            }
        }

        return result
    }

    private fun funcWhile(context: CalcFunctionContext) : Double {
        val lambda = context.functions().createLambda() ?: return NULL
        var result = NULL

        while (isTrue(context.operands().firstOperand())) {
            val callContext = lambda.callFunction()
            val iterationResult = callContext.results().lastOrNull() ?: NULL
            if (isValidDouble(iterationResult)) {
                result = iterationResult
            }
            if (callContext.isBroken()) {
                break
            }
        }
        return result
    }

    private fun funcDoWhile(context: CalcFunctionContext) : Double {
        val lambda = context.functions().createLambda() ?: return NULL
        var result = NULL

        do {
            val callContext = lambda.callFunction()
            val iterationResult = callContext.results().lastOrNull() ?: NULL
            if (isValidDouble(iterationResult)) {
                result = iterationResult
            }
            if (callContext.isBroken()) {
                break
            }
        } while (isTrue(context.operands().firstOperand()))
        return result
    }

    private fun funcWhileMap(context: CalcFunctionContext) : Sequence<Double> {
        return sequence {
            val lambda = context.functions().createLambda() ?: return@sequence

            while (isTrue(context.operands().firstOperand())) {
                val callContext = lambda.callFunction()
                val singleValue = getSingleValue(context, callContext.results())
                yield(singleValue)
                if (callContext.isBroken()) {
                    return@sequence
                }
            }
        }
    }

    private fun funcWhileFlatMap(context: CalcFunctionContext) : Sequence<Double> {
        return sequence {
            val lambda = context.functions().createLambda() ?: return@sequence

            while (isTrue(context.operands().firstOperand())) {
                val callContext = lambda.callFunction()
                for (result in callContext.results()) {
                    if (isValidDouble(result)) {
                        yield(result)
                    }
                }
                if (callContext.isBroken()) {
                    return@sequence
                }
            }
        }
    }

    private fun funcAny(context: CalcFunctionContext) : Double {
        val lambda = context.functions().createLambda() ?: return FALSE
        val iterator = lambda.operands().iterator()

        while (iterator.hasNext()) {
            val checkingValue = iterator.next()
            val callContext = lambda.callFunction(checkingValue)
            val checkResult = callContext.results().lastOrNull() ?: continue
            if (isTrue(checkResult)) {
                return TRUE
            }
            if (callContext.isBroken()) {
                break
            }
        }

        return FALSE
    }

    private fun funcAll(context: CalcFunctionContext) : Double {
        val lambda = context.functions().createLambda() ?: return FALSE
        val iterator = lambda.operands().iterator()
        if (!iterator.hasNext()) return FALSE

        do {
            val checkingValue = iterator.next()
            val callContext = lambda.callFunction(checkingValue)
            val checkResult = callContext.results().lastOrNull() ?: continue
            if (!isTrue(checkResult)) {
                return FALSE
            }
            if (callContext.isBroken()) {
                break
            }
        } while (iterator.hasNext())

        return TRUE
    }

    private fun funcReduce(context: CalcFunctionContext) : Double {
        val lambda = context.functions().createLambda() ?: return NULL
        val iterator = lambda.operands().iterator()
        if (!iterator.hasNext()) return NULL

        var accumulator = iterator.next()
        while (iterator.hasNext()) {
            val item = iterator.next()
            val callContext = lambda.callFunction(accumulator, item)
            val singleValue = getSingleValue(context, callContext.results())
            if (isValidDouble(singleValue)) {
                accumulator = singleValue
            }
            if (callContext.isBroken()) {
                break
            }
        }

        return accumulator
    }

    private fun funcFold(context: CalcFunctionContext) : Double {
        val lambda = context.functions().createLambda() ?: return NULL
        val iterator = lambda.operands().iterator()
        if (!iterator.hasNext()) return NULL

        var accumulator = iterator.next()
        while (iterator.hasNext()) {
            val item = iterator.next()
            val callContext = lambda.callFunction(accumulator, item)
            val singleValue = getSingleValue(context, callContext.results())
            if (isValidDouble(singleValue)) {
                accumulator = singleValue
            }
            if (callContext.isBroken()) {
                break
            }
        }

        return accumulator
    }

    private fun funcEntriesMap(context: CalcFunctionContext) : Sequence<Double> {
        return sequence{
            val lambda = context.functions().createLambda() ?: return@sequence
            val iterator = lambda.operands().iterator()

            if (!iterator.hasNext()) return@sequence
            val collectionRef = iterator.nextRef()
            val collection = context.data().getCollectionByRef(collectionRef) ?: return@sequence
            val keys = collection.keys().iterator()
            val values = collection.values().iterator()

            while (keys.hasNext() && values.hasNext()) {
                val key = keys.next()
                val value = values.next()

                val callContext = lambda.callFunction(key, value)
                val singleValue = getSingleValue(context, callContext.results())
                yield(singleValue)
                if (callContext.isBroken()) {
                    return@sequence
                }
            }
        }
    }

    private fun funcEntriesFlatMap(context: CalcFunctionContext) : Sequence<Double> {
        return sequence{
            val lambda = context.functions().createLambda() ?: return@sequence
            val iterator = lambda.operands().iterator()

            if (!iterator.hasNext()) return@sequence
            val collectionRef = iterator.nextRef()
            val collection = context.data().getCollectionByRef(collectionRef) ?: return@sequence
            val keys = collection.keys().iterator()
            val values = collection.values().iterator()

            while (keys.hasNext() && values.hasNext()) {
                val key = keys.next()
                val value = values.next()

                val callContext = lambda.callFunction(key, value)
                for (result in callContext.results()) {
                    if (isValidDouble(result)) {
                        yield(result)
                    }
                }
                if (callContext.isBroken()) {
                    return@sequence
                }
            }
        }
    }

    private fun funcZipMap(context: CalcFunctionContext) : Sequence<Double> {
        return sequence{
            val lambda = context.functions().createLambda() ?: return@sequence
            val iterator = lambda.operands().iterator()

            if (!iterator.hasNext()) return@sequence
            val collections = ArrayList<Iterator<Double>>()
            while (iterator.hasNext()) {
                val collectionRef = iterator.nextRef()
                val collection = context.data().getCollectionByRef(collectionRef) ?: return@sequence
                collections.add(collection.values().iterator())
            }

            while (collections.all { it.hasNext() }) {

                val values = collections.map { it.next() }

                val callContext = lambda.callFunction(values)
                val singleValue = getSingleValue(context, callContext.results())
                yield(singleValue)
                if (callContext.isBroken()) {
                    return@sequence
                }
            }
        }
    }

    private fun funcZipFlatMap(context: CalcFunctionContext) : Sequence<Double> {
        return sequence{
            val lambda = context.functions().createLambda() ?: return@sequence
            val iterator = lambda.operands().iterator()

            if (!iterator.hasNext()) return@sequence
            val collections = ArrayList<Iterator<Double>>()
            while (iterator.hasNext()) {
                val collectionRef = iterator.nextRef()
                val collection = context.data().getCollectionByRef(collectionRef) ?: return@sequence
                collections.add(collection.values().iterator())
            }

            while (collections.all { it.hasNext() }) {

                val values = collections.map { it.next() }

                val callContext = lambda.callFunction(values)
                for (result in callContext.results()) {
                    if (isValidDouble(result)) {
                        yield(result)
                    }
                }
                if (callContext.isBroken()) {
                    return@sequence
                }
            }
        }
    }

    private fun funcSequence(context: CalcFunctionContext) : Sequence<Double> {
        val iterator = context.operands().iterator()

        return sequence {
            while (iterator.hasNext()) {
                yield(iterator.next())
            }
        }
    }

    private fun funcDistinct(context: CalcFunctionContext) : Sequence<Double> {
        return context.operands().distinct()
    }

    private fun funcSortAsc(context: CalcFunctionContext) : Sequence<Double> {
        return context.operands().sorted()
    }

    private fun funcSortDesc(context: CalcFunctionContext) : Sequence<Double> {
        return context.operands().sortedDescending()
    }

    private fun funcSortByAsc(context: CalcFunctionContext) : Sequence<Double> {
        val lambda = context.functions().createLambda() ?: return emptySequence()

        return lambda.operands().sortedBy {
            lambda.callFunction(it).results().firstOrNull()
        }
    }

    private fun funcSortByDesc(context: CalcFunctionContext) : Sequence<Double> {
        val lambda = context.functions().createLambda() ?: return emptySequence()

        return lambda.operands().sortedByDescending {
            lambda.callFunction(it).results().firstOrNull()
        }
    }

    private fun funcGroup(context: CalcFunctionContext) : Sequence<Double> {
        return context.operands().groupBy { it }.entries.asSequence().map {
            context.data().newCollectionRef(CalcCollectionGroup(it.key, it.value))
        }
    }

    private fun funcGroupBy(context: CalcFunctionContext) : Sequence<Double> {
        val lambda = context.functions().createLambda() ?: return emptySequence()

        return lambda.operands().groupBy {
                    lambda.callFunction(it).results().firstOrNull() ?: it
                }.entries.asSequence().map {
                    context.data().newCollectionRef(CalcCollectionGroup(it.key, it.value))
                }
    }

    private fun funcGroupKey(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val collectionRef = iterator.nextRef()
        val collection = context.data().getCollectionByRef(collectionRef) ?: return NULL

        return if (collection is CalcCollectionGroup) {
            collection.groupKey
        } else {
            NULL
        }
    }

    private fun funcCount(context: CalcFunctionContext) : Double {
        return context.operands().count().toDouble()
    }

    private fun funcSkip(context: CalcFunctionContext) : Sequence<Double> {
        return sequence {
            var skipCount = getInt(context.operands().lastOperand()) ?: return@sequence
            val iterator = context.operands(true).iterator()

            while (skipCount > 0 && iterator.hasNext()) {
                iterator.next()
                skipCount--
            }

            while (iterator.hasNext()) {
                yield(iterator.next())
            }
        }
    }

    private fun funcLimit(context: CalcFunctionContext) : Sequence<Double> {
        return sequence {
            var takeCount = getInt(context.operands().lastOperand()) ?: return@sequence
            val iterator = context.operands(true).iterator()

            while (takeCount > 0 && iterator.hasNext()) {
                yield(iterator.next())
                takeCount--
            }
        }
    }

    private fun funcElementAt(context: CalcFunctionContext) : Double {
        val elementIndex = getInt(context.operands().lastOperand()) ?: return NULL
        val iterator = context.operands(true).iterator()

        var index = 0
        while (iterator.hasNext()) {
            val element = iterator.next()
            if (index++ == elementIndex) {
                return element
            }
        }
        return NULL
    }

    private fun funcInc(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val variableRef = iterator.nextRef()
        var variableValue = context.data().getValueByRef(variableRef) ?: return NULL

        val deltaValue = if (iterator.hasNext()) iterator.next() else 1.0
        variableValue += deltaValue

        context.data().setValueByRef(variableRef, variableValue)
        return variableValue
    }

    private fun funcDec(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val variableRef = iterator.nextRef()
        var variableValue = context.data().getValueByRef(variableRef) ?: return NULL

        val deltaValue = if (iterator.hasNext()) iterator.next() else 1.0
        variableValue -= deltaValue

        return context.data().setValueByRef(variableRef, variableValue) ?: NULL
    }

    private fun funcIncPost(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val variableRef = iterator.nextRef()
        val variableValue = context.data().getValueByRef(variableRef) ?: return NULL

        val deltaValue = if (iterator.hasNext()) iterator.next() else 1.0

        context.data().setValueByRef(variableRef, variableValue + deltaValue) ?: return NULL
        return variableValue
    }

    private fun funcDecPost(context: CalcFunctionContext) : Double
    {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val variableRef = iterator.nextRef()
        val variableValue = context.data().getValueByRef(variableRef) ?: return NULL

        val deltaValue = if (iterator.hasNext()) iterator.next() else 1.0

        context.data().setValueByRef(variableRef, variableValue - deltaValue) ?: return NULL
        return variableValue
    }

    private fun funcIndexOf(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val string = context.data().getStringByRef(iterator.nextRef()) ?: return NULL

        if (!iterator.hasNext()) return NULL
        val findingString = context.data().getStringByRef(iterator.nextRef()) ?: return NULL
        val beginIndex = if (iterator.hasNext()) iterator.next() else null

        return string.indexOf(findingString, getInt(beginIndex) ?: 0).toDouble()
    }

    private fun funcReplace(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val string = context.data().getStringByRef(iterator.nextRef()) ?: return NULL

        if (!iterator.hasNext()) return NULL
        val findingString = context.data().getStringByRef(iterator.nextRef()) ?: return NULL

        if (!iterator.hasNext()) return NULL
        val replacingString = context.data().getStringByRef(iterator.nextRef()) ?: return NULL

        val newString = string.replace(findingString, replacingString)
        return context.data().newStringRef(newString)
    }

    private fun funcUppercase(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val string = context.data().getStringByRef(iterator.nextRef()) ?: return NULL

        val newString = string.toUpperCase()
        return context.data().newStringRef(newString)
    }

    private fun funcLowercase(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val string = context.data().getStringByRef(iterator.nextRef()) ?: return NULL

        val newString = string.toLowerCase()
        return context.data().newStringRef(newString)
    }

    private fun funcSubstring(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val string = context.data().getStringByRef(iterator.nextRef()) ?: return NULL

        if (!iterator.hasNext()) return NULL
        val sliceFrom = getInt(iterator.next()) ?: return NULL
        val sliceTo = if (iterator.hasNext()) (getInt(iterator.next()) ?: return NULL) else string.length

        val newString = string.substring(sliceFrom, sliceTo)
        return context.data().newStringRef(newString)
    }

    private fun funcSplit(context: CalcFunctionContext) : Sequence<Double> {
        val iterator = context.operands().iterator()
        if (!iterator.hasNext()) return emptySequence()
        val string = context.data().getStringByRef(iterator.nextRef()) ?: return emptySequence()
        val separator = context.data().getStringByRef(iterator.nextRef()) ?: ""
        val limit = if (iterator.hasNext()) (getInt(iterator.next()) ?: 0) else 0
        val ignoreCase = if (iterator.hasNext()) isTrue(iterator.next()) else false
        val skipEmpty = if (iterator.hasNext()) isTrue(iterator.next()) else true

        return sequence {
            for (item in string.split(separator, ignoreCase = ignoreCase, limit = limit)) {
                if (skipEmpty && item.isEmpty()) {
                    continue
                }
                yield(context.data().newStringRef(item))
            }
        }
    }

    private fun funcStringJoin(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val separator = context.data().getStringByRef(iterator.nextRef()) ?: ""

        val joiner = StringJoiner(separator)
        while (iterator.hasNext()) {
            val string = context.data().getStringByRef(iterator.nextRef()) ?: continue
            joiner.add(string)
        }

        return context.data().newStringRef(joiner.toString())
    }

    private fun funcToString(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val value = iterator.next()
        val format = if (iterator.hasNext()) context.data().getStringByRef(iterator.nextRef()) else null

        val string = format?.replace("%d","%.0f")?.format(value) ?: value.toString()
        return context.data().newStringRef(string)
    }

    private fun funcToNumber(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val stringRef = iterator.nextRef()
        val string = context.data().getStringByRef(stringRef) ?: return NULL

        return string.toDoubleOrNull() ?: return NULL
    }

    private fun funcStringFormat(context: CalcFunctionContext) : Double {
        val iterator = context.operands().iterator()

        if (!iterator.hasNext()) return NULL
        val formatString = context.data().getStringByRef(iterator.nextRef()) ?: return NULL
        val params = context.operands()
                .map { context.data().getStringByRef(it) ?: it}
                .toList().toTypedArray<Any?>()
        val newString = formatString.replace("%d","%.0f").format(*params)
        return context.data().newStringRef(newString)
    }

    private fun funcStringNewLine(context: CalcFunctionContext) : Double {
        return context.data().newStringRef(System.getProperty("line.separator"))
    }

    private fun funcInReadLine(context: CalcFunctionContext) : Double {
        val reader = context.inputReader() ?: return NULL
        val line = reader.readLine() ?: return NULL
        return context.data().newStringRef(line)
    }

    private fun funcInReadLines(context: CalcFunctionContext) : Sequence<Double> {
        val reader = context.inputReader() ?: return emptySequence()
        return sequence {
            var line = reader.readLine()
            while (line != null) {
                yield(context.data().newStringRef(line))
                line = reader.readLine()
            }
        }
    }

    private fun funcOutWrite(context: CalcFunctionContext) : Double {
        val writer = context.outputWriter() ?: return NULL
        val iterator = context.operands().iterator()
        if (!iterator.hasNext()) return NULL
        val stringRef = iterator.nextRef()
        val string = context.data().getStringByRef(stringRef) ?: return NULL
        writer.write(string)
        return stringRef
    }

    private fun funcOutWriteAll(context: CalcFunctionContext) : Double {
        val writer = context.outputWriter() ?: return NULL
        val iterator = context.operands().iterator()

        while (iterator.hasNext()) {
            val stringRef = iterator.nextRef()
            val string = context.data().getStringByRef(stringRef) ?: continue
            writer.write(string)
        }

        return NULL
    }

    private fun funcOutWriteLine(context: CalcFunctionContext) : Double {
        val writer = context.outputWriter() ?: return NULL
        val iterator = context.operands().iterator()
        if (!iterator.hasNext()) return NULL
        val stringRef = iterator.nextRef()
        val string = context.data().getStringByRef(stringRef) ?: return NULL
        writer.write(string)
        writer.newLine()
        return stringRef
    }

    private fun funcOutWriteLines(context: CalcFunctionContext) : Double {
        val writer = context.outputWriter() ?: return NULL
        val iterator = context.operands().iterator()

        while (iterator.hasNext()) {
            val stringRef = iterator.nextRef()
            val string = context.data().getStringByRef(stringRef) ?: continue
            writer.write(string)
            writer.newLine()
        }

        return NULL
    }

    private fun getInt(value: Double?) : Int? {
        return CalcNumberHelper.getInt(value)
    }

    private fun getSingleValue(context: CalcFunctionContext, values: Sequence<Double>) : Double {
        val iterator = values.iterator()
        if (!iterator.hasNext()) return NULL

        val value = iterator.next()
        if (!iterator.hasNext()) {
            return value
        }

        val list = ArrayList<Double>()
        list.add(value)
        do {
            list.add(iterator.next())
        } while (iterator.hasNext())

        return context.data().newCollectionRef(CalcCollectionList(list))
    }
}