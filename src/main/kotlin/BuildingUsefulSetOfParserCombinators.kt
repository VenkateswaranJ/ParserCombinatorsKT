import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.andThen
import arrow.core.curried
import arrow.core.none
import arrow.core.right
import arrow.core.some

fun <T, U> Parser<T>.mapP(transform: (T) -> U): Parser<U> {
    val parserFn = { input: InputState ->
        runOnInput(input).map { (matchedValue, remainingInput) ->
            transform(matchedValue) to remainingInput
        }
    }
    return Parser(parserFn, label)
}

fun <T> returnP(value: T): Parser<T> {
    val parserFn = { input: InputState -> (value to input).right() }
    return Parser(parserFn, "Unknown label")
}

fun <T, U> Parser<(T) -> U>.applyP(xp: Parser<T>): Parser<U> =
    (this andThen xp).mapP { (f, x) -> f(x) }

fun <T, U, C> lift2(f: (T, U) -> C): (Parser<T>, Parser<U>) -> Parser<C> =
    { parseT: Parser<T>, parseU: Parser<U> -> returnP(f.curried()).applyP(parseT).applyP(parseU) }

// should be archived with tail recursion, but my head is spinning if I think on how to maike it :)
fun <T> List<Parser<T>>.sequence(): Parser<List<T>> {
    val consP = lift2<T, List<T>, List<T>>(::cons)
    return if (isEmpty()) returnP(emptyList()) else consP(first(), takeLast(size - 1).sequence())
}

fun<T> cons(head: T, tail: List<T>): List<T> = listOf(head) + tail

fun <T> parseZeroOrMore(parser: Parser<T>, input: InputState): Pair<List<T>, InputState> =
    when (val result = parser.runOnInput(input)) {
        is Either.Left -> emptyList<T>() to input
        is Either.Right -> {
            val (firstValue, inputAfterFirstParse) = result.value
            val (subsequentValues, remainingInput) = parseZeroOrMore(parser, inputAfterFirstParse)
            cons(firstValue, subsequentValues) to remainingInput
        }
    }

fun <T> many(parser: Parser<T>): Parser<List<T>> {
    val parserFn = { input: InputState ->
        val (values, remainingInput) = parseZeroOrMore(parser, input)
        (values to remainingInput).right()
    }
    return Parser(parserFn, "zero or more " + parser.label)
}

fun <T> many1(parser: Parser<T>): Parser<List<T>> {
    val parserFn = { input: InputState ->
        when (val result = parser.runOnInput(input)) {
            is Either.Left -> result
            is Either.Right -> {
                val (firstValue, inputAfterFirstParse) = result.value
                val (subsequentValues, remainingInput) = parseZeroOrMore(parser, inputAfterFirstParse)
                (cons(firstValue, subsequentValues) to remainingInput).right()
            }
        }
    }
    return Parser(parserFn, "one or more " + parser.label)
}

fun <T> opt(parser: Parser<T>): Parser<out Option<T>> {
    val some = parser.mapP { it.some() }
    val none = returnP(none<T>())
    return some orElse none
}

fun buildSignedIntParser(): Parser<Int> {
    val parseIntWithSign = opt(parseChar('-')) andThen digits
    return parseIntWithSign.mapP { (sign, digits) ->
        val i = digits.joinToString("").toInt()
        when (sign) {
            is Some -> -i
            is None -> i
        }
    }
}

fun <T, U> Parser<T>.throwLeft(other: Parser<U>): Parser<U> =
    (this andThen other).mapP { (_, right) -> right }

fun <T, U> Parser<T>.throwRight(other: Parser<U>): Parser<T> =
    (this andThen other).mapP { (left, _) -> left }

fun <L, M, R> between(left: Parser<L>, middle: Parser<M>, right: Parser<R>): Parser<M> =
    left.throwLeft(middle).throwRight(right)

fun <T, U> sepBy1(listItemParser: Parser<T>, separator: Parser<U>): Parser<List<T>> {
    val sepThenP = separator.throwLeft(listItemParser)
    return (listItemParser andThen many(sepThenP))
        .mapP { (firstItemInList, remainingItemsInList) ->
            listOf(firstItemInList) + remainingItemsInList
        }
}

fun <T, U> sepBy(listParser: Parser<T>, separator: Parser<U>): Parser<out List<T>> =
    sepBy1(listParser, separator) orElse returnP(emptyList())

fun <T, U> Parser<T>.bindP(transform: (T) -> Parser<U>): Parser<U> {
    val parserFn = { input: InputState ->
        when (val result = runOnInput(input)) {
            is Either.Right -> {
                val (firstValue, inputAfterFirstParse) = result.value
                transform(firstValue).runOnInput(inputAfterFirstParse)
            }
            is Either.Left -> result
        }
    }
    return Parser(parserFn, "unknown")
}

fun <T, U> Parser<T>.mapUsingBind(transform: (T) -> U): Parser<U> =
    bindP(transform.andThen { returnP(it) })

val parseABC = parseString("ABC")
val manyA = many(parseA)
val manyAB = many(parseString("AB"))
val whitespaceChar = anyOf(listOf(' ', '\t', '\n'))
val whitespace = many(whitespaceChar)
val digits = many1(parseDigit)
val number = digits.mapP { it.joinToString("").toInt() }
val digitThenSemicolon = parseDigit andThen opt(parseChar(';'))
val signedIntParser = buildSignedIntParser()
val digitThenSemicolonBetter = parseDigit.throwRight(opt(parseChar(';')))
val doubleQuoteParser = parseChar('"')
val quotedInteger = between(doubleQuoteParser, signedIntParser, doubleQuoteParser)
val comma = parseChar(',')
val zeroOrMoreDigitList = sepBy(parseDigit, comma)
val oneOrMoreDigitList = sepBy1(parseDigit, comma)
