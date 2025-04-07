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

package planning.engine.core.map.io.node

import cats.effect.IO
import planning.engine.common.UnitSpecIO
import neotypes.model.types.Value
import planning.engine.core.map.io.variable.{BooleanIoVariable, IntIoVariable}
import cats.effect.cps.*
import planning.engine.common.values.Index
import planning.engine.core.map.hidden.node.ConcreteHiddenNode

class IoNodeSpec extends UnitSpecIO:

  private class CaseData extends Case:
    val inputNodeProperties = Map(
      "type" -> Value.Str(InputNode.propertyNodeType),
      "name" -> Value.Str("inputNode"),
      "variable.type" -> Value.Str("bool"),
      "variable.domain" -> Value.ListValue(List(Value.Bool(true), Value.Bool(false)))
    )

    val outputNodeProperties = Map(
      "type" -> Value.Str(OutputNode.propertyNodeType),
      "name" -> Value.Str("outputNode"),
      "variable.type" -> Value.Str("int"),
      "variable.min" -> Value.Integer(0),
      "variable.max" -> Value.Integer(10)
    )

    val invalidNodeTypeProperties = Map(
      "type" -> Value.Str("unknown"),
      "name" -> Value.Str("invalidNode"),
      "variable.type" -> Value.Str("bool"),
      "variable.domain" -> Value.ListValue(List(Value.Bool(true), Value.Bool(false)))
    )

    val missingTypeProperties = Map(
      "name" -> Value.Str("missingTypeNode"),
      "variable.type" -> Value.Str("bool"),
      "variable.domain" -> Value.ListValue(List(Value.Bool(true), Value.Bool(false)))
    )

    def makeInputBoolNode: IO[InputNode[IO]] = InputNode[IO]("inputNode", BooleanIoVariable[IO](Set(true, false)))

    def makeConcreteNode(index: Long, ioNode: IoNode[IO]): IO[ConcreteHiddenNode[IO]] =
      ConcreteHiddenNode[IO](Index(index), ioNode)

  "fromProperties" should:
    "create InputNode from valid input node properties" in newCase[CaseData]: data =>
      IoNode.fromProperties[IO](data.inputNodeProperties)
        .logValue
        .asserting: node =>
          node mustBe a[InputNode[IO]]
          node.name mustEqual "inputNode"
          node.variable mustBe a[BooleanIoVariable[IO]]
          node.variable.asInstanceOf[BooleanIoVariable[IO]].acceptableValues mustEqual Set(true, false)

    "create OutputNode from valid output node properties" in newCase[CaseData]: data =>
      IoNode.fromProperties[IO](data.outputNodeProperties)
        .logValue
        .asserting: node =>
          node mustBe a[OutputNode[IO]]
          node.name mustEqual "outputNode"
          node.variable mustBe a[IntIoVariable[IO]]
          node.variable.asInstanceOf[IntIoVariable[IO]].min mustEqual 0
          node.variable.asInstanceOf[IntIoVariable[IO]].max mustEqual 10

    "raise an AssertionError for invalid node type properties" in newCase[CaseData]: data =>
      IoNode.fromProperties[IO](data.invalidNodeTypeProperties)
        .logValue
        .assertThrows[AssertionError]

    "raise an AssertionError for missing type properties" in newCase[CaseData]: data =>
      IoNode.fromProperties[IO](data.missingTypeProperties)
        .logValue
        .assertThrows[AssertionError]

  "addConcreteHiddenNode" should:
    "add a new hidden node to an empty map" in newCase[CaseData]: data =>
      async[IO]:
        val ioNode: IoNode[IO] = data.makeInputBoolNode.await
        val conNode01: ConcreteHiddenNode[IO] = data.makeConcreteNode(0, ioNode).await
        val conNode02: ConcreteHiddenNode[IO] = data.makeConcreteNode(0, ioNode).await
        val conNode11: ConcreteHiddenNode[IO] = data.makeConcreteNode(1, ioNode).await

        val hiddenNodes = ioNode.getAllConcreteNode.logValue.await

        hiddenNodes mustEqual Map(Index(0) -> Vector(conNode01, conNode02), Index(1) -> Vector(conNode11))

  "toProperties" should:
    "return correct properties map for InputNode" in newCase[CaseData]: data =>
      InputNode[IO]("inputNode", BooleanIoVariable[IO](Set(true, false)))
        .flatMap(_.toProperties)
        .logValue
        .asserting(_ mustEqual data.inputNodeProperties)

    "return correct properties map for OutputNode" in newCase[CaseData]: data =>
      OutputNode[IO]("outputNode", IntIoVariable[IO](0, 10))
        .flatMap(_.toProperties)
        .logValue
        .asserting(_ mustEqual data.outputNodeProperties)
