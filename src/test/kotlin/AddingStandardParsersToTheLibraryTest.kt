import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AddingStandardParsersToTheLibraryTest: FunSpec() {
    init {
        test("Testing parseString") {
            parseString("AB")
                .run("ABC")
                .shouldBeRight()
                .also { (matchedString, remainingInput) ->
                    matchedString.shouldBe("AB")
                    remainingInput.shouldBe(InputState(listOf("ABC"), Position(0, 2)))
                }
            parseString("AB").run("A|C").shouldBeLeft()
        }

        test("Testing whitespace") {
            spaces
                .run(" ABC")
                .shouldBeRight()
                .also { (charsToMatch, remainingInput) ->
                    charsToMatch.shouldBe(listOf(' '))
                    remainingInput.shouldBe(InputState(listOf(" ABC"), Position(0, 1)))
                }
            spaces
                .run("A")
                .shouldBeRight()
                .also { (charsToMatch, remainingInput) ->
                    charsToMatch.shouldBe(listOf())
                    remainingInput.shouldBe(InputState(listOf("A"), Position(0, 0)))
                }
            spaces1
                .run(" ABC")
                .shouldBeRight()
                .also { (charsToMatch, remainingInput) ->
                    charsToMatch.shouldBe(listOf(' '))
                    remainingInput.shouldBe(InputState(listOf(" ABC"), Position(0, 1)))
                }
            spaces1.run("A").shouldBeLeft()
        }

        test( "Testing Int and Float parsers") {
            parseInt()
                .run("-123Z")
                .shouldBeRight()
                .also { (matchedInt, remainingInput) ->
                    matchedInt.shouldBe(-123)
                    remainingInput.shouldBe(InputState(listOf("-123Z"), Position(0, 4)))
                }
            parseInt().run("-Z123").shouldBeLeft()

            parseFloat()
                .run("-123.45Z")
                .shouldBeRight()
                .also { (matchedFloat, remainingInput) ->
                    matchedFloat.shouldBe(-123.45f)
                    remainingInput.shouldBe(InputState(listOf("-123.45Z"), Position(0, 7)))
                }
            parseFloat().run("-123Z45").shouldBeLeft()
        }
    }
}