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

import cats.syntax.all.*
import cats.syntax.ext.*
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
  def conNodesAdded[F[_]: MT](nodes: Set[Node.Con]): F[Unit]

  // Compute the next action for the given observation:
  // Processing of the step can be split into several phases:
  //  1. Find set of concrete nodes base on observed IO values.
  //  2. Activation: find active abstract forest, starting from found concrete nodes.
  //  3. Context update: extend context base active forest, and cleanup inactive part of it.
  //  4. TODO
  def step[F[_]: Async](observation: Observation)(using Scheduler): F[Action]

private[mpi] object Planner:
  type Msg = Actor.Msg

  def spawn[F[_]: MT](
      variables: IoVars,
      ctx: ActorContext[?],
  ): F[Planner] = ApiImpl(Actor.spawn(Definition(variables), (b, n) => ctx.spawn(b, n))).pure
