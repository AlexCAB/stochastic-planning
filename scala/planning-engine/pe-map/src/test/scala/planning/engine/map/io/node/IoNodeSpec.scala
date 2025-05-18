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
import planning.engine.map.hidden.node.ConcreteNode
import planning.engine.map.io.variable.{BooleanIoVariable, IntIoVariable}
import planning.engine.common.properties.QueryParamMapping.*
import cats.effect.cps.*
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.properties.PROP_NAME
import planning.engine.common.values.text.Name
import planning.engine.common.values.node.IoIndex
import planning.engine.common.values.node.HnId
import planning.engine.map.database.Neo4jQueries.IO_NODE_LABEL
import planning.engine.map.hidden.state.node.HiddenNodeState
import planning.engine.map.io.variable.IoVariable.PROP_VALUE.*
import planning.engine.map.knowledge.graph.KnowledgeGraphInternal

class IoNodeSpec extends UnitSpecIO with AsyncMockFactory:

  private class CaseData extends Case:
    private val `variable.varType` = s"${PROP_NAME.VARIABLE}.${PROP_NAME.VAR_TYPE}"
    private val `variable.min` = s"${PROP_NAME.VARIABLE}.${PROP_NAME.MIN}"
    private val `variable.max` = s"${PROP_NAME.VARIABLE}.${PROP_NAME.MAX}"
    private val `variable.domain` = s"${PROP_NAME.VARIABLE}.${PROP_NAME.DOMAIN}"

    val inputNodeProperties = Map(
      PROP_NAME.IO_TYPE -> Value.Str(InputNode.IN_NODE_TYPE),
      PROP_NAME.NAME -> Value.Str("inputNode"),
      `variable.varType` -> Value.Str(INT_TYPE),
      `variable.min` -> Value.Integer(22),
      `variable.max` -> Value.Integer(33)
    )

    val inputNodeQueryParams = inputNodeProperties.map:
      case (k, v) => k -> v.toParam

    val outputNodeProperties = Map(
      PROP_NAME.IO_TYPE -> Value.Str(OutputNode.OUT_NODE_TYPE),
      PROP_NAME.NAME -> Value.Str("outputNode"),
      `variable.varType` -> Value.Str(INT_TYPE),
      `variable.min` -> Value.Integer(0),
      `variable.max` -> Value.Integer(10)
    )

    val outputNodeQueryParams = outputNodeProperties.map:
      case (k, v) => k -> v.toParam

    val invalidNodeTypeProperties = Map(
      PROP_NAME.IO_TYPE -> Value.Str("unknown"),
      PROP_NAME.NAME -> Value.Str("invalidNode"),
      `variable.varType` -> Value.Str(BOOL_TYPE),
      `variable.domain` -> Value.ListValue(List(Value.Bool(true), Value.Bool(false)))
    )

    val missingTypeProperties = Map(
      PROP_NAME.NAME -> Value.Str("missingTypeNode"),
      `variable.varType` -> Value.Str(BOOL_TYPE),
      `variable.domain` -> Value.ListValue(List(Value.Bool(true), Value.Bool(false)))
    )

    val inputDbNode = Node("1", Set(IO_NODE_LABEL), inputNodeProperties)
    val outputDbNode = Node("2", Set(IO_NODE_LABEL), outputNodeProperties)
    
    val mockedKnowledgeGraph = mock[KnowledgeGraphInternal[IO]]

    def makeInputBoolNode: IO[InputNode[IO]] = InputNode[IO](
      Name("inputNode"),
      BooleanIoVariable[IO](Set(true, false))
    )

    def makeConcreteNode(index: Long, ioNode: IoNode[IO]): IO[ConcreteNode[IO]] = ConcreteNode[IO](
      HnId(123),
      Some(Name("test")), 
      ioNode, 
      IoIndex(index), 
      HiddenNodeState.init[IO],
      mockedKnowledgeGraph)

  "fromProperties" should:
    "create InputNode from valid input node properties" in newCase[CaseData]: data =>
      IoNode.fromProperties[IO](data.inputNodeProperties)
        .logValue
        .asserting: node =>
          node mustBe a[InputNode[IO]]
          node.name mustEqual Name("inputNode")
          node.variable mustBe a[IntIoVariable[IO]]
          val variable = node.variable.asInstanceOf[IntIoVariable[IO]]
          variable.min mustEqual 22
          variable.max mustEqual 33

    "create OutputNode from valid output node properties" in newCase[CaseData]: data =>
      IoNode.fromProperties[IO](data.outputNodeProperties)
        .logValue
        .asserting: node =>
          node mustBe a[OutputNode[IO]]
          node.name mustEqual Name("outputNode")
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
        val conNode01: ConcreteNode[IO] = data.makeConcreteNode(0, ioNode).await
        val conNode02: ConcreteNode[IO] = data.makeConcreteNode(0, ioNode).await
        val conNode11: ConcreteNode[IO] = data.makeConcreteNode(1, ioNode).await

        val hiddenNodes = ioNode.getAllConcreteNode.logValue.await

        hiddenNodes mustEqual Map(IoIndex(0) -> List(conNode01, conNode02), IoIndex(1) -> List(conNode11))

  "toQueryParams" should:
    "return correct properties map for InputNode" in newCase[CaseData]: data =>
      InputNode[IO](Name("inputNode"), IntIoVariable[IO](22, 33))
        .flatMap(_.toQueryParams)
        .logValue
        .asserting(_ mustEqual data.inputNodeQueryParams)

    "return correct properties map for OutputNode" in newCase[CaseData]: data =>
      OutputNode[IO](Name("outputNode"), IntIoVariable[IO](0, 10))
        .flatMap(_.toQueryParams)
        .logValue
        .asserting(_ mustEqual data.outputNodeQueryParams)

  "fromNode" should:
    "create InputNode from valid input node" in newCase[CaseData]: data =>
      IoNode.fromNode[IO](data.inputDbNode)
        .logValue
        .asserting: node =>
          node mustBe a[InputNode[IO]]
          node.asInstanceOf[InputNode[IO]].name mustEqual Name("inputNode")

    "create OutputNode from valid input node" in newCase[CaseData]: data =>
      IoNode.fromNode[IO](data.outputDbNode)
        .logValue
        .asserting: node =>
          node mustBe a[OutputNode[IO]]
          node.asInstanceOf[OutputNode[IO]].name mustEqual Name("outputNode")
