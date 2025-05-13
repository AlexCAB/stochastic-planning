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
| created: 2025-03-24 |||||||||||*/

package planning.engine.map.io.variable

import cats.effect.IO
import neotypes.model.query.QueryParam
import planning.engine.common.UnitSpecIO
import neotypes.model.types.Value
import neotypes.query.QueryArg.Param
import planning.engine.common.properties.PROP_NAME
import planning.engine.common.values.node.IoIndex
import planning.engine.map.io.variable.IoVariable.PROP_VALUE.BOOL_TYPE

import scala.jdk.CollectionConverters.*

class BooleanIoVariableSpec extends UnitSpecIO:

  private class CaseData extends Case:
    val trueAcceptable = BooleanIoVariable[IO](Set(true))
    val falseAcceptable = BooleanIoVariable[IO](Set(false))
    val bothAcceptable = BooleanIoVariable[IO](Set(true, false))

    val invalidProperties = Map(
      PROP_NAME.VAR_TYPE -> Value.Str(BOOL_TYPE),
      PROP_NAME.DOMAIN -> Value.ListValue(List(Value.Str("invalid")))
    )

    val validProperties = Map(
      PROP_NAME.VAR_TYPE -> Value.Str(BOOL_TYPE),
      PROP_NAME.DOMAIN -> Value.ListValue(List(Value.Bool(true), Value.Bool(false)))
    )

  "valueForIndex" should:
    "return false for index 0 if false is acceptable or fail otherwise" in newCase[CaseData]: data =>
      data.falseAcceptable.valueForIndex(IoIndex(0)).asserting(_ mustEqual false)
      data.trueAcceptable.valueForIndex(IoIndex(0)).assertThrows[AssertionError]

    "return true for index 1 if true is acceptable or fail otherwise" in newCase[CaseData]: data =>
      data.trueAcceptable.valueForIndex(IoIndex(1)).asserting(_ mustEqual true)
      data.falseAcceptable.valueForIndex(IoIndex(1)).assertThrows[AssertionError]

    "return true or false for index 1 or 0" in newCase[CaseData]: data =>
      data.bothAcceptable.valueForIndex(IoIndex(0)).asserting(_ mustEqual false)
      data.bothAcceptable.valueForIndex(IoIndex(1)).asserting(_ mustEqual true)

    "raise an AssertionError for invalid index" in newCase[CaseData]: data =>
      data.trueAcceptable.valueForIndex(IoIndex(2)).assertThrows[AssertionError]

  "indexForValue" should:
    "return Index(0) for false if false is acceptable or fail otherwise" in newCase[CaseData]: data =>
      data.falseAcceptable.indexForValue(false).asserting(_ mustEqual IoIndex(0))
      data.trueAcceptable.indexForValue(false).assertThrows[AssertionError]

    "return Index(1) for true if true is acceptable or fail otherwise" in newCase[CaseData]: data =>
      data.trueAcceptable.indexForValue(true).asserting(_ mustEqual IoIndex(1))
      data.falseAcceptable.indexForValue(true).assertThrows[AssertionError]

    "return Index(0) or Index(1) for false or true" in newCase[CaseData]: data =>
      data.bothAcceptable.indexForValue(false).asserting(_ mustEqual IoIndex(0))
      data.bothAcceptable.indexForValue(true).asserting(_ mustEqual IoIndex(1))

  "toQueryParams" should:
    "return correct properties map" in newCase[CaseData]: data =>
      data.bothAcceptable.toQueryParams.logValue
        .asserting: params =>
          val acceptableValues = params
            .getOrElse(PROP_NAME.DOMAIN, fail(s"${PROP_NAME.DOMAIN} should be present"))
            .param.asInstanceOf[java.util.Iterator[java.lang.Boolean]]

          params.size mustEqual 2
          params.get(PROP_NAME.VAR_TYPE) mustEqual Some(Param(QueryParam(BOOL_TYPE)))
          acceptableValues.asScala.toSet mustEqual Set(true, false)

  "fromProperties" should:
    "create BooleanIoVariable from valid properties" in newCase[CaseData]: data =>
      BooleanIoVariable
        .fromProperties[IO](data.validProperties)
        .logValue
        .asserting(_.acceptableValues mustEqual Set(true, false))

    "raise an AssertionError for invalid domain value" in newCase[CaseData]: data =>
      BooleanIoVariable
        .fromProperties[IO](data.invalidProperties)
        .logValue
        .assertThrows[AssertionError]
