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
| created: 2026-09-09 |||||||||||*/

package planning.engine.planner.mpi.actors.guardian

import cats.effect.{Async, Resource}
import cats.syntax.all.*
import org.apache.pekko.actor.typed.{ActorSystem, Scheduler}
import planning.engine.planner.mpi.Visualization
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.guardian.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.actors.planner.Planner
import planning.engine.planner.mpi.actors.visualizer.Visualizer
import planning.engine.planner.mpi.model.io.Variable

private[mpi] trait Guardian:

  // Initialize the map network, creating manager, planner, and optionally a visualizer.
  // Will fail (and terminate the Guardian) if called more than once without a reset.
  private[mpi] def initialize[F[_]: Async](
      inVars: Set[Variable.Input],
      outVars: Set[Variable.Output],
      visualization: Option[Visualization],
  )(using Scheduler): F[(Manager, Planner, Option[Visualizer])]

  // Reset the map network, stopping all child actors and allowing a new initialization.
  // Calling multiple times has no effect.
  private[mpi] def reset[F[_]: Async]()(using Scheduler): F[Unit]

private[mpi] object Guardian:
  type Msg = Actor.Msg

  def create[F[_]: Async](): Resource[F, (Guardian, Scheduler)] = Resource
    .make(Async[F].delay(ActorSystem(Actor(), Actor.name)))(s => Async[F].delay(s.terminate()).void)
    .map(s => (ApiImpl(s), s.scheduler))
