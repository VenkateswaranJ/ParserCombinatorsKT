import arrow.core.None
import arrow.core.Some

fun parseChar(charToMatch: Char): Parser<Char> =
    satisfy({ c: Char -> c == charToMatch }, "$charToMatch")

fun<T> choice(parsers: List<Parser<out T>>): Parser<out T> =
    parsers.reduce { parseA, parseB -> parseA orElse parseB }

fun anyOf(charsToMatch: List<Char>): Parser<out Char> =
    charsToMatch
        .map { char -> parseChar(char) }
        .let { choice(it) }

fun manyChars(cp: Parser<out Char>): Parser<String> =
    many(cp).mapP { it.joinToString("") }

fun manyChars1(cp: Parser<Char>): Parser<String> =
    many1(cp).mapP { it.joinToString("") }

fun parseString(stringToMatch: String): Parser<String> =
    stringToMatch
        .map { it }
        .map { parseChar(it) }
        .sequence()
        .mapP { it.joinToString("") }
        .setLabel(stringToMatch)

fun whitespaceChar(): Parser<Char> = satisfy({ c: Char -> c.isWhitespace() }, "whitespace")
val spaces = many(whitespaceChar())
val spaces1 = many1(whitespaceChar())

fun digitChar(): Parser<Char> = satisfy({ c: Char -> c.isDigit() }, "digit")

fun parseInt(): Parser<Int> {
    val label = "integer"
    val digits = manyChars1(digitChar())
    val parseIntWithSign = opt(parseChar('-')) andThen digits
    return parseIntWithSign
        .mapP { (sign, digits) ->
            val i = digits.toInt()
            when (sign) {
                is Some -> -i
                is None -> i
            }
        }
        .setLabel(label)
}

fun parseFloat(): Parser<Float> {
    val label = "float"
    val digits = manyChars1(digitChar())
    val floatWithSign =
        opt(parseChar('-')) andThen
            digits andThen
            parseChar('.') andThen
            digits
    return floatWithSign
        .mapP {
            val (signWithDigits1AndPoint, digits2) = it
            val (signWithDigits1, _) = signWithDigits1AndPoint
            val (sign, digits1) = signWithDigits1
            val floatValue = "$digits1.$digits2".toFloat()
            when (sign) {
                is None -> floatValue
                is Some -> -floatValue
            }
        }
        .setLabel(label)
}
