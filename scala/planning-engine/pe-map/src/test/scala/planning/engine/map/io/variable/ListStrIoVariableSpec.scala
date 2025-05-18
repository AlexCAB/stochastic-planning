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
import planning.engine.map.io.variable.IoVariable.*
import scala.jdk.CollectionConverters.*

class ListStrIoVariableSpec extends UnitSpecIO:

  private class CaseData extends Case:
    val variable = ListStrIoVariable[IO](List("a", "b", "c"))

    val invalidProperties = Map(
      PROP_NAME.VAR_TYPE -> Value.Str(PROP_VALUE.LIST_STR_TYPE),
      PROP_NAME.DOMAIN -> Value.ListValue(List(Value.Integer(1)))
    )

    val validProperties = Map(
      PROP_NAME.VAR_TYPE -> Value.Str(PROP_VALUE.LIST_STR_TYPE),
      PROP_NAME.DOMAIN -> Value.ListValue(List(Value.Str("a"), Value.Str("b"), Value.Str("c")))
    )

  "valueForIndex" should:
    "return the correct value for a valid index" in newCase[CaseData]: data =>
      data.variable.valueForIndex(IoIndex(1)).asserting(_ mustEqual "b")

    "raise an AssertionError for an invalid index" in newCase[CaseData]: data =>
      data.variable.valueForIndex(IoIndex(3)).assertThrows[AssertionError]

  "indexForValue" should:
    "return the correct index for a valid value" in newCase[CaseData]: data =>
      data.variable.indexForValue("b").asserting(_ mustEqual IoIndex(1))

    "raise an AssertionError for an invalid value" in newCase[CaseData]: data =>
      data.variable.indexForValue("d").assertThrows[AssertionError]

  "toQueryParams" should:
    "return the correct properties map" in newCase[CaseData]: data =>
      data.variable
        .toQueryParams
        .logValue
        .asserting: params =>
          val acceptableValues = params
            .getOrElse(PROP_NAME.DOMAIN, fail(s"${PROP_NAME.DOMAIN} should be present"))
            .param.asInstanceOf[java.util.Iterator[java.lang.Boolean]]

          params.size mustEqual 2
          params.get(PROP_NAME.VAR_TYPE) mustEqual Some(Param(QueryParam(PROP_VALUE.LIST_STR_TYPE)))
          acceptableValues.asScala.toList mustEqual List("a", "b", "c")

  "fromProperties" should:
    "create ListStrIoVariable from valid properties" in newCase[CaseData]: data =>
      ListStrIoVariable.fromProperties[IO](data.validProperties)
        .logValue
        .asserting(_.elements mustEqual List("a", "b", "c"))

    "raise an AssertionError for invalid properties" in newCase[CaseData]: data =>
      ListStrIoVariable.fromProperties[IO](data.invalidProperties)
        .logValue
        .assertThrows[AssertionError]
