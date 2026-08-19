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
| created: 18.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.manager.data

import org.apache.pekko.actor.typed.ActorRef
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.io.IoValue
import planning.engine.common.values.node.MnId
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.ActorBase.WithSender
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.common.data.edge.EdgeData
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.common.data.samples.Sample
import planning.engine.planner.mpi.common.repr.Representable

private[manager] sealed trait Message extends Representable

private[manager] object Message:

  // Synchronous command sent to Manager. Reply with type Result is expected to be sent back to the sender.
  sealed trait Command[R] extends Message with WithSender[R]
  sealed trait Result

  final case class AddNode(data: NodeData, sender: ActorRef[NodeAdded]) extends Command[NodeAdded]
  final case class NodeAdded(id: MnId) extends Result

  final case class AddEdge(key: MeKey, data: EdgeData, sender: ActorRef[EdgeAdded]) extends Command[EdgeAdded]
  final case class EdgeAdded(key: MeKey) extends Result

  final case class AddManSamples(
      samples: Set[Sample.Man],
      nodes: Map[MnId.Nim, NodeData],
      sender: ActorRef[ManSamplesAdded],
  ) extends Command[ManSamplesAdded]

  final case class ManSamplesAdded(samples: Map[SampleId, Sample.Man]) extends Result

  final case class AddGenSamples(
      samples: Set[Sample.Gen],
      newNodes: Map[MnId.Nim, Option[IoValue]],
      sender: ActorRef[GenSamplesAdded],
  ) extends Command[GenSamplesAdded]

  final case class GenSamplesAdded(samples: Map[SampleId, Sample.Gen]) extends Result

  // Sent from NodeActor to ManagerActor in case of any error.
  final case class NodeActorError(nodeRef: Node, msg: Option[Representable], err: Throwable) extends Message
