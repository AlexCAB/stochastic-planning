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
import planning.engine.map.knowledge.graph.KnowledgeGraphTestData
import planning.engine.map.knowledge.graph.KnowledgeGraphInternal
import planning.engine.common.properties.PROP_NAME
import cats.effect.cps.*

class ConcreteNodeSpec extends UnitSpecIO with AsyncMockFactory with KnowledgeGraphTestData:

  private class CaseData extends Case:
    val id = HnId(1234L)
    val name = Some(Name("TestNode"))
    val ioValueIndex = IoIndex(1L)
    val mockedKnowledgeGraph = mock[KnowledgeGraphInternal[IO]]

  "apply" should:
    "create ConcreteNode" in newCase[CaseData]: data =>
      async[IO]:
        val node = ConcreteNode[IO](
          data.id,
          data.name,
          boolInNode,
          data.ioValueIndex,
          emptyHiddenNodeState,
          data.mockedKnowledgeGraph
        ).await

        node.id mustEqual data.id
        node.name mustEqual data.name
        node.ioNode mustEqual boolInNode
        node.valueIndex mustEqual data.ioValueIndex

  "makeDbParams" should:
    "ake DB node properties" in newCase[CaseData]: data =>
      ConcreteNode.makeDbParams[IO](data.id, data.name, data.ioValueIndex)
        .logValue
        .asserting(_ mustEqual Map(
          PROP_NAME.HN_ID -> data.id.toDbParam,
          PROP_NAME.NAME -> data.name.get.toDbParam,
          PROP_NAME.IO_INDEX -> data.ioValueIndex.toDbParam
        ))
