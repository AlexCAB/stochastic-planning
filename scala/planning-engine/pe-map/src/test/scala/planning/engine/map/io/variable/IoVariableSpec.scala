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
import planning.engine.common.UnitSpecIO
import neotypes.model.types.Value
import planning.engine.common.properties.PROP_NAME
import planning.engine.map.io.variable.IoVariable.PROP_VALUE.*

class IoVariableSpec extends UnitSpecIO:

  private class CaseData extends Case:
    val boolProperties = Map(
      PROP_NAME.VAR_TYPE -> Value.Str(BOOL_TYPE),
      PROP_NAME.DOMAIN -> Value.ListValue(List(Value.Bool(true), Value.Bool(false)))
    )

    val intProperties = Map(
      PROP_NAME.VAR_TYPE -> Value.Str(INT_TYPE),
      PROP_NAME.MIN -> Value.Integer(0),
      PROP_NAME.MAX -> Value.Integer(10)
    )

    val floatProperties = Map(
      PROP_NAME.VAR_TYPE -> Value.Str(FLOAT_TYPE),
      PROP_NAME.MIN -> Value.Decimal(0.0),
      PROP_NAME.MAX -> Value.Decimal(10.0)
    )

    val listStrProperties = Map(
      PROP_NAME.VAR_TYPE -> Value.Str(LIST_STR_TYPE),
      PROP_NAME.DOMAIN -> Value.ListValue(List(Value.Str("a"), Value.Str("b"), Value.Str("c")))
    )

    val invalidTypeProperties = Map(
      PROP_NAME.VAR_TYPE -> Value.Str("unknown")
    )

    val missingTypeProperties = Map(
      PROP_NAME.DOMAIN -> Value.ListValue(List(Value.Str("a"), Value.Str("b"), Value.Str("c")))
    )

  "fromProperties" should:
    "create BooleanIoVariable from valid bool properties" in newCase[CaseData]: data =>
      IoVariable.fromProperties[IO](data.boolProperties)
        .logValue
        .asserting(_ mustBe a[BooleanIoVariable[IO]])

    "create IntIoVariable from valid int properties" in newCase[CaseData]: data =>
      IoVariable.fromProperties[IO](data.intProperties)
        .logValue
        .asserting(_ mustBe a[IntIoVariable[IO]])

    "create FloatIoVariable from valid float properties" in newCase[CaseData]: data =>
      IoVariable.fromProperties[IO](data.floatProperties)
        .logValue
        .asserting(_ mustBe a[FloatIoVariable[IO]])

    "create ListStrIoVariable from valid list-str properties" in newCase[CaseData]: data =>
      IoVariable.fromProperties[IO](data.listStrProperties)
        .logValue
        .asserting(_ mustBe a[ListStrIoVariable[IO]])

    "raise an AssertionError for invalid type properties" in newCase[CaseData]: data =>
      IoVariable.fromProperties[IO](data.invalidTypeProperties)
        .logValue
        .assertThrows[AssertionError]

    "raise an AssertionError for missing type properties" in newCase[CaseData]: data =>
      IoVariable.fromProperties[IO](data.missingTypeProperties)
        .logValue
        .assertThrows[AssertionError]
