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
import planning.engine.planner.mpi.actors.guardian.data.Definition
import planning.engine.planner.mpi.actors.guardian.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.planner.Planner
import planning.engine.planner.mpi.actors.visualizer.Visualizer
import planning.engine.planner.mpi.model.data.map.Metadata
import planning.engine.planner.mpi.model.io.IoVars

private[mpi] trait Guardian:

  // Initialize the map network, creating manager, planner, and optionally a visualizer.
  // Will fail (and terminate the Guardian) if called more than once without a reset.
  private[mpi] def initialize[F[_]: Async](
      metadata: Metadata,
      variables: IoVars,
  )(using Scheduler): F[(Manager, Planner, Option[Visualizer])]

  // Reset the map network, stopping all child actors and allowing a new initialization.
  // Calling multiple times has no effect.
  private[mpi] def reset[F[_]: Async]()(using Scheduler): F[Unit]

private[mpi] object Guardian:
  type Msg = Actor.Msg

  // `terminate()` only starts the shutdown, so wait until all actors stopped (and their `PostStop` handled),
  // otherwise they outlive the resources acquired before this one (for example the `Visualization` dependencies).
  private def terminate[F[_]: Async](system: ActorSystem[Msg]): F[Unit] =
    for
      _ <- Async[F].delay(system.terminate())
      _ <- Async[F].fromFuture(Async[F].delay(system.whenTerminated))
    yield ()

  def create[F[_]: Async](viz: Option[Visualization]): Resource[F, (Guardian, Scheduler)] = Resource
    .make(Async[F].delay(ActorSystem(Actor(Definition(viz)), Actor.name)))(terminate)
    .map(s => (ApiImpl(s), s.scheduler))
