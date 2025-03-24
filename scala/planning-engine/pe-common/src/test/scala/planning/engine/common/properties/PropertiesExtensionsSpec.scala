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

import cats.ApplicativeThrow
import cats.effect.IO

import neotypes.model.types.Value
import planning.engine.common.UnitSpecIO


class PropertiesExtensionsSpec extends UnitSpecIO:

  private class CaseData extends Case:
    val oneStrProp = Map("key1" -> Value.Str("value1"))
    val oneListStrProp = Map("key1" -> Value.ListValue(List(Value.Str("value1"), Value.Str("value2"))))
    val oneIntProp = Map("key1" -> Value.Integer(42))
    val oneDecProp = Map("key1" -> Value.Decimal(3.14))

    def parseStr(value: Value): IO[String] = value match
      case Value.Str(str) => IO.pure(str)
      case _ => IO.raiseError(new AssertionError("Expected a string value"))

  "getForKey" should:
    "return the parsed value if the key exists" in newCase[CaseData]: data =>
      data.oneStrProp
        .getForKey[IO, String]("key1")(data.parseStr)
        .logValue
        .asserting(_ mustEqual "value1")

    "raise an AssertionError if the key does not exist" in newCase[CaseData]: data =>
      data.oneStrProp
        .getForKey[IO, String]("key2")(data.parseStr)
        .logValue
        .assertThrows[AssertionError]

  "getList" should:
    "return the parsed list if the key exists and is a list" in newCase[CaseData]: data =>
      data.oneListStrProp
        .getList[IO, String]("key1")(data.parseStr)
        .logValue
        .asserting(_ mustEqual List("value1", "value2"))

    "raise an AssertionError if the key exists but is not a list" in newCase[CaseData]: data =>
      data.oneListStrProp
        .getForKey[IO, String]("key2")(data.parseStr)
        .logValue
        .assertThrows[AssertionError]

  "getNumber" should:
    "return the parsed int value if the key exists and is an int" in newCase[CaseData]: data =>
      data.oneIntProp
        .getNumber[IO, Int]("key1")
        .logValue
        .asserting(_ mustEqual 42)

    "return the parsed float value if the key exists and is a float" in newCase[CaseData]: data =>
      data.oneDecProp
        .getNumber[IO, Float]("key1")
        .logValue
        .asserting(_ mustEqual 3.14f)

    "raise an AssertionError if the key exists" in newCase[CaseData]: data =>
      data.oneIntProp
        .getNumber[IO, Int]("key2")
        .logValue
        .assertThrows[AssertionError]

    "raise an AssertionError if property type is incorrect" in newCase[CaseData]: data =>
      data.oneIntProp
        .getNumber[IO, Float]("key1")
        .logValue
        .assertThrows[AssertionError]

      data.oneDecProp
        .getNumber[IO, Int]("key1")
        .logValue
        .assertThrows[AssertionError]
