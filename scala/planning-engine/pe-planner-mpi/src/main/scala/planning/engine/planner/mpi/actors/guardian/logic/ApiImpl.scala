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

package planning.engine.planner.mpi.actors.guardian.logic

import cats.effect.Async
import cats.syntax.all.*
import org.apache.pekko.actor.typed.Scheduler
import planning.engine.planner.mpi.Visualization
import planning.engine.planner.mpi.actors.ApiBase
import planning.engine.planner.mpi.actors.guardian.Guardian
import planning.engine.planner.mpi.actors.guardian.data.Message
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.planner.Planner
import planning.engine.planner.mpi.actors.visualizer.Visualizer
import planning.engine.planner.mpi.model.io.Variable

private[guardian] final case class ApiImpl(actor: Actor.Ref) extends ApiBase[Actor.Msg] with Guardian:
  import Message.*

  override def initialize[F[_]: Async](
      inVars: Set[Variable.Input],
      outVars: Set[Variable.Output],
      visualization: Option[Visualization],
  )(using Scheduler): F[(Manager, Planner, Option[Visualizer])] = actor
    .askF[F, Initialized](ref => Initialize(inVars, outVars, visualization, ref))
    .map(i => (i.manager, i.planner, i.visualizer))

  override def reset[F[_]: Async]()(using Scheduler): F[Unit] = actor.askF[F, Cleaned.type](ref => Reset(ref)).void

  override lazy val toString: String = s"Guardian(path = ${actor.path})"
