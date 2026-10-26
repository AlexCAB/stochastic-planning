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
| created: 09.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.visualizer.data

import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.io.IoTime
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.Base.WithSender
import planning.engine.planner.mpi.actors.planner.data.parts.ActiveSteps
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.planner.mpi.model.data.samples.Sample
import planning.engine.planner.mpi.repr.Representable

private[visualizer] sealed trait Message extends Representable

private[visualizer] object Message:

  // Synchronous command sent to visualizer. Reply with type Result is expected to be sent back to the sender.
  sealed trait Command[R] extends Message with WithSender[R]
  sealed trait Result

  // Sent from ManagerActor after new nodes were added to the map network.
  final case class ShowNodesAdded(ids: Map[MnId, NodeData]) extends Message

  // Sent from ManagerActor after edges were upserted in the map network.
  final case class ShowEdgesAdded(keys: Set[MeKey]) extends Message

  // Sent from ManagerActor after new samples were added to the map network.
  final case class ShowSamplesAdded(samples: Set[Sample]) extends Message

  // Sent from PlannerActor after active steps were found in the map network.
  final case class ShowActiveStepsFound(time: IoTime, steps: ActiveSteps) extends Message
