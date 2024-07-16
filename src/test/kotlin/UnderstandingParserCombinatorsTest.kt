import arrow.core.None
import arrow.core.Some
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe

class UnderstandingParserCombinatorsTest : FunSpec() {
    init {
        test("test for parseChar") {
            val parseChar = parseChar('A')
            parseChar.run("ABC")
                .shouldBeRight()
                .also { (charToMatch, remainingInput) ->
                    charToMatch.shouldBe('A')
                    remainingInput.shouldBe(InputState(listOf("ABC"), Position(0, 1)))
                }

            parseChar.run("ZBC")
                .shouldBeLeft()
                .also { (label, error) ->
                    label.shouldBe("A")
                    error.shouldBe("Unexpected Z")
                }
        }

        test("test for parseA") {
            parseA.run("ABC")
                .shouldBeRight()
                .also { (charToMatch, remainingInput) ->
                    charToMatch.shouldBe('A')
                    remainingInput.shouldBe(InputState(listOf("ABC"), Position(0, 1)))
                }

            parseA.run("ZBC")
                .shouldBeLeft()
                .also { (label, error) ->
                    label.shouldBe("A")
                    error.shouldBe("Unexpected Z")
                }
        }

        test("test A andThen B parser") {
            parseAThenB.run("ABC")
                .shouldBeRight()
                .also { (charToMatch, remainingInput) ->
                    charToMatch.shouldBe('A' to 'B')
                    remainingInput.shouldBe(InputState(listOf("ABC"), Position(0, 2)))
                }

            parseAThenB.run("ZBC")
                .shouldBeLeft()
                .also { (label, error) ->
                    label.shouldBe("A")
                    error.shouldBe("Unexpected Z")
                }

            parseAThenB.run("AZC")
                .shouldBeLeft()
                .also { (label, error) ->
                    label.shouldBe("B")
                    error.shouldBe("Unexpected Z")
                }
        }

        test("test A orElse B parser") {
            parseAorElseB.run("AZZ")
                .shouldBeRight()
                .also { (charToMatch, remainingInput) ->
                    charToMatch.shouldBe('A')
                    remainingInput.shouldBe(InputState(listOf("AZZ"), Position(0, 1)))
                }

            parseAorElseB.run("BZZ")
                .shouldBeRight()
                .also { (charToMatch, remainingInput) ->
                    charToMatch.shouldBe('B')
                    remainingInput.shouldBe(InputState(listOf("BZZ"), Position(0, 1)))
                }

            parseAorElseB.run("CZZ")
                .shouldBeLeft()
                .also { (label, error) ->
                    label.shouldBe("B")
                    error.shouldBe("Unexpected C")
                }
        }

        test("test both 'andThen', 'orElse'") {
            aAndThenBorC.run("ABZ")
                .shouldBeRight()
                .also { (charToMatch, remainingInput) ->
                    charToMatch.shouldBe('A' to 'B')
                    remainingInput.shouldBe(InputState(listOf("ABZ"), Position(0, 2)))
                }

            aAndThenBorC.run("ACZ")
                .shouldBeRight()
                .also { (charToMatch, remainingInput) ->
                    charToMatch.shouldBe('A' to 'C')
                    remainingInput.shouldBe(InputState(listOf("ACZ"), Position(0, 2)))
                }

            aAndThenBorC.run("QBZ")
                .shouldBeLeft()
                .also { (label, error) ->
                    label.shouldBe("A")
                    error.shouldBe("Unexpected Q")
                }

            aAndThenBorC.run("AQZ")
                .shouldBeLeft()
                .also { (label, error) ->
                    label.shouldBe("C")
                    error.shouldBe("Unexpected Q")
                }
        }

        test("test anyOf") {
            parseLowercase.run("aBC")
                .shouldBeRight()
                .also { (charToMatch, remainingInput) ->
                    charToMatch.shouldBe('a')
                    remainingInput.shouldBe(InputState(listOf("aBC"), Position(0, 1)))
                }

            parseLowercase.run("ABC")
                .shouldBeLeft()
                .also { (label, error) ->
                    label.shouldBe("z")
                    error.shouldBe("Unexpected A")
                }

            parseDigit.run("1ABC")
                .shouldBeRight()
                .also { (charToMatch, remainingInput) ->
                    charToMatch.shouldBe('1')
                    remainingInput.shouldBe(InputState(listOf("1ABC"), Position(0, 1)))
                }

            parseDigit.run("9ABC")
                .shouldBeRight()
                .also { (charToMatch, remainingInput) ->
                    charToMatch.shouldBe('9')
                    remainingInput.shouldBe(InputState(listOf("9ABC"), Position(0, 1)))
                }

            parseDigit.run("|ABC")
                .shouldBeLeft()
                .also { (label, error) ->
                    label.shouldBe("9")
                    error.shouldBe("Unexpected |")
                }

            parseDigitWithLabel.run("|ABC").shouldBeLeft()
        }

        test("read input characters test") {

            fun InputState.readAllChars(): List<Char> =
                buildList {
                    var input = this@readAllChars
                    while (true) {
                        val (remainingInput, charOpt) = input.nextChar()
                        when (charOpt) {
                            is None -> break
                            is Some -> {
                                add(charOpt.value)
                                input = remainingInput
                            }
                        }
                    }
                }

            "".toInputState().readAllChars().shouldBeEmpty()
            "a".toInputState().readAllChars().shouldBe(listOf('a', '\n'))
            "ab".toInputState().readAllChars().shouldBe(listOf('a', 'b', '\n'))
            "a\nb".toInputState().readAllChars().shouldBe(listOf('a', '\n', 'b', '\n'))
        }
    }
}
