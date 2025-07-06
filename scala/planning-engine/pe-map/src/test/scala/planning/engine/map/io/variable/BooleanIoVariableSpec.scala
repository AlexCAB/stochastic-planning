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
import planning.engine.common.UnitSpecWithData
import neotypes.model.types.Value
import neotypes.query.QueryArg.Param
import planning.engine.common.properties.PROP
import planning.engine.common.values.node.IoIndex
import planning.engine.map.io.variable.IoVariable.PROP_VALUE.BOOL_TYPE

import scala.jdk.CollectionConverters.*

class BooleanIoVariableSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    val trueAcceptable = BooleanIoVariable[IO](Set(true))
    val falseAcceptable = BooleanIoVariable[IO](Set(false))
    val bothAcceptable = BooleanIoVariable[IO](Set(true, false))

    val invalidProperties = Map(
      PROP.VAR_TYPE -> Value.Str(BOOL_TYPE),
      PROP.DOMAIN -> Value.ListValue(List(Value.Str("invalid")))
    )

    val validProperties = Map(
      PROP.VAR_TYPE -> Value.Str(BOOL_TYPE),
      PROP.DOMAIN -> Value.ListValue(List(Value.Bool(true), Value.Bool(false)))
    )

  "BooleanIoVariable.valueForIndex(...)" should:
    "return false for index 0 if false is acceptable or fail otherwise" in newCase[CaseData]: (_, data) =>
      data.falseAcceptable.valueForIndex(IoIndex(0)).asserting(_ mustEqual false)
      data.trueAcceptable.valueForIndex(IoIndex(0)).assertThrows[AssertionError]

    "return true for index 1 if true is acceptable or fail otherwise" in newCase[CaseData]: (_, data) =>
      data.trueAcceptable.valueForIndex(IoIndex(1)).asserting(_ mustEqual true)
      data.falseAcceptable.valueForIndex(IoIndex(1)).assertThrows[AssertionError]

    "return true or false for index 1 or 0" in newCase[CaseData]: (_, data) =>
      data.bothAcceptable.valueForIndex(IoIndex(0)).asserting(_ mustEqual false)
      data.bothAcceptable.valueForIndex(IoIndex(1)).asserting(_ mustEqual true)

    "raise an AssertionError for invalid index" in newCase[CaseData]: (_, data) =>
      data.trueAcceptable.valueForIndex(IoIndex(2)).assertThrows[AssertionError]

  "BooleanIoVariable.indexForValue(...)" should:
    "return Index(0) for false if false is acceptable or fail otherwise" in newCase[CaseData]: (_, data) =>
      data.falseAcceptable.indexForValue(false).asserting(_ mustEqual IoIndex(0))
      data.trueAcceptable.indexForValue(false).assertThrows[AssertionError]

    "return Index(1) for true if true is acceptable or fail otherwise" in newCase[CaseData]: (_, data) =>
      data.trueAcceptable.indexForValue(true).asserting(_ mustEqual IoIndex(1))
      data.falseAcceptable.indexForValue(true).assertThrows[AssertionError]

    "return Index(0) or Index(1) for false or true" in newCase[CaseData]: (_, data) =>
      data.bothAcceptable.indexForValue(false).asserting(_ mustEqual IoIndex(0))
      data.bothAcceptable.indexForValue(true).asserting(_ mustEqual IoIndex(1))

  "BooleanIoVariable.toQueryParams" should:
    "return correct properties map" in newCase[CaseData]: (tn, data) =>
      data.bothAcceptable.toQueryParams.logValue(tn)
        .asserting: params =>
          val acceptableValues = params
            .getOrElse(PROP.DOMAIN, fail(s"${PROP.DOMAIN} should be present"))
            .param.asInstanceOf[java.util.Iterator[java.lang.Boolean]]

          params.size mustEqual 2
          params.get(PROP.VAR_TYPE) mustEqual Some(Param(QueryParam(BOOL_TYPE)))
          acceptableValues.asScala.toSet mustEqual Set(true, false)

  "BooleanIoVariable.fromProperties(...)" should:
    "create BooleanIoVariable from valid properties" in newCase[CaseData]: (tn, data) =>
      BooleanIoVariable
        .fromProperties[IO](data.validProperties)
        .logValue(tn)
        .asserting(_.acceptableValues mustEqual Set(true, false))

    "raise an AssertionError for invalid domain value" in newCase[CaseData]: (tn, data) =>
      BooleanIoVariable
        .fromProperties[IO](data.invalidProperties)
        .logValue(tn)
        .assertThrows[AssertionError]
