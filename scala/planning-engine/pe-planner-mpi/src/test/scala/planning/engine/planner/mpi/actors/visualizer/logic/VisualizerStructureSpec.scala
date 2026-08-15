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
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.graph.edges.MeKey.{Link, Then}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.actors.visualizer.TestVisualizer

class VisualizerStructureSpec extends UnitSpecWithIOAndTestKit with TestVisualizer:
  private class CaseData extends Case with WithVisualizerActor:
    val conId: MnId.Con = MnId.Con(1L)
    val absId: MnId.Abs = MnId.Abs(2L) 

    val conName: Option[HnName] = Some(HnName("Test Con Node"))
    val absName: Option[HnName] = None

    val linkKey: Link = Link(conId, absId)
    val thenKey: Then = Then(absId, conId)

  "Visualizer.nodesAdded" should:
    "log the added Concrete node with its ID and name" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        visualizer.nodesAdded[IO](Map(conId -> conName, absId -> absName)).logValue(tn).await
        succeed

    "log the added Abstract node with its ID and unnamed marker" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        visualizer.nodesAdded[IO](Map(conId -> conName, absId -> absName)).logValue(tn).await
        succeed

  "Visualizer.edgesAdded" should:
    "log the added edge keys" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val keys: Set[MeKey] = Set(linkKey, thenKey)
        visualizer.edgesAdded[IO](keys).logValue(tn).await
        succeed