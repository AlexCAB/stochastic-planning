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
import cats.syntax.all.*
import planning.engine.map.io.node.InputNode
import planning.engine.map.io.variable.IntIoVariable
import neotypes.model.types.{Node, Value}
import planning.engine.map.database.Neo4jQueries.{CONCRETE_LABEL, HN_LABEL}

class ConcreteNodeSpec extends UnitSpecIO with AsyncMockFactory:

  private class CaseData extends Case:
    lazy val id = HnId(1234L)
    lazy val name = Some(Name("TestNode"))
    lazy val valueIndex = IoIndex(1L)
    lazy val intInNode = InputNode[IO](Name("inputNode"), IntIoVariable[IO](0, 10)).unsafeRunSync()
    lazy val singleNode = ConcreteNode[IO](id, name, intInNode, valueIndex).unsafeRunSync()

    lazy val nodeProperties = Map(
      PROP_NAME.HN_ID -> id.toDbParam,
      PROP_NAME.NAME -> name.get.toDbParam,
      PROP_NAME.IO_INDEX -> valueIndex.toDbParam
    )

    lazy val nodeValues = Map(
      PROP_NAME.HN_ID -> Value.Integer(id.value),
      PROP_NAME.NAME -> Value.Str(name.get.value),
      PROP_NAME.IO_INDEX -> Value.Integer(valueIndex.value)
    )

    lazy val rawNode = Node("1", Set(HN_LABEL.s, CONCRETE_LABEL.s), nodeValues)

  "apply" should:
    "create ConcreteNode with given state" in newCase[CaseData]: data =>
      data.singleNode.pure[IO].logValue.asserting: node =>
        node.id mustEqual data.id
        node.name mustEqual data.name
        node.ioNode mustEqual data.intInNode
        node.valueIndex mustEqual data.valueIndex
        node.parents mustEqual List()
        node.children mustEqual List()

  "fromNode" should:
    "create ConcreteNode from raw node" in newCase[CaseData]: data =>
      ConcreteNode.fromNode[IO](data.rawNode, data.intInNode).asserting: node =>
        node.id mustEqual data.id
        node.name mustEqual data.name
        node.ioNode mustEqual data.intInNode
        node.valueIndex mustEqual data.valueIndex
        node.parents mustEqual List()
        node.children mustEqual List()

  "toProperties" should:
    "make DB node properties" in newCase[CaseData]: data =>
      data.singleNode.toProperties.logValue.asserting(_ mustEqual data.nodeProperties)

  "equals" should:
    "return true for same nodes" in newCase[CaseData]: data =>
      ConcreteNode[IO](data.id, data.name, data.intInNode, data.valueIndex).asserting: node2 =>
        data.singleNode.equals(node2) mustEqual true

    "return false for different nodes" in newCase[CaseData]: data =>
      ConcreteNode[IO](HnId(5678), Some(Name("TestNode2")), data.intInNode, IoIndex(2)).asserting: node2 =>
        data.singleNode.equals(node2) mustEqual false
