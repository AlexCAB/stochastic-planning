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
import cats.effect.Async
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import planning.engine.common.graph.io.{Action, Observation}
import planning.engine.common.values.io.IoName
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.planner.data.Definition
import planning.engine.planner.mpi.actors.planner.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.common.io.{InputVariable, OutputVariable}

trait Planner:
  // Compute the next action for the given observation.
  def step[F[_]: Async](observation: Observation)(using ActorSystem[?]): F[Action]

  // Notify the planner that a new concrete node was added to the map network.
  def conNodesAdded[F[_]: MonadThrow](nodes: Map[MnId.Con, Node]): F[Unit]

object Planner:
  type Msg = Actor.Msg

  def spawn[F[_]: MonadThrow](
      inVar: Map[IoName, InputVariable],
      outVar: Map[IoName, OutputVariable],
      make: (Behavior[Msg], String) => ActorRef[Msg],
  ): F[Planner] = MonadThrow[F].catchNonFatal(ApiImpl(Actor.spawn(Definition(inVar, outVar), make)))
