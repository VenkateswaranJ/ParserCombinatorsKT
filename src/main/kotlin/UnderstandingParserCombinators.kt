import arrow.core.*
import arrow.core.raise.either

typealias ParserLabel = String
typealias ParserError = String
typealias ParserResult<T> = Either<Triple<ParserLabel, ParserError, ParserPosition>, Pair<T, InputState>>
typealias ParserFunction<T> = (InputState) -> ParserResult<T>

data class Position(val line:Int = 0, val column: Int = 0)
data class InputState(val lines: List<String> = listOf(), val position: Position = Position())
data class ParserPosition(val currentLine: String, val line:Int, val column: Int)

class Parser<T>(val parseFn: ParserFunction<T>, val label:  ParserLabel)

fun <T> Parser<T>.runOnInput(inputState: InputState): ParserResult<T> = parseFn(inputState)
fun <T> Parser<T>.run(inputString: String): ParserResult<T> = runOnInput(inputString.toInputState())

fun <T> Parser<T>.setLabel(newLabel: ParserLabel): Parser<T> {
    val parserFn = { input: InputState ->
        when(val result = runOnInput(input)) {
            is Either.Right -> result
            is Either.Left -> {
                val (_,err) = result.value
                Triple(newLabel, err, input.toParserPosition()).left()
            }
        }
    }
    return Parser(parserFn, newLabel)
}

fun <T> printResult(parserResult: ParserResult<T>) =
    when (parserResult) {
        is Either.Right -> {
            val (value, _) = parserResult.value; println(value)
        }
        is Either.Left -> {
            val (label, error, position) = parserResult.value
            val errorLine = position.currentLine
            val linePos = position.line
            val colPos = position.column
            val failureCaretPosition = position.column + error.length + 1
            val failureCaret = ("^$error").padStart(failureCaretPosition, ' ')
            println("Line:$linePos Col:$colPos Error parsing $label\n$errorLine\n$failureCaret")
        }
    }

fun satisfy(predicate: (Char) -> Boolean, label: ParserLabel): Parser<Char> {
    val parserFn = { input: InputState ->
        val (remainingInput,charOpt) = input.nextChar()
        when(charOpt) {
            is None -> Triple(label, "No more input", input.toParserPosition()).left()
            is Some -> {
                if(predicate(charOpt.value)) (charOpt.value to remainingInput).right()
                else Triple(label, "Unexpected ${charOpt.value}", input.toParserPosition()).left()
            }
        }
    }
    return Parser(parserFn, label)
}

fun String.toInputState(): InputState =
    if(isEmpty()) InputState() else InputState(lines(), Position())

fun InputState.currentLine(): String = lines.getOrElse(position.line) { "end of file" }

fun InputState.toParserPosition(): ParserPosition =
    ParserPosition(
        currentLine = currentLine(),
        line = position.line,
        column = position.column
    )

// Get the next character from the input, if any
// else return None. Also return the updated InputState
// Signature: InputState -> InputState * char option

// three cases
// 1) if line >= maxLine ->
//       return EOF
// 2) if col less than line length ->
//       return char at colPos, increment colPos
// 3) if col at line length ->
//       return NewLine, increment linePos
fun InputState.nextChar(): Pair<InputState, Option<Char>> {
    val linePos = position.line
    val columnPos = position.column
    val currentLine = currentLine()
    return when {
        linePos >= lines.size ->  this to None
        columnPos < currentLine.length -> {
            val char = currentLine[columnPos]
            val newPos = position.copy(column = columnPos + 1)
            val newState = copy(position = newPos)
            newState to char.some()
        }
        else -> {
            // end of line, so return LF and move to next line
            val char = '\n'
            val newPos = Position(line = linePos + 1, column = 0)
            val newState = copy(position = newPos)
            newState to char.some()
        }
    }
}

fun InputState.readAllChars(): List<Char> =
    buildList {
        var input = this@readAllChars
        while (true) {
            val (remainingInput, charOpt) = input.nextChar()
            when(charOpt) {
                is None -> break
                is Some -> { add(charOpt.value); input = remainingInput }
            }
        }
    }

fun parseChar(charToMatch: Char): Parser<Char> =
    satisfy({c: Char -> c == charToMatch}, "$charToMatch")

infix fun <T, U> Parser<T>.andThen(other: Parser<U>): Parser<Pair<T,U>> {
    val label = "$label andThen ${other.label}"
    val parserFn = { input: InputState ->
        either {
            val (matchedValue1, remainingInput1) = runOnInput(input).bind()
            val (matchedValue2, remainingInput2) = other.runOnInput(remainingInput1).bind()
            (matchedValue1 to matchedValue2) to remainingInput2
        }
    }
    return Parser(parserFn, label)
}

infix fun <T> Parser<T>.orElse(other: Parser<T>): Parser<T> {
    val label = "$label orElse ${other.label}"
    val parserFn = { input: InputState ->
        when(val result = runOnInput(input)) {
            is Either.Right -> result
            is Either.Left -> other.runOnInput(input)
        }
    }
    return Parser(parserFn, label)
}

fun anyOf(charsToMatch: List<Char>): Parser<Char> =
    charsToMatch
        .map { char -> parseChar(char) }
        .reduce { parseA, parseB -> parseA orElse parseB }

fun digitChar(): Parser<Char> = satisfy({ c: Char -> c.isDigit() }, "digit")
fun whitespaceChar(): Parser<Char> = satisfy({ c: Char -> c.isWhitespace() }, "whitespace")

val parseA = parseChar('A')
val parseB = parseChar('B')
val parseC = parseChar('C')
val parseAThenB = parseA andThen parseB
val parseAorElseB = parseA orElse parseB
val bOrElseC = parseB orElse parseC
val aAndThenBorC = parseA andThen bOrElseC
val parseLowercase = anyOf(('a'..'z').toList())
val parseDigit = anyOf(('0'..'9').toList())

val parseDigitWithLabel = anyOf(('0'..'9').toList()).setLabel("digit")