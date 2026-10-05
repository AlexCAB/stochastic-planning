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
| created: 31.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.planner

import cats.MonadThrow
import cats.syntax.all.*
import cats.effect.Async
import org.apache.pekko.actor.typed.Scheduler
import org.apache.pekko.actor.typed.scaladsl.ActorContext
import planning.engine.common.graph.io.{Action, Observation}
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.planner.data.Definition
import planning.engine.planner.mpi.actors.planner.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.model.io.IoVars

private[mpi] trait Planner:
  // Notify the planner that a new concrete node was added to the map network.
  def conNodesAdded[F[_]: MonadThrow](nodes: Set[Node.Con]): F[Unit]

  // Compute the next action for the given observation.
  def step[F[_]: Async](observation: Observation)(using Scheduler): F[Action]

private[mpi] object Planner:
  type Msg = Actor.Msg

  def spawn[F[_]: MonadThrow](
      variables: IoVars,
      ctx: ActorContext[?],
  ): F[Planner] = ApiImpl(Actor.spawn(Definition(variables), (b, n) => ctx.spawn(b, n))).pure
