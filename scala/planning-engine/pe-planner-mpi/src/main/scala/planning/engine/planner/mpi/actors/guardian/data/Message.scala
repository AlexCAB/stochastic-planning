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
| created: 2026-09-13 |||||||||||*/

package planning.engine.planner.mpi.actors.guardian.data

import org.apache.pekko.actor.typed.ActorRef
import planning.engine.planner.mpi.Visualization
import planning.engine.planner.mpi.actors.Base.WithSender
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.planner.Planner
import planning.engine.planner.mpi.actors.visualizer.Visualizer
import planning.engine.planner.mpi.model.io.Variable
import planning.engine.planner.mpi.repr.Representable

private[guardian] sealed trait Message extends Representable

private[guardian] object Message:
  sealed trait Command[R] extends Message with WithSender[R]
  sealed trait Result

  final case class Initialize(
      inVars: Set[Variable.Input],
      outVars: Set[Variable.Output],
      visualization: Option[Visualization],
      sender: ActorRef[Initialized],
  ) extends Command[Initialized]

  final case class Initialized(
      manager: Manager,
      planner: Planner,
      visualizer: Option[Visualizer],
  ) extends Result

  final case class Reset(sender: ActorRef[Cleaned.type]) extends Command[Cleaned.type]
  case object Cleaned extends Result
