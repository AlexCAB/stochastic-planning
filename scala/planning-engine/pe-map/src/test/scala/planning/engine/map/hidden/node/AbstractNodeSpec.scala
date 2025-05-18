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
import planning.engine.map.knowledge.graph.KnowledgeGraphTestData
import planning.engine.map.knowledge.graph.KnowledgeGraphInternal

class AbstractNodeSpec extends UnitSpecIO with AsyncMockFactory with KnowledgeGraphTestData:

  private class CaseData extends Case:
    val id = HnId(1234)
    val name = Some(Name("TestNode"))
    val mockedKnowledgeGraph = mock[KnowledgeGraphInternal[IO]]

  "apply" should:
    "create AbstractNode" in newCase[CaseData]: data =>
      AbstractNode
        .apply[IO](data.id, data.name, emptyHiddenNodeState, data.mockedKnowledgeGraph)
        .logValue
        .asserting: node =>
          node.id mustEqual data.id
          node.name mustEqual data.name

  "makeDbParams" should:
    "make DB node properties" in newCase[CaseData]: data =>
      AbstractNode.makeDbParams[IO](data.id, data.name)
        .logValue
        .asserting(_ mustEqual Map(
          PROP_NAME.HN_ID -> data.id.toDbParam,
          PROP_NAME.NAME -> data.name.get.toDbParam
        ))
