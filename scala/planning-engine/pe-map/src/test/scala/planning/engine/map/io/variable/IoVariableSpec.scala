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
import planning.engine.common.UnitSpecWithData
import neotypes.model.types.Value
import planning.engine.common.properties.PROP
import planning.engine.map.io.variable.IoVariable.PROP_VALUE.*

class IoVariableSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    val boolProperties = Map(
      PROP.VAR_TYPE -> Value.Str(BOOL_TYPE),
      PROP.DOMAIN -> Value.ListValue(List(Value.Bool(true), Value.Bool(false)))
    )

    val intProperties = Map(
      PROP.VAR_TYPE -> Value.Str(INT_TYPE),
      PROP.MIN -> Value.Integer(0),
      PROP.MAX -> Value.Integer(10)
    )

    val floatProperties = Map(
      PROP.VAR_TYPE -> Value.Str(FLOAT_TYPE),
      PROP.MIN -> Value.Decimal(0.0),
      PROP.MAX -> Value.Decimal(10.0)
    )

    val listStrProperties = Map(
      PROP.VAR_TYPE -> Value.Str(LIST_STR_TYPE),
      PROP.DOMAIN -> Value.ListValue(List(Value.Str("a"), Value.Str("b"), Value.Str("c")))
    )

    val invalidTypeProperties = Map(
      PROP.VAR_TYPE -> Value.Str("unknown")
    )

    val missingTypeProperties = Map(
      PROP.DOMAIN -> Value.ListValue(List(Value.Str("a"), Value.Str("b"), Value.Str("c")))
    )

  "fromProperties" should:
    "create BooleanIoVariable from valid bool properties" in newCase[CaseData]: (tn, data) =>
      IoVariable.fromProperties[IO](data.boolProperties)
        .logValue(tn)
        .asserting(_ mustBe a[BooleanIoVariable[IO]])

    "create IntIoVariable from valid int properties" in newCase[CaseData]: (tn, data) =>
      IoVariable.fromProperties[IO](data.intProperties)
        .logValue(tn)
        .asserting(_ mustBe a[IntIoVariable[IO]])

    "create FloatIoVariable from valid float properties" in newCase[CaseData]: (tn, data) =>
      IoVariable.fromProperties[IO](data.floatProperties)
        .logValue(tn)
        .asserting(_ mustBe a[FloatIoVariable[IO]])

    "create ListStrIoVariable from valid list-str properties" in newCase[CaseData]: (tn, data) =>
      IoVariable.fromProperties[IO](data.listStrProperties)
        .logValue(tn)
        .asserting(_ mustBe a[ListStrIoVariable[IO]])

    "raise an AssertionError for invalid type properties" in newCase[CaseData]: (tn, data) =>
      IoVariable.fromProperties[IO](data.invalidTypeProperties)
        .logValue(tn)
        .assertThrows[AssertionError]

    "raise an AssertionError for missing type properties" in newCase[CaseData]: (tn, data) =>
      IoVariable.fromProperties[IO](data.missingTypeProperties)
        .logValue(tn)
        .assertThrows[AssertionError]
