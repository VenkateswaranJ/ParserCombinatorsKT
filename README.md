Kotlin version of the blog post [Understanding Parser Combinators](https://fsharpforfunandprofit.com/posts/understanding-parser-combinators/#series-toc)

1. Blog post - [Understanding Parser Combinators](https://fsharpforfunandprofit.com/posts/understanding-parser-combinators/) ( [source](https://github.com/VenkateswaranJ/ParserCombinatorsKT/blob/master/src/main/kotlin/UnderstandingParserCombinators.kt) and 
[tests](https://github.com/VenkateswaranJ/ParserCombinatorsKT/blob/master/src/test/kotlin/UnderstandingParserCombinatorsTest.kt) )
2. Blog post - [Building a useful set of parser combinators](https://fsharpforfunandprofit.com/posts/understanding-parser-combinators-2/) ( [source](https://github.com/VenkateswaranJ/ParserCombinatorsKT/blob/master/src/main/kotlin/BuildingUsefulSetOfParserCombinators.kt) and
   [tests](https://github.com/VenkateswaranJ/ParserCombinatorsKT/blob/master/src/test/kotlin/BuildingUsefulSetOfParserCombinatorsTest.kt) )
3. Blog post - [Improving the parser library](https://fsharpforfunandprofit.com/posts/understanding-parser-combinators-3/) ( improvements directly applied to existing source code )
4. Blog post - [Writing a JSON parser from scratch](https://fsharpforfunandprofit.com/posts/understanding-parser-combinators-4/) ( [source](https://github.com/VenkateswaranJ/ParserCombinatorsKT/blob/master/src/main/kotlin/JsonParser.kt) and [tests](https://github.com/VenkateswaranJ/ParserCombinatorsKT/blob/master/src/test/kotlin/JsonParserTest.kt) )

#### JsonParser test sample

```kotlin 
val json = 
"""{
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
printResult(JsonParser.jValue.run(json))
```
#### output
```kotlin
JObject(
   value = {
       widget=JObject(value=
       { debug=JString(value=on),
          window=JObject(value={
              title=JString(value=Sample Konfabulator Widget),
              name=JString(value=main_window),
              width=JNumber(value=500.0),
              height=JNumber(value=500.0)
          }),
          image=JObject(value={ 
              src=JString(value=Images/Sun.png),
              name=JString(value=sun1),
              hOffset=JNumber(value=250.0),
              vOffset=JNumber(value=250.0),
              alignment=JString(value=center)}),
          text=JObject(value={
              data=JString(value=Click Here),
              size=JNumber(value=36.0),
              style=JString(value=bold),
              name=JString(value=text1),
              hOffset=JNumber(value=250.0),
              vOffset=JNumber(value=100.0),
              alignment=JString(value=center),
              onMouseUp=JString(value=sun1.opacity = (sun1.opacity / 100) * 90;)
          })
       })
   })
```