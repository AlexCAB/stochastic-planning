/*|||||||||||||||||||||||||||||||||
|| 0 * * * * * * * * * ▲ * * * * ||
|| * ||||||||||| * ||||||||||| * ||
|| * ||  * * * * * ||       || 0 ||
|| * ||||||||||| * ||||||||||| * ||
|| * * ▲ * * 0|| * ||   (< * * * ||
|| * ||||||||||| * ||  ||||||||||||
|| * * * * * * * * *   ||||||||||||
| author: CAB |||||||||||||||||||||
| website: github.com/alexcab |||||
| created: 2025-03-22 |||||||||||*/

package planning.engine.common.properties

import cats.effect.IO

import neotypes.model.types.Value
import planning.engine.common.UnitSpecIO
import cats.syntax.all.*

class PropertiesSpec extends UnitSpecIO:

  private class CaseData extends Case:
    val properties = Map(
      "strKey1" -> Value.Str("strValue1"),
      "listStrKey1" -> Value.ListValue(List(Value.Str("listStrValue1"), Value.Str("listStrValue2"))),
      "intKey1" -> Value.Integer(42),
      "floatKey1" -> Value.Decimal(3.14),
      "boolKey1" -> Value.Bool(true),
      "nested.key1" -> Value.Str("nestedValue1"),
      "nested.key2" -> Value.Str("nestedValue2")
    )

    def parseStr(value: Value): IO[String] = value match
      case Value.Str(str) => IO.pure(str)
      case _              => IO.raiseError(new AssertionError("Expected a string value"))

  "propsOf" should:
    "combine multiple properties" in newCase[CaseData]: data =>
      propsOf[IO](
        "key1" -> Value.Str("value1"),
        "key2" -> IO.pure(Value.Str("value2")),
        "key3" -> IO.pure(Map("nestedKey" -> Value.Str("nestedValue")))
      ).logValue
        .asserting(_ mustEqual Map(
          "key1" -> Value.Str("value1"),
          "key2" -> Value.Str("value2"),
          "key3.nestedKey" -> Value.Str("nestedValue")
        ))

  "parseValue" should:
    "return the parsed value if the key exists" in newCase[CaseData]: data =>
      data.properties
        .parseValue[IO, String]("strKey1")(data.parseStr)
        .logValue
        .asserting(_ mustEqual "strValue1")

    "raise an AssertionError if the key does not exist" in newCase[CaseData]: data =>
      data.properties
        .parseValue[IO, String]("not_exist")(data.parseStr)
        .logValue
        .assertThrows[AssertionError]

  "parseList" should:
    "return the parsed list if the key exists and is a list" in newCase[CaseData]: data =>
      data.properties
        .parseList[IO, String]("listStrKey1")(data.parseStr)
        .logValue
        .asserting(_ mustEqual List("listStrValue1", "listStrValue2"))

    "raise an AssertionError if the key exists but is not a list" in newCase[CaseData]: data =>
      data.properties
        .parseValue[IO, String]("not_exist")(data.parseStr)
        .logValue
        .assertThrows[AssertionError]

  "getValue" should:
    "return the parsed int value if the key exists and is an int" in newCase[CaseData]: data =>
      data.properties
        .getValue[IO, Int]("intKey1")
        .logValue
        .asserting(_ mustEqual 42)

    "return the parsed float value if the key exists and is a float" in newCase[CaseData]: data =>
      data.properties
        .getValue[IO, Float]("floatKey1")
        .logValue
        .asserting(_ mustEqual 3.14f)

    "return the parsed String value if the key exists" in newCase[CaseData]: data =>
      data.properties
        .getValue[IO, String]("strKey1")
        .logValue
        .asserting(_ mustEqual "strValue1")

    "return the parsed Boolean value if the key exists" in newCase[CaseData]: data =>
      data.properties
        .getValue[IO, Boolean]("boolKey1")
        .logValue
        .asserting(_ mustEqual true)

    "raise an AssertionError if the key exists" in newCase[CaseData]: data =>
      data.properties
        .getValue[IO, Int]("not_exist")
        .logValue
        .assertThrows[AssertionError]

    "raise an AssertionError if property type is incorrect" in newCase[CaseData]: data =>
      data.properties
        .getValue[IO, Float]("intKey1")
        .logValue
        .assertThrows[AssertionError]

      data.properties
        .getValue[IO, Int]("floatKey1")
        .logValue
        .assertThrows[AssertionError]

    "getList" should:
      "return the correct list for a valid key" in newCase[CaseData]: data =>
        data.properties
          .getList[IO, String]("listStrKey1")
          .logValue
          .asserting(_ mustEqual List("listStrValue1", "listStrValue2"))

      "raise an AssertionError for an invalid list value" in newCase[CaseData]: data =>
        data.properties
          .getList[IO, String]("strKey1")
          .logValue
          .assertThrows[AssertionError]

    "getProps" should:
      "return the correct properties map for a valid key" in newCase[CaseData]: data =>
        data.properties
          .getProps[IO]("nested")
          .logValue
          .asserting(_ mustEqual Map("key1" -> Value.Str("nestedValue1"), "key2" -> Value.Str("nestedValue2")))

      "return an empty map for a non-existent key" in newCase[CaseData]: data =>
        data.properties
          .getProps[IO]("nonExistentKey")
          .logValue
          .asserting(_ mustEqual Map.empty)
