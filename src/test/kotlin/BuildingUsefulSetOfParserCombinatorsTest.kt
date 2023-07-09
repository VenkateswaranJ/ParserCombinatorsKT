import arrow.core.none
import arrow.core.some
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class BuildingUsefulSetOfParserCombinatorsTest : FunSpec() {
    init {
        test("test sequence: List<Parse<T>> to Parser<List<T>>") {
            val parsers = listOf(parseA, parseB, parseC)
            val combined = parsers.sequence()
            combined.run("ABCD")
                .shouldBeRight()
                .also { (matchedChars, remainingInput) ->
                    matchedChars.shouldBe(listOf('A', 'B', 'C'))
                    remainingInput.shouldBe(InputState(listOf("ABCD"), Position(0, 3)))
                }
        }

        test("test string parser") {
            parseABC.run("ABCDE")
                .shouldBeRight()
                .also { (matchedString, remainingInput) ->
                    matchedString.shouldBe("ABC")
                    remainingInput.shouldBe(InputState(listOf("ABCDE"), Position(0, 3)))
                }
        }

        test("test many parser") {
            manyA.run("ABCD")
                .shouldBeRight()
                .also { (matchedChars, remainingInput) ->
                    matchedChars.shouldBe(listOf('A'))
                    remainingInput.shouldBe(InputState(listOf("ABCD"), Position(0, 1)))
                }

            manyA.run("AACD")
                .shouldBeRight()
                .also { (matchedChars, remainingInput) ->
                    matchedChars.shouldBe(listOf('A', 'A'))
                    remainingInput.shouldBe(InputState(listOf("AACD"), Position(0, 2)))
                }

            manyA.run("AAAD")
                .shouldBeRight()
                .also { (matchedChars, remainingInput) ->
                    matchedChars.shouldBe(listOf('A', 'A', 'A'))
                    remainingInput.shouldBe(InputState(listOf("AAAD"), Position(0, 3)))
                }

            manyA.run("|BCD")
                .shouldBeRight()
                .also { (matchedChars, remainingInput) ->
                    matchedChars.shouldBe(listOf())
                    remainingInput.shouldBe(InputState(listOf("|BCD"), Position(0, 0)))
                }

            manyAB.run("ABCD")
                .shouldBeRight()
                .also { (matchedString, remainingInput) ->
                    matchedString.shouldBe(listOf("AB"))
                    remainingInput.shouldBe(InputState(listOf("ABCD"), Position(0, 2)))
                }

            manyAB.run("ABABCD")
                .shouldBeRight()
                .also { (matchedString, remainingInput) ->
                    matchedString.shouldBe(listOf("AB", "AB"))
                    remainingInput.shouldBe(InputState(listOf("ABABCD"), Position(0, 4)))
                }

            manyAB.run("ZCD")
                .shouldBeRight()
                .also { (matchedString, remainingInput) ->
                    matchedString.shouldBe(listOf())
                    remainingInput.shouldBe(InputState(listOf("ZCD"), Position(0, 0)))
                }

            manyAB.run("AZCD")
                .shouldBeRight()
                .also { (matchedString, remainingInput) ->
                    matchedString.shouldBe(listOf())
                    remainingInput.shouldBe(InputState(listOf("AZCD"), Position(0, 0)))
                }

            whitespace.run("ABC")
                .shouldBeRight()
                .also { (matchedChars, remainingInput) ->
                    matchedChars.shouldBe(listOf())
                    remainingInput.shouldBe(InputState(listOf("ABC"), Position(0, 0)))
                }

            whitespace.run(" ABC")
                .shouldBeRight()
                .also { (matchedChars, remainingInput) ->
                    matchedChars.shouldBe(listOf(' '))
                    remainingInput.shouldBe(InputState(listOf(" ABC"), Position(0, 1)))
                }

            whitespace.run("\tABC")
                .shouldBeRight()
                .also { (matchedChars, remainingInput) ->
                    matchedChars.shouldBe(listOf('\t'))
                    remainingInput.shouldBe(InputState(listOf("\tABC"), Position(0, 1)))
                }
        }

        test("test many1 parser") {
            digits.run("1ABC")
                .shouldBeRight()
                .also { (matchedChars, remainingInput) ->
                    matchedChars.shouldBe(listOf('1'))
                    remainingInput.shouldBe(InputState(listOf("1ABC"), Position(0, 1)))
                }

            digits.run("12BC")
                .shouldBeRight()
                .also { (matchedChars, remainingInput) ->
                    matchedChars.shouldBe(listOf('1', '2'))
                    remainingInput.shouldBe(InputState(listOf("12BC"), Position(0, 2)))
                }

            digits.run("123C")
                .shouldBeRight()
                .also { (matchedChars, remainingInput) ->
                    matchedChars.shouldBe(listOf('1', '2', '3'))
                    remainingInput.shouldBe(InputState(listOf("123C"), Position(0, 3)))
                }

            digits.run("1234")
                .shouldBeRight()
                .also { (matchedChars, remainingInput) ->
                    matchedChars.shouldBe(listOf('1', '2', '3', '4'))
                    remainingInput.shouldBe(InputState(listOf("1234"), Position(0, 4)))
                }

            number.run("1ABC")
                .shouldBeRight()
                .also { (matchedNumber, remainingInput) ->
                    matchedNumber.shouldBe(1)
                    remainingInput.shouldBe(InputState(listOf("1ABC"), Position(0, 1)))
                }

            number.run("12BC")
                .shouldBeRight()
                .also { (matchedNumber, remainingInput) ->
                    matchedNumber.shouldBe(12)
                    remainingInput.shouldBe(InputState(listOf("12BC"), Position(0, 2)))
                }

            number.run("123C")
                .shouldBeRight()
                .also { (matchedNumber, remainingInput) ->
                    matchedNumber.shouldBe(123)
                    remainingInput.shouldBe(InputState(listOf("123C"), Position(0, 3)))
                }

            number.run("1234")
                .shouldBeRight()
                .also { (matchedNumber, remainingInput) ->
                    matchedNumber.shouldBe(1234)
                    remainingInput.shouldBe(InputState(listOf("1234"), Position(0, 4)))
                }
        }

        test("test zero or one parser") {
            digitThenSemicolon.run("1;")
                .shouldBeRight()
                .also { (matchedChars, remainingInput) ->
                    matchedChars.shouldBe('1' to ';'.some())
                    remainingInput.shouldBe(InputState(listOf("1;"), Position(0, 2)))
                }

            digitThenSemicolon.run("1")
                .shouldBeRight()
                .also { (matchedChars, remainingInput) ->
                    matchedChars.shouldBe('1' to none())
                    remainingInput.shouldBe(InputState(listOf("1"), Position(0, 1)))
                }

            signedIntParser.run("123C")
                .shouldBeRight()
                .also { (matchedInts, remainingInput) ->
                    matchedInts.shouldBe(123)
                    remainingInput.shouldBe(InputState(listOf("123C"), Position(0, 3)))
                }

            signedIntParser.run("-123C")
                .shouldBeRight()
                .also { (matchedInts, remainingInput) ->
                    matchedInts.shouldBe(-123)
                    remainingInput.shouldBe(InputState(listOf("-123C"), Position(0, 4)))
                }
        }

        test("throwing results away") {
            digitThenSemicolonBetter.run("1;")
                .shouldBeRight()
                .also { (matchedChar, remainingInput) ->
                    matchedChar.shouldBe('1')
                    remainingInput.shouldBe(InputState(listOf("1;"), Position(0, 2)))
                }

            digitThenSemicolonBetter.run("1")
                .shouldBeRight()
                .also { (matchedChar, remainingInput) ->
                    matchedChar.shouldBe('1')
                    remainingInput.shouldBe(InputState(listOf("1"), Position(0, 1)))
                }
        }

        test("test between parser") {
            quotedInteger.run("\"1234\"")
                .shouldBeRight()
                .also { (matchedInts, remainingInput) ->
                    matchedInts.shouldBe(1234)
                    remainingInput.shouldBe(InputState(listOf("\"1234\""), Position(0, 6)))
                }

            quotedInteger.run("1234").shouldBeLeft()
        }

        test("list separated by separator parser") {
            oneOrMoreDigitList.run("1;")
                .shouldBeRight()
                .also { (digitList, remainingInput) ->
                    digitList.shouldBe(listOf('1'))
                    remainingInput.shouldBe(InputState(listOf("1;"), Position(0, 1)))
                }

            oneOrMoreDigitList.run("1,2;")
                .shouldBeRight()
                .also { (digitList, remainingInput) ->
                    digitList.shouldBe(listOf('1', '2'))
                    remainingInput.shouldBe(InputState(listOf("1,2;"), Position(0, 3)))
                }

            oneOrMoreDigitList.run("1,2,3;")
                .shouldBeRight()
                .also { (digitList, remainingInput) ->
                    digitList.shouldBe(listOf('1', '2', '3'))
                    remainingInput.shouldBe(InputState(listOf("1,2,3;"), Position(0, 5)))
                }

            oneOrMoreDigitList.run("Z;").shouldBeLeft()

            zeroOrMoreDigitList.run("1;")
                .shouldBeRight()
                .also { (digitList, remainingInput) ->
                    digitList.shouldBe(listOf('1'))
                    remainingInput.shouldBe(InputState(listOf("1;"), Position(0, 1)))
                }

            zeroOrMoreDigitList.run("1,2;")
                .shouldBeRight()
                .also { (digitList, remainingInput) ->
                    digitList.shouldBe(listOf('1', '2'))
                    remainingInput.shouldBe(InputState(listOf("1,2;"), Position(0, 3)))
                }

            zeroOrMoreDigitList.run("1,2,3;")
                .shouldBeRight()
                .also { (digitList, remainingInput) ->
                    digitList.shouldBe(listOf('1', '2', '3'))
                    remainingInput.shouldBe(InputState(listOf("1,2,3;"), Position(0, 5)))
                }

            zeroOrMoreDigitList.run("Z;")
                .shouldBeRight()
                .also { (digitList, remainingInput) ->
                    digitList.shouldBe(listOf())
                    remainingInput.shouldBe(InputState(listOf("Z;"), Position(0, 0)))
                }
        }
    }
}
