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
| created: 2025-03-31 |||||||||||*/

package planning.engine.map.io.node

import cats.effect.IO
import planning.engine.common.UnitSpecIO
import neotypes.model.types.{Node, Value}
import planning.engine.map.io.variable.{BooleanIoVariable, IntIoVariable}
import planning.engine.common.properties.PropertiesMapping.*
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.properties.PROP_NAME
import planning.engine.common.values.text.Name
import planning.engine.map.database.Neo4jQueries.{IN_LABEL, IO_LABEL, OUT_LABEL}
import planning.engine.map.io.variable.IoVariable.PROP_VALUE.*

class IoNodeSpec extends UnitSpecIO with AsyncMockFactory:

  private class CaseData extends Case:
    lazy val `variable.varType` = s"${PROP_NAME.VARIABLE}.${PROP_NAME.VAR_TYPE}"
    lazy val `variable.min` = s"${PROP_NAME.VARIABLE}.${PROP_NAME.MIN}"
    lazy val `variable.max` = s"${PROP_NAME.VARIABLE}.${PROP_NAME.MAX}"
    lazy val `variable.domain` = s"${PROP_NAME.VARIABLE}.${PROP_NAME.DOMAIN}"

    lazy val inputNodeProperties = Map(
      PROP_NAME.NAME -> Value.Str("inputNode"),
      `variable.varType` -> Value.Str(INT_TYPE),
      `variable.min` -> Value.Integer(22),
      `variable.max` -> Value.Integer(33)
    )

    lazy val outputNodeProperties = Map(
      PROP_NAME.NAME -> Value.Str("outputNode"),
      `variable.varType` -> Value.Str(INT_TYPE),
      `variable.min` -> Value.Integer(0),
      `variable.max` -> Value.Integer(10)
    )

    lazy val inputNodeQueryParams = inputNodeProperties.map((k, v) => k -> v.toParam)
    lazy val outputNodeQueryParams = outputNodeProperties.map((k, v) => k -> v.toParam)

    lazy val missingTypeProperties = Map(
      PROP_NAME.NAME -> Value.Str("missingTypeNode"),
      `variable.varType` -> Value.Str(BOOL_TYPE),
      `variable.domain` -> Value.ListValue(List(Value.Bool(true), Value.Bool(false)))
    )

    lazy val inputDbNode = Node("1", Set(IO_LABEL.s, IN_LABEL.s), inputNodeProperties)
    lazy val outputDbNode = Node("2", Set(IO_LABEL.s, OUT_LABEL.s), outputNodeProperties)

    lazy val inputBoolNode = InputNode[IO]
      .apply(Name("inputNode"), BooleanIoVariable[IO](Set(true, false)))
      .unsafeRunSync()

  "fromNode" should:
    "create InputNode from valid input DB node" in newCase[CaseData]: data =>
      IoNode.fromNode[IO](data.inputDbNode).logValue.asserting: node =>
        node mustBe a[InputNode[IO]]
        node.name mustEqual Name("inputNode")
        node.variable mustBe a[IntIoVariable[IO]]
        val variable = node.variable.asInstanceOf[IntIoVariable[IO]]
        variable.min mustEqual 22
        variable.max mustEqual 33

    "create OutputNode from valid output DB node" in newCase[CaseData]: data =>
      IoNode.fromNode[IO](data.outputDbNode).logValue.asserting: node =>
        node mustBe a[OutputNode[IO]]
        node.name mustEqual Name("outputNode")
        node.variable mustBe a[IntIoVariable[IO]]
        node.variable.asInstanceOf[IntIoVariable[IO]].min mustEqual 0
        node.variable.asInstanceOf[IntIoVariable[IO]].max mustEqual 10

  "nameFromNode" should:
    "create InputNode from valid input DB node" in newCase[CaseData]: data =>
      IoNode.nameFromNode[IO](data.inputDbNode).logValue.asserting: name =>
        name mustEqual Name("inputNode")

  "toQueryParams" should:
    "return correct properties map for InputNode" in newCase[CaseData]: data =>
      InputNode[IO](Name("inputNode"), IntIoVariable[IO](22, 33))
        .flatMap(_.toQueryParams)
        .logValue
        .asserting(_ mustEqual (IN_LABEL, data.inputNodeQueryParams))

    "return correct properties map for OutputNode" in newCase[CaseData]: data =>
      OutputNode[IO](Name("outputNode"), IntIoVariable[IO](0, 10))
        .flatMap(_.toQueryParams)
        .logValue
        .asserting(_ mustEqual (OUT_LABEL, data.outputNodeQueryParams))

  "equals" should:
    "return true for same nodes" in newCase[CaseData]: data =>
      InputNode[IO](Name("inputNode"), BooleanIoVariable[IO](Set(true, false)))
        .asserting(node2 => data.inputBoolNode.equals(node2) mustEqual true)

    "return false if name different" in newCase[CaseData]: data =>
      InputNode[IO](Name("otherInputNode"), BooleanIoVariable[IO](Set(true, false)))
        .asserting(node2 => data.inputBoolNode.equals(node2) mustEqual false)

    "return false if variable is different" in newCase[CaseData]: data =>
      InputNode[IO](Name("inputNode"), BooleanIoVariable[IO](Set(true)))
        .asserting(node2 => data.inputBoolNode.equals(node2) mustEqual false)
