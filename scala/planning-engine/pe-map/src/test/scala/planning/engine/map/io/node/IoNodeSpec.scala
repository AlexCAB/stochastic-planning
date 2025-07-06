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
import planning.engine.common.UnitSpecWithData
import neotypes.model.types.{Node, Value}
import planning.engine.map.io.variable.{BooleanIoVariable, IntIoVariable}
import planning.engine.common.properties.PropertiesMapping.*
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.properties.PROP
import planning.engine.common.values.text.Name
import planning.engine.common.values.db.Neo4j.{IN_LABEL, IO_LABEL, OUT_LABEL}
import planning.engine.map.io.variable.IoVariable.PROP_VALUE.*
import cats.syntax.all.*

class IoNodeSpec extends UnitSpecWithData with AsyncMockFactory:

  private class CaseData extends Case:
    lazy val `variable.varType` = s"${PROP.VARIABLE}.${PROP.VAR_TYPE}"
    lazy val `variable.min` = s"${PROP.VARIABLE}.${PROP.MIN}"
    lazy val `variable.max` = s"${PROP.VARIABLE}.${PROP.MAX}"
    lazy val `variable.domain` = s"${PROP.VARIABLE}.${PROP.DOMAIN}"

    lazy val inputNodeProperties = Map(
      PROP.NAME -> Value.Str("inputNode"),
      `variable.varType` -> Value.Str(INT_TYPE),
      `variable.min` -> Value.Integer(22),
      `variable.max` -> Value.Integer(33)
    )

    lazy val outputNodeProperties = Map(
      PROP.NAME -> Value.Str("outputNode"),
      `variable.varType` -> Value.Str(INT_TYPE),
      `variable.min` -> Value.Integer(0),
      `variable.max` -> Value.Integer(10)
    )

    lazy val inputNodeQueryParams = inputNodeProperties.map((k, v) => k -> v.toParam)
    lazy val outputNodeQueryParams = outputNodeProperties.map((k, v) => k -> v.toParam)

    lazy val missingTypeProperties = Map(
      PROP.NAME -> Value.Str("missingTypeNode"),
      `variable.varType` -> Value.Str(BOOL_TYPE),
      `variable.domain` -> Value.ListValue(List(Value.Bool(true), Value.Bool(false)))
    )

    lazy val inputDbNode = Node("1", Set(IO_LABEL, IN_LABEL), inputNodeProperties)
    lazy val outputDbNode = Node("2", Set(IO_LABEL, OUT_LABEL), outputNodeProperties)

    lazy val inputBoolNode = InputNode[IO](Name("inputNode"), BooleanIoVariable[IO](Set(true, false)))

  "IoNode.fromNode(...)" should:
    "create InputNode from valid input DB node" in newCase[CaseData]: (tn, data) =>
      IoNode.fromNode[IO](data.inputDbNode).logValue(tn).asserting: node =>
        node mustBe a[InputNode[IO]]
        node.name mustEqual Name("inputNode")
        node.variable mustBe a[IntIoVariable[IO]]
        val variable = node.variable.asInstanceOf[IntIoVariable[IO]]
        variable.min mustEqual 22
        variable.max mustEqual 33

    "create OutputNode from valid output DB node" in newCase[CaseData]: (tn, data) =>
      IoNode.fromNode[IO](data.outputDbNode).logValue(tn).asserting: node =>
        node mustBe a[OutputNode[IO]]
        node.name mustEqual Name("outputNode")
        node.variable mustBe a[IntIoVariable[IO]]
        node.variable.asInstanceOf[IntIoVariable[IO]].min mustEqual 0
        node.variable.asInstanceOf[IntIoVariable[IO]].max mustEqual 10

  "IoNode.nameFromNode(...)" should:
    "create InputNode from valid input DB node" in newCase[CaseData]: (tn, data) =>
      IoNode.nameFromNode[IO](data.inputDbNode).logValue(tn).asserting: name =>
        name mustEqual Name("inputNode")

  "IoNode.toQueryParams" should:
    "return correct properties map for InputNode" in newCase[CaseData]: (tn, data) =>
      InputNode[IO](Name("inputNode"), IntIoVariable[IO](22, 33)).pure[IO]
        .flatMap(_.toQueryParams)
        .logValue(tn)
        .asserting(_ mustEqual (IN_LABEL, data.inputNodeQueryParams))

    "return correct properties map for OutputNode" in newCase[CaseData]: (tn, data) =>
      OutputNode[IO](Name("outputNode"), IntIoVariable[IO](0, 10)).pure[IO]
        .flatMap(_.toQueryParams)
        .logValue(tn)
        .asserting(_ mustEqual (OUT_LABEL, data.outputNodeQueryParams))

  "IoNode.equals(...)" should:
    "return true for same nodes" in newCase[CaseData]: (_, data) =>
      InputNode[IO](Name("inputNode"), BooleanIoVariable[IO](Set(true, false))).pure[IO]
        .asserting(node2 => data.inputBoolNode.equals(node2) mustEqual true)

    "return false if name different" in newCase[CaseData]: (_, data) =>
      InputNode[IO](Name("otherInputNode"), BooleanIoVariable[IO](Set(true, false))).pure[IO]
        .asserting(node2 => data.inputBoolNode.equals(node2) mustEqual false)

    "return false if variable is different" in newCase[CaseData]: (_, data) =>
      InputNode[IO](Name("inputNode"), BooleanIoVariable[IO](Set(true))).pure[IO]
        .asserting(node2 => data.inputBoolNode.equals(node2) mustEqual false)
