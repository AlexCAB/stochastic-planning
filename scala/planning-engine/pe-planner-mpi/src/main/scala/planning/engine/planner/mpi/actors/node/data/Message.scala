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

package planning.engine.planner.mpi.actors.node.data

import planning.engine.common.values.io.IoTime
import planning.engine.common.values.plan.Depth
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.Base.WithSender
import planning.engine.planner.mpi.model.data.edge.MeRef
import planning.engine.planner.mpi.model.data.node.StepKey
import planning.engine.planner.mpi.model.data.samples.Sample
import planning.engine.planner.mpi.repr.Representable

private[node] sealed trait Message extends Representable

private[node] object Message:

  // Synchronous command sent to Manager. Reply with type Result is expected to be sent back to the sender.
  sealed trait Command[R] extends Message with WithSender[R]
  sealed trait Result extends Representable

  sealed trait AddEdge extends Message:
    def ref: MeRef

  final case class UpsertEdgeSrc(ref: MeRef, props: Map[SampleId, Sample.Props]) extends AddEdge

  final case class UpsertEdgeTrg(ref: MeRef, props: Map[SampleId, Sample.Props]) extends AddEdge

  // Messages which propagate activation and planning signals through the Plan Tree.
  final case class Activation(bottom: StepKey, up: StepKey, time: IoTime) extends Message

  final case class ContextExtend(prev: StepKey, next: StepKey, nextTime: IoTime) extends Message

  final case class ContextShrink(prev: StepKey, depth: Depth) extends Message

  final case class PathCut(prev: StepKey) extends Message

  final case class TreeCut(next: StepKey) extends Message

  final case class Inference(prev: StepKey, next: StepKey) extends Message

  final case class PlanExtend(prev: StepKey, next: StepKey) extends Message
