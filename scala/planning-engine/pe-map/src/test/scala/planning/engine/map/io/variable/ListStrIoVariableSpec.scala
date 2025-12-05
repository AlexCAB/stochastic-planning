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
import planning.engine.common.values.io.IoIndex
import planning.engine.map.io.variable.IoVariable.*
import scala.jdk.CollectionConverters.*

class ListStrIoVariableSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    val variable = ListStrIoVariable[IO](List("a", "b", "c"))

    val invalidProperties = Map(
      PROP.VAR_TYPE -> Value.Str(PROP_VALUE.LIST_STR_TYPE),
      PROP.DOMAIN -> Value.ListValue(List(Value.Integer(1)))
    )

    val validProperties = Map(
      PROP.VAR_TYPE -> Value.Str(PROP_VALUE.LIST_STR_TYPE),
      PROP.DOMAIN -> Value.ListValue(List(Value.Str("a"), Value.Str("b"), Value.Str("c")))
    )

  "ListStrIoVariable.valueForIndex(...)" should:
    "return the correct value for a valid index" in newCase[CaseData]: (_, data) =>
      data.variable.valueForIndex(IoIndex(1)).asserting(_ mustEqual "b")

    "raise an AssertionError for an invalid index" in newCase[CaseData]: (_, data) =>
      data.variable.valueForIndex(IoIndex(3)).assertThrows[AssertionError]

  "ListStrIoVariable.indexForValue(...)" should:
    "return the correct index for a valid value" in newCase[CaseData]: (_, data) =>
      data.variable.indexForValue("b").asserting(_ mustEqual IoIndex(1))

    "raise an AssertionError for an invalid value" in newCase[CaseData]: (_, data) =>
      data.variable.indexForValue("d").assertThrows[AssertionError]

  "ListStrIoVariable.toQueryParams" should:
    "return the correct properties map" in newCase[CaseData]: (tn, data) =>
      data.variable
        .toQueryParams
        .logValue(tn)
        .asserting: params =>
          val acceptableValues = params
            .getOrElse(PROP.DOMAIN, fail(s"${PROP.DOMAIN} should be present"))
            .param.asInstanceOf[java.util.Iterator[java.lang.Boolean]]

          params.size mustEqual 2
          params.get(PROP.VAR_TYPE) mustEqual Some(Param(QueryParam(PROP_VALUE.LIST_STR_TYPE)))
          acceptableValues.asScala.toList mustEqual List("a", "b", "c")

  "ListStrIoVariable.fromProperties(...)" should:
    "create ListStrIoVariable from valid properties" in newCase[CaseData]: (tn, data) =>
      ListStrIoVariable.fromProperties[IO](data.validProperties)
        .logValue(tn)
        .asserting(_.elements mustEqual List("a", "b", "c"))

    "raise an AssertionError for invalid properties" in newCase[CaseData]: (tn, data) =>
      ListStrIoVariable.fromProperties[IO](data.invalidProperties)
        .logValue(tn)
        .assertThrows[AssertionError]
