import arrow.core.*
import arrow.core.raise.either

typealias Parser<T> = (String) -> Either<String, Pair<T, String>>

fun parseChar(charToMatch: Char, inputString:String): Either<String, Pair<Char, String>> =
    when {
        inputString.isEmpty() -> "No more input".left()
        inputString.first() == charToMatch -> (charToMatch to inputString.takeLast(inputString.length - 1)).right()
        else -> "Expecting '$charToMatch'. Got '${inputString.first()}'".left()
    }

infix fun <T> Parser<T>.andThen(other: Parser<T>): Parser<Pair<T,T>> =
    { inputString: String ->
        either {
            val (charToMatch1, remainingString1) = this@andThen(inputString).bind()
            val (charToMatch2, remainingString2) = other(remainingString1).bind()
            (charToMatch1 to charToMatch2) to remainingString2
        }
    }

infix fun <T> Parser<T>.orElse(other: Parser<T>): Parser<T> =
    { inputString: String ->
        when(val result = this(inputString)) {
            is Either.Right -> result
            is Either.Left -> other(inputString)
        }
    }

fun anyOf(charsToMatch: List<Char>): Parser<Char> =
    charsToMatch
        .map { char -> ::parseChar.curried()(char) }
        .reduce { parseA, parseB -> parseA orElse parseB }

val parseA = ::parseChar.curried()('A')
val parseB = ::parseChar.curried()('B')
val parseC = ::parseChar.curried()('C')
val parseAThenB = parseA andThen parseB
val parseAorElseB = parseA orElse parseB
val bOrElseC = parseB orElse parseC
val aAndThenBorC = parseA andThen bOrElseC
val parseLowercase = anyOf(('a'..'z').toList())
val parseDigit = anyOf(('0'..'9').toList())