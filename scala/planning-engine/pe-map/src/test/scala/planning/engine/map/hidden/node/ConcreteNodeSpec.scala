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
| created: 2025-05-15 |||||||||||*/

package planning.engine.map.hidden.node

import cats.effect.IO
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.UnitSpecIO
import planning.engine.common.values.node.{HnId, IoIndex}
import planning.engine.common.values.text.Name
import planning.engine.common.properties.PROP_NAME
import neotypes.model.types.Value
import cats.syntax.all.*
import planning.engine.map.io.node.InputNode
import planning.engine.map.io.variable.IntIoVariable
import cats.effect.cps.*
import planning.engine.map.graph.KnowledgeGraphTestData

class ConcreteNodeSpec extends UnitSpecIO with AsyncMockFactory with KnowledgeGraphTestData:

  private class CaseData extends Case:
    val id = HnId(1234L)
    val name = Some(Name("TestNode"))
    val valueIndex = IoIndex(1L)
    val intInNode = InputNode[IO](Name("inputNode"), IntIoVariable[IO](0, 10)).unsafeRunSync()
    val node = ConcreteNode[IO](id, name, intInNode, valueIndex, initHiddenNodeState).unsafeRunSync()
    val properties = Map(
      PROP_NAME.HN_ID -> id.toDbParam,
      PROP_NAME.NAME -> name.get.toDbParam,
      PROP_NAME.NEXT_HN_INEX -> nonEmptyHiddenNodeState.nextHnIndex.toDbParam,
      PROP_NAME.IO_INDEX -> valueIndex.toDbParam
    )

    val values = Map(
      PROP_NAME.HN_ID -> Value.Integer(id.value),
      PROP_NAME.NAME -> Value.Str(name.get.value),
      PROP_NAME.NEXT_HN_INEX -> Value.Integer(nonEmptyHiddenNodeState.nextHnIndex.value),
      PROP_NAME.IO_INDEX -> Value.Integer(valueIndex.value)
    )

  "apply" should:
    "create ConcreteNode with given state" in newCase[CaseData]: data =>
      data.node.pure[IO].logValue.asserting: node =>
        node.id mustEqual data.id
        node.name mustEqual data.name
        node.ioNode mustEqual data.intInNode
        node.valueIndex mustEqual data.valueIndex
        node.getState.unsafeRunSync() mustEqual nonEmptyHiddenNodeState

    "create ConcreteNode with init state" in newCase[CaseData]: data =>
      ConcreteNode[IO](data.id, data.name, boolInNode, data.valueIndex).logValue.asserting: node =>
        node.id mustEqual data.id
        node.name mustEqual data.name
        node.ioNode mustEqual boolInNode
        node.valueIndex mustEqual data.valueIndex
        node.getState.unsafeRunSync() mustEqual initHiddenNodeState

  "fromProperties" should:
    "create ConcreteNode from properties" in newCase[CaseData]: data =>
      ConcreteNode.fromProperties[IO](data.values, boolInNode).asserting: node =>
        node.id mustEqual data.id
        node.name mustEqual data.name
        node.ioNode mustEqual boolInNode
        node.valueIndex mustEqual data.valueIndex
        node.getState.unsafeRunSync() mustEqual nonEmptyHiddenNodeState

  "init and remove" should:
    "initialize ConcreteNode and remove it" in newCase[CaseData]: data =>
      async[IO]:
        val (initNode, initRes) = data.node.init(123.pure).await
        initNode mustEqual data.node
        initRes mustEqual 123

        data.node.ioNode.getState.await mustEqual Map(data.valueIndex -> Map(data.id -> data.node))
        data.node.remove(321.pure).await mustEqual 321
        data.node.ioNode.getState.await mustEqual Map()

  "toProperties" should:
    "make DB node properties" in newCase[CaseData]: data =>
      data.node.toProperties.logValue.asserting: props =>
        props mustEqual data.properties

  "equals" should:
    "return true for same nodes" in newCase[CaseData]: data =>
      ConcreteNode[IO](data.id, data.name, data.intInNode, data.valueIndex).asserting: node2 =>
        data.node.equals(node2) mustEqual true

    "return false for different nodes" in newCase[CaseData]: data =>
      ConcreteNode[IO](HnId(5678), Some(Name("TestNode2")), boolInNode, IoIndex(2)).asserting: node2 =>
        data.node.equals(node2) mustEqual false
