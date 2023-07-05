import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class UnderstandingParserCombinatorsTest: FunSpec() {
    init {
        test("test for parseChar") {

            parseChar('A', "ABC")
                .shouldBeRight()
                .also { (charToMatch, remainingString) ->
                    charToMatch.shouldBe('A')
                    remainingString.shouldBe("BC")
                }

            parseChar('A', "ZBC")
                .shouldBeLeft("Expecting 'A'. Got 'Z'")
        }

        test("test for parseA") {
            parseA("ABC")
                .shouldBeRight()
                .also { (charToMatch, remainingString) ->
                    charToMatch.shouldBe('A')
                    remainingString.shouldBe("BC")
                }

            parseA("ZBC")
                .shouldBeLeft("Expecting 'A'. Got 'Z'")
        }

        test("test A andThen B parser") {
            parseAThenB("ABC")
                .shouldBeRight()
                .also { (charToMatch, remainingString) ->
                    charToMatch.shouldBe('A' to 'B')
                    remainingString.shouldBe("C")
                }

            parseAThenB("ZBC")
                .shouldBeLeft("Expecting 'A'. Got 'Z'")
            parseAThenB("AZC")
                .shouldBeLeft("Expecting 'B'. Got 'Z'")
        }

        test("test A orElse B parser") {
            parseAorElseB("AZZ")
                .shouldBeRight()
                .also { (charToMatch, remainingString) ->
                    charToMatch.shouldBe('A')
                    remainingString.shouldBe("ZZ")
                }

            parseAorElseB("BZZ")
                .shouldBeRight()
                .also { (charToMatch, remainingString) ->
                    charToMatch.shouldBe('B')
                    remainingString.shouldBe("ZZ")
                }

            parseAorElseB("CZZ")
                .shouldBeLeft("Expecting 'B'. Got 'C'")
        }

        test("test both 'andThen', 'orElse'") {
            aAndThenBorC("ABZ")
                .shouldBeRight()
                .also { (charToMatch, remainingString) ->
                    charToMatch.shouldBe('A' to 'B')
                    remainingString.shouldBe("Z")
                }

            aAndThenBorC("ACZ")
                .shouldBeRight()
                .also { (charToMatch, remainingString) ->
                    charToMatch.shouldBe('A' to 'C')
                    remainingString.shouldBe("Z")
                }

            aAndThenBorC("QBZ")
                .shouldBeLeft("Expecting 'A'. Got 'Q'")

            aAndThenBorC("AQZ")
                .shouldBeLeft("Expecting 'C'. Got 'Q'")
        }

        test("test anyOf") {
            parseLowercase("aBC")
                .shouldBeRight()
                .also { (charToMatch, remainingString) ->
                    charToMatch.shouldBe('a')
                    remainingString.shouldBe("BC")
                }

            parseLowercase("ABC")
                .shouldBeLeft("Expecting 'z'. Got 'A'")

            parseDigit("1ABC")
                .shouldBeRight()
                .also { (charToMatch, remainingString) ->
                    charToMatch.shouldBe('1')
                    remainingString.shouldBe("ABC")
                }

            parseDigit("9ABC")
                .shouldBeRight()
                .also { (charToMatch, remainingString) ->
                    charToMatch.shouldBe('9')
                    remainingString.shouldBe("ABC")
                }

            parseDigit("|ABC")
                .shouldBeLeft("Expecting '9'. Got '|'")
        }
    }
}