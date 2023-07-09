import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class JsonParserTest : FunSpec() {
    init {
        test("JSON primitives tests") {
            // null
            JsonParser.jNull.run("null")
                .shouldBeRight()
                .also { (value, remainingInput) ->
                    value.shouldBeInstanceOf<JValue.JNull>()
                    remainingInput.shouldBe(InputState(listOf("null"), Position(0, 4)))
                }
            JsonParser.jNull.run("nulp").shouldBeLeft()

            // boolean
            JsonParser.jBool.run("true")
                .shouldBeRight()
                .also { (value, remainingInput) ->
                    value.shouldBeInstanceOf<JValue.JBool>().value.shouldBe(true)
                    remainingInput.shouldBe(InputState(listOf("true"), Position(0, 4)))
                }
            JsonParser.jBool.run("false")
                .shouldBeRight()
                .also { (value, remainingInput) ->
                    value.shouldBeInstanceOf<JValue.JBool>().value.shouldBe(false)
                    remainingInput.shouldBe(InputState(listOf("false"), Position(0, 5)))
                }
            JsonParser.jBool.run("truX").shouldBeLeft()

            // string
            // testing " and \
            JsonParser.jUnescapedChar.run("a").shouldBeRight()
            JsonParser.jUnescapedChar.run("\\").shouldBeLeft()

            // testing escaped chars
            JsonParser.jEscapedChar.run("\\\\").shouldBeRight()
            JsonParser.jEscapedChar.run("\\t").shouldBeRight()
            JsonParser.jEscapedChar.run("\\n").shouldBeRight()
            JsonParser.jEscapedChar.run("a").shouldBeLeft()

            // testing unicode characters
            JsonParser.jUnicodeChar.run("\\u263A").shouldBeRight()
            JsonParser.jUnicodeChar.run("u263A").shouldBeLeft()

            JsonParser.jString.run("\"\"").shouldBeRight()
            JsonParser.jString.run("\"a\"").shouldBeRight()
            JsonParser.jString.run("\"ab\"").shouldBeRight()
            JsonParser.jString.run("\"ab\\tde\"").shouldBeRight()
            JsonParser.jString.run("\"ab\\u263Ade\"").shouldBeRight()

            // testing number
            JsonParser.jNumber.run("123").shouldBeRight()
            JsonParser.jNumber.run("-123").shouldBeRight()
            JsonParser.jNumber.run("123.4").shouldBeRight()
            JsonParser.jNumber.run("123e4").shouldBeRight()
            JsonParser.jNumber.run("123.4e5").shouldBeRight()
            JsonParser.jNumber.run("1123.4e-5").shouldBeRight()
        }

        test("test json parser") {
            val example1 = """{
                              "name": "Scott",
                              "isMale": true,
                              "bday": {
                                "year": 2001,
                                "month": 12,
                                "day": 25
                              },
                              "favouriteColors": [
                                "blue",
                                "green"
                              ],
                              "emptyArray": [],
                              "emptyObject": {}
                            }"""
            val example2 = """{
                                "widget": {
                                    "debug": "on",
                                    "window": {
                                        "title": "Sample Konfabulator Widget",
                                        "name": "main_window",
                                        "width": 500,
                                        "height": 500
                                    },
                                    "image": {
                                        "src": "Images/Sun.png",
                                        "name": "sun1",
                                        "hOffset": 250,
                                        "vOffset": 250,
                                        "alignment": "center"
                                    },
                                    "text": {
                                        "data": "Click Here",
                                        "size": 36,
                                        "style": "bold",
                                        "name": "text1",
                                        "hOffset": 250,
                                        "vOffset": 100,
                                        "alignment": "center",
                                        "onMouseUp": "sun1.opacity = (sun1.opacity / 100) * 90;"
                                    }
                                    }
                                }
                            """

            printResult(JsonParser.jValue.run(example1))
            printResult(JsonParser.jValue.run(example2))
        }
    }
}
