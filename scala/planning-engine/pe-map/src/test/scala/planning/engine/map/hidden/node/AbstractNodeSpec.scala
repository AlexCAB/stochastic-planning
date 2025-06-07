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
import planning.engine.common.values.text.Name
import planning.engine.common.values.node.HnId
import planning.engine.common.properties.PROP_NAME
import cats.syntax.all.*
import neotypes.model.types.{Node, Value}
import planning.engine.map.database.Neo4jQueries.{ABSTRACT_LABEL, HN_LABEL}

class AbstractNodeSpec extends UnitSpecIO with AsyncMockFactory:

  private class CaseData extends Case:
    lazy val id = HnId(1234)
    lazy val name = Some(Name("TestNode"))
    lazy val singleNode = AbstractNode[IO](id, name).unsafeRunSync()

    lazy  val nodeProperties = Map(
      PROP_NAME.HN_ID -> id.toDbParam,
      PROP_NAME.NAME -> name.get.toDbParam
    )

    lazy val nodeValues = Map(
      PROP_NAME.HN_ID -> Value.Integer(id.value),
      PROP_NAME.NAME -> Value.Str(name.get.value)
    )

    lazy val rawNode = Node("1", Set(HN_LABEL.s, ABSTRACT_LABEL.s), nodeValues)

  "apply" should:
    "create single AbstractNode with given params" in newCase[CaseData]: data =>
      data.singleNode.pure[IO].logValue.asserting: node =>
        node.id mustEqual data.id
        node.name mustEqual data.name
        node.parents mustEqual List()
        node.children mustEqual List()

  "fromNode" should:
    "create AbstractNode from raw node" in newCase[CaseData]: data =>
      AbstractNode.fromNode[IO](data.rawNode).asserting: node =>
        node.id mustEqual data.id
        node.name mustEqual data.name
        node.parents mustEqual List()
        node.children mustEqual List()

  "toProperties" should:
    "make DB node properties" in newCase[CaseData]: data =>
      data.singleNode.toProperties.logValue.asserting(_ mustEqual data.nodeProperties)

  "equals" should:
    "return true for same nodes" in newCase[CaseData]: data =>
      AbstractNode[IO](data.id, data.name).asserting: node2 =>
        data.singleNode.equals(node2) mustEqual true

    "return false for different nodes" in newCase[CaseData]: data =>
      AbstractNode[IO](HnId(5678), Some(Name("TestNode2"))).asserting: node2 =>
        data.singleNode.equals(node2) mustEqual false
