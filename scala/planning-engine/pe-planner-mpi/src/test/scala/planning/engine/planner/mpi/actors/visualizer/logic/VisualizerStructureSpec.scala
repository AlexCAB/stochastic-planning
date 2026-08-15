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
| created: 14.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.visualizer.logic

import cats.effect.IO
import cats.effect.cps.*
import org.scalatest.Assertion
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.graph.edges.MeKey.{Link, Then}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.visualizer.{TestVisualizer, WithTestVisualizer}

class VisualizerStructureSpec extends UnitSpecWithIOAndTestKit with WithTestVisualizer:
  private class CaseData extends Case with WithVisualizer:
    val conId: MnId.Con = MnId.Con(1L)
    val absId: MnId.Abs = MnId.Abs(2L)

    val conName: Option[HnName] = Some(HnName("Test Con Node"))
    val absName: Option[HnName] = None

    val linkKey: Link = Link(conId, absId)
    val thenKey: Then = Then(absId, conId)

    def checkVisualizerState(
        visualizer: TestVisualizer,
        expConNodes: Map[MnId.Con, Option[HnName]] = Map.empty,
        expAbsNodes: Map[MnId.Abs, Option[HnName]] = Map.empty,
        expSrcLinkMap: Map[MnId, Set[Link.End]] = Map.empty,
        expSrcThenMap: Map[MnId, Set[Then.End]] = Map.empty,
        expTrgLinkMap: Map[MnId, Set[Link.End]] = Map.empty,
        expTrgThenMap: Map[MnId, Set[Then.End]] = Map.empty,
    ): Assertion =
      val (conNodes, absNodes, srcLinkMap, srcThenMap, trgLinkMap, trgThenMap) = visualizer.state

      conNodes mustBe expConNodes
      absNodes mustBe expAbsNodes
      srcLinkMap mustBe expSrcLinkMap
      srcThenMap mustBe expSrcThenMap
      trgLinkMap mustBe expTrgLinkMap
      trgThenMap mustBe expTrgThenMap

  "Visualizer.nodesAdded" should:
    "log the added Concrete node with its ID and name" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        visualizer.api.nodesAdded[IO](Map(conId -> conName, absId -> absName)).logValue(tn).await

        checkVisualizerState(visualizer, expConNodes = Map(conId -> conName), expAbsNodes = Map(absId -> absName))

    "log the added Abstract node with its ID and unnamed marker" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        visualizer.api.nodesAdded[IO](Map(conId -> conName, absId -> absName)).logValue(tn).await

        checkVisualizerState(visualizer, expConNodes = Map(conId -> conName), expAbsNodes = Map(absId -> absName))

  "Visualizer.edgesAdded" should:
    "log the added edge keys" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val keys: Set[MeKey] = Set(linkKey, thenKey)
        visualizer.api.edgesAdded[IO](keys).logValue(tn).await

        checkVisualizerState(
          visualizer,
          expSrcLinkMap = Map(conId -> Set(linkKey.trgEnd)),
          expSrcThenMap = Map(absId -> Set(thenKey.trgEnd)),
          expTrgLinkMap = Map(absId -> Set(linkKey.srcEnd)),
          expTrgThenMap = Map(conId -> Set(thenKey.srcEnd)),
        )
