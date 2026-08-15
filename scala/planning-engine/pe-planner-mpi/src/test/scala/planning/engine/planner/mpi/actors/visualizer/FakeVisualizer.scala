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
| created: 11.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.visualizer

import org.apache.pekko.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.graph.edges.MeKey.{Link, Then}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.FakeActorBase
import planning.engine.planner.mpi.actors.visualizer.data.Message.{ShowNodesAdded, ShowEdgesAdded}
import planning.engine.planner.mpi.actors.visualizer.data.State
import planning.engine.planner.mpi.actors.visualizer.logic.ApiImpl

final case class FakeVisualizer(api: Visualizer, probe: TestProbe[Visualizer.Msg]) extends FakeActorBase:
  def getState(using testKit: ActorTestKit): FakeVisualizer.VisualizerState = Tuple.fromProductTyped(getActorState[State])

  def expectShowNodesAdded: Map[MnId, Option[HnName]] = probe.expectMessageType[ShowNodesAdded].ids

  def expectShowEdgesAdded: Set[MeKey] = probe.expectMessageType[ShowEdgesAdded].keys

object FakeVisualizer:
  type VisualizerState = (
      Map[MnId.Con, Option[HnName]],
      Map[MnId.Abs, Option[HnName]],
      Map[MnId, Set[Link.End]],
      Map[MnId, Set[Then.End]],
      Map[MnId, Set[Link.End]],
      Map[MnId, Set[Then.End]],
  )

  def apply()(using testKit: ActorTestKit): FakeVisualizer =
    val probe = testKit.createTestProbe[Visualizer.Msg]("FakeVisualizerProbe")
    FakeVisualizer(ApiImpl(probe.ref), probe)
