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
import cats.effect.std.AtomicCell
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.UnitSpecIO
import planning.engine.common.values.node.HnId
import planning.engine.common.values.text.Name
import planning.engine.map.hidden.state.node.HiddenNodeState
import planning.engine.map.knowledge.graph.KnowledgeGraphTestData
import planning.engine.map.knowledge.graph.KnowledgeGraphInternal
import cats.effect.cps.*
import cats.syntax.all.*

class HiddenNodeSpec extends UnitSpecIO with AsyncMockFactory with KnowledgeGraphTestData:

  private class CaseData extends Case:
    val mockedKnowledgeGraph = mock[KnowledgeGraphInternal[IO]]

    val hiddenNodeState = HiddenNodeState.init[IO]
      .copy(numberOfUsages = 2L)

    val hiddenNode: HiddenNode[IO] = new HiddenNode[IO]:
      val id = HnId(1234L)
      val name = Some(Name("TestNode"))
      val nodeState = AtomicCell[IO].of[HiddenNodeState[IO]](hiddenNodeState).unsafeRunSync()
      val knowledgeGraph = mockedKnowledgeGraph

  "release" should:
    "decrease `numberOfUsages` until 0, then call graph.releaseHiddenNode" in newCase[CaseData]: data =>
      async[IO]:
        data.mockedKnowledgeGraph.releaseHiddenNode.expects(data.hiddenNode).returns(().pure).once()
        data.hiddenNode.release.await
        data.hiddenNode.getState.await.numberOfUsages mustEqual 1L
        data.hiddenNode.release.await
        data.hiddenNode.getState.await.numberOfUsages mustEqual 0L
