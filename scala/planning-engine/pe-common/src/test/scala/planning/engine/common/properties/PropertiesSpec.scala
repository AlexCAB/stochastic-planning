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
import planning.engine.common.UnitSpecWithData
import neotypes.query.QueryArg.Param
import neotypes.model.query.QueryParam.NullValue
import neotypes.model.query.QueryParam
import cats.syntax.all.*

class PropertiesSpec extends UnitSpecWithData:

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

    val params = Map(
      "key1" -> "value1".toDbParam,
      "nested.key" -> "value2".toDbParam
    )

    def paramsWithPrefix(prefix: String): Map[String, Param] = params.map((k, v) => (s"$prefix.$k", v))

    def parseStr(value: Value): IO[String] = value match
      case Value.Str(str) => IO.pure(str)
      case _              => IO.raiseError(new AssertionError("Expected a string value"))

  "T.toDbParam" should:
    "convert a non-null value to a query parameter" in: _ =>
      ("testValue".toDbParam mustEqual Param(QueryParam("testValue"))).pure

    "convert an integer value to a query parameter" in: _ =>
      (42.toDbParam mustEqual Param(QueryParam(42))).pure

    "convert a null value to NullValue" in: _ =>
      ((null: String).toDbParam mustEqual Param(NullValue)).pure

  "paramsOf" should:
    "combine multiple properties" in: ts =>
      paramsOf[IO](
        "key1" -> "value1".toDbParam,
        "key2" -> Some("value2".toDbParam),
        "keyNone" -> None,
        "key3" -> IO.pure("value3".toDbParam),
        "key4" -> IO.pure(Map("nestedKey" -> "nestedValue".toDbParam))
      ).logValue(ts.name)
        .asserting(_ mustEqual Map(
          "key1" -> "value1".toDbParam,
          "key2" -> "value2".toDbParam,
          "key3" -> "value3".toDbParam,
          "key4.nestedKey" -> "nestedValue".toDbParam
        ))

  "Map[String, Value].addKeyPrefix" should:
    "add the prefix to all keys in the map" in newCase[CaseData]: (tn, data) =>
      IO.pure(data.params)
        .addKeyPrefix("prefix")
        .logValue(tn)
        .asserting(_ mustEqual data.paramsWithPrefix("prefix"))

  "Map[String, Value].removeKeyPrefix" should:
    "remove the prefix from all keys in the map" in newCase[CaseData]: (tn, data) =>
      IO.pure(data.paramsWithPrefix("prefix"))
        .removeKeyPrefix("prefix")
        .logValue(tn)
        .asserting(_ mustEqual data.params)

    "raise an error if a key does not start with the prefix" in newCase[CaseData]: (tn, data) =>
      IO.pure(data.paramsWithPrefix("prefix") + ("invalid_prefix" -> "invalid_value".toDbParam))
        .removeKeyPrefix("nonexistent")
        .logValue(tn)
        .assertThrows[AssertionError]

  "Map[String, Value].parseValue" should:
    "return the parsed value if the key exists" in newCase[CaseData]: (tn, data) =>
      info
      data.properties
        .parseValue[IO, String]("strKey1")(data.parseStr)
        .logValue(tn)
        .asserting(_ mustEqual "strValue1")

    "Map[String, Value].raise an AssertionError if the key does not exist" in newCase[CaseData]: (tn, data) =>
      data.properties
        .parseValue[IO, String]("not_exist")(data.parseStr)
        .logValue(tn)
        .assertThrows[AssertionError]

  "Map[String, Value].parseList" should:
    "return the parsed list if the key exists and is a list" in newCase[CaseData]: (tn, data) =>
      data.properties
        .parseList[IO, String]("listStrKey1")(data.parseStr)
        .logValue(tn)
        .asserting(_ mustEqual List("listStrValue1", "listStrValue2"))

    "raise an AssertionError if the key exists but is not a list" in newCase[CaseData]: (tn, data) =>
      data.properties
        .parseValue[IO, String]("not_exist")(data.parseStr)
        .logValue(tn)
        .assertThrows[AssertionError]

  "Map[String, Value].getValue" should:
    "return the parsed int value if the key exists and is an int" in newCase[CaseData]: (tn, data) =>
      data.properties
        .getValue[IO, Long]("intKey1")
        .logValue(tn)
        .asserting(_ mustEqual 42L)

    "return the parsed float value if the key exists and is a float" in newCase[CaseData]: (tn, data) =>
      data.properties
        .getValue[IO, Double]("floatKey1")
        .logValue(tn)
        .asserting(_ mustEqual 3.14)

    "return the parsed String value if the key exists" in newCase[CaseData]: (tn, data) =>
      data.properties
        .getValue[IO, String]("strKey1")
        .logValue(tn)
        .asserting(_ mustEqual "strValue1")

    "return the parsed Boolean value if the key exists" in newCase[CaseData]: (tn, data) =>
      data.properties
        .getValue[IO, Boolean]("boolKey1")
        .logValue(tn)
        .asserting(_ mustEqual true)

    "raise an AssertionError if the key exists" in newCase[CaseData]: (tn, data) =>
      data.properties
        .getValue[IO, Long]("not_exist")
        .logValue(tn)
        .assertThrows[AssertionError]

    "raise an AssertionError if property type is incorrect" in newCase[CaseData]: (tn, data) =>
      data.properties
        .getValue[IO, Double]("intKey1")
        .logValue(tn)
        .assertThrows[AssertionError]

      data.properties
        .getValue[IO, Long]("floatKey1")
        .logValue(tn)
        .assertThrows[AssertionError]

    "Map[String, Value].getOptional" should:
      "return some value for exist key" in newCase[CaseData]: (tn, data) =>
        data.properties
          .getOptional[IO, String]("strKey1")
          .logValue(tn)
          .asserting(_ must contain("strValue1"))

      "return none for not exist key" in newCase[CaseData]: (tn, data) =>
        data.properties
          .getOptional[IO, String]("not_exist_string_1")
          .logValue(tn)
          .asserting(_ mustBe empty)

    "Map[String, Value].getList" should:
      "raise an AssertionError for an invalid list value" in newCase[CaseData]: (tn, data) =>
        data.properties
          .getList[IO, String]("listStrKey1")
          .logValue(tn)
          .asserting(_ mustEqual List("listStrValue1", "listStrValue2"))

      "raise an AssertionError for other invalid list value" in newCase[CaseData]: (tn, data) =>
        data.properties
          .getList[IO, String]("strKey1")
          .logValue(tn)
          .assertThrows[AssertionError]

    "Map[String, Value].getProps" should:
      "return the correct properties map for a valid key" in newCase[CaseData]: (tn, data) =>
        data.properties
          .getProps[IO]("nested")
          .logValue(tn)
          .asserting(_ mustEqual Map("key1" -> Value.Str("nestedValue1"), "key2" -> Value.Str("nestedValue2")))

      "return an empty map for a non-existent key" in newCase[CaseData]: (tn, data) =>
        data.properties
          .getProps[IO]("nonExistentKey")
          .logValue(tn)
          .asserting(_ mustEqual Map.empty)

    "Node.getValue" should:
      "return the correct value for a valid key" in newCase[CaseData]: (tn, data) =>
        data.properties
          .getValue[IO, String]("strKey1")
          .logValue(tn)
          .asserting(_ mustEqual "strValue1")

    "Node.getOptional" should:
      "return the correct optional value for a valid key" in newCase[CaseData]: (tn, data) =>
        data.properties
          .getOptional[IO, String]("strKey1")
          .logValue(tn)
          .asserting(_ must contain("strValue1"))

    "Node.getList" should:
      "return the correct list for a valid key" in newCase[CaseData]: (tn, data) =>
        data.properties
          .getList[IO, String]("listStrKey1")
          .logValue(tn)
          .asserting(_ mustEqual List("listStrValue1", "listStrValue2"))

    "Node.getProps" should:
      "return the correct properties map for a valid key" in newCase[CaseData]: (tn, data) =>
        data.properties
          .getProps[IO]("nested")
          .logValue(tn)
          .asserting(_ mustEqual Map("key1" -> Value.Str("nestedValue1"), "key2" -> Value.Str("nestedValue2")))
