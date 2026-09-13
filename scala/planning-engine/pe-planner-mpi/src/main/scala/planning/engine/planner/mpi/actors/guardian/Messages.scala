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

package planning.engine.planner.mpi.actors.guardian

import org.apache.pekko.actor.typed.ActorRef
import planning.engine.planner.mpi.actors.Base.WithSender
import planning.engine.planner.mpi.actors.visualizer.Visualizer
import planning.engine.planner.mpi.map.Visualization
import planning.engine.planner.mpi.model.io.Variable
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.planner.Planner
import planning.engine.planner.mpi.model.repr.Representable

private[guardian] trait Messages:
  sealed trait Command extends Representable
  sealed trait Result extends Representable

  case class Initialize(
      inVars: Set[Variable.Input],
      outVars: Set[Variable.Output],
      visualization: Option[Visualization],
      sender: ActorRef[Initialized],
  ) extends Command with WithSender[Initialized]

  case class Initialized(
      manager: Manager,
      planner: Planner,
      visualizer: Option[Visualizer],
  ) extends Result

  case class Reset(sender: ActorRef[Cleaned.type]) extends Command with WithSender[Cleaned.type]
  case object Cleaned extends Result
