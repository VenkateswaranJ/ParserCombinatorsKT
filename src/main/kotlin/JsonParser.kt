import arrow.core.None
import arrow.core.Some
import arrow.core.getOrElse

sealed class JValue {
    data class JString(val value: String) : JValue()
    data class JNumber(val value: Float) : JValue()
    data class JBool(val value: Boolean) : JValue()
    object JNull : JValue()
    data class JObject(val value: Map<String, JValue>) : JValue()
    data class JArray(val value: List<JValue>) : JValue()
}

object JsonParser {

    // jValue forward references for jArray and jObject
    private var parserRef: Parser<out JValue> =
        Parser(
            parseFn = { _ -> throw Error("unfixed forwarded parser") },
            label = "unknown"
        )
    val jValue: Parser<JValue> =
        Parser(
            parseFn = { input: InputState -> parserRef.runOnInput(input) },
            label = "unknown"
        )

    // null
    val jNull = parseString("null").mapP { JValue.JNull }.setLabel("null")

    // boolean
    private val jTrue = parseString("true").mapP { JValue.JBool(true) }
    private val jFalse = parseString("false").mapP { JValue.JBool(false) }
    val jBool = (jTrue orElse jFalse).setLabel("bool")

    // string
    val jUnescapedChar = satisfy({ c: Char -> c != '\\' && c != '\"' }, "char")
    val jEscapedChar =
        listOf(
            "\\\"" to '\"', // quote
            "\\\\" to '\\', // reverse solidus
            "\\/" to '/', // solidus
            "\\b" to '\b', // backspace
            "\\f" to '\u000c', // form feed
            "\\n" to '\n', // newline
            "\\r" to '\r', // cr
            "\\t" to '\t' // tab
        )
            .map { (toMatch, result) -> parseString(toMatch).mapP { result } }
            .let { choice(it) }
            .setLabel("escaped char")
    private val backslash = parseChar('\\')
    private val uChar = parseChar('u')
    private val hexDigit =
        anyOf(('0'..'9').toList() + ('A'..'F').toList() + ('a'..'f').toList())
    private val fourHexDigits = hexDigit andThen hexDigit andThen hexDigit andThen hexDigit
    val jUnicodeChar = backslash.throwLeft(uChar).throwLeft(fourHexDigits)
        .mapP {
            val (remainingDigits1, fourthDigit) = it
            val (remainingDigits2, thirdDigit) = remainingDigits1
            val (firstDigit, secondDigit) = remainingDigits2
            "$firstDigit$secondDigit$thirdDigit$fourthDigit".toInt(16).toChar()
        }
    private val quote = parseChar('\"').setLabel("quote")
    private val jChar = jUnescapedChar orElse jEscapedChar orElse jUnicodeChar
    private val quotedString = quote.throwLeft(manyChars(jChar)).throwRight(quote)
    val jString = quotedString.mapP { JValue.JString(it) }.setLabel("quoted string")

    // number
    private val optSign = opt(parseChar('-'))
    private val zero = parseString("0")
    private val digitOneToNine = satisfy({ c: Char -> c.isDigit() && c != '0' }, "1-9")
    private val digit = satisfy({ c: Char -> c.isDigit() }, "digit")
    private val point = parseChar('.')
    private val e = parseChar('e') orElse parseChar('E')
    private val optPlusMinus = opt(parseChar('-') orElse parseChar('+'))

    private val nonZeroInt = (digitOneToNine andThen manyChars(digit)).mapP { (first, rest) -> first + rest }
    private val intPart = zero orElse nonZeroInt
    private val fractionPart = point.throwLeft(manyChars1(digit))
    private val exponentPart = e.throwLeft(optPlusMinus) andThen manyChars1(digit)
    val jNumber =
        (optSign andThen intPart andThen opt(fractionPart) andThen opt(exponentPart))
            .mapP {
                val (remainingValue1, expPart) = it
                val (remainingValue2, fractionPart) = remainingValue1
                val (optSign, intPart) = remainingValue2

                val signStr = optSign.getOrElse { "" }.toString()
                val fractionPartStr =
                    fractionPart.getOrElse { "" }.let { digits -> ".$digits" }
                val expPartStr = when (expPart) {
                    is None -> ""
                    is Some -> {
                        val (optSignExponent, digits) = expPart.value
                        "e" + optSignExponent.getOrElse { "" } + digits
                    }
                }
                (signStr + intPart + fractionPartStr + expPartStr)
                    .toFloat().let { float -> JValue.JNumber(float) }
            }
            .setLabel("number")

    // array
    private val leftBracket = parseChar('[').throwRight(spaces)
    private val rightBracket = parseChar(']').throwRight(spaces)
    private val comma = parseChar(',').throwRight(spaces)
    private val value = jValue.throwRight(spaces)
    private val values = sepBy(value, comma.throwRight(spaces))
    private val jArray = between(leftBracket, values, rightBracket).mapP { JValue.JArray(it) }.setLabel("array")

    // object
    // set up the "primitive" parsers
    private val leftBrace = spaces.throwLeft(parseChar('{')).throwRight(spaces)
    private val rightBrace = parseChar('}').throwRight(spaces)
    private val colon = parseChar(':').throwRight(spaces)
    private val key = quotedString.throwRight(spaces)

    // set up the list parser
    private val keyValue = key.throwRight(colon) andThen value
    private val keyValues = sepBy(keyValue, comma)

    // set up the main parser
    private val jObject = between(leftBrace, keyValues, rightBrace)
        .mapP { it.toMap() }
        .mapP { JValue.JObject(it) }
        .setLabel("object")

    init { parserRef = choice(listOf(jNull, jBool, jNumber, jString, jArray, jObject)) }
}
