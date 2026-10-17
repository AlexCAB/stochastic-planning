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

package planning.engine.planner.mpi.actors.planner.logic

import cats.effect.Async
import cats.syntax.all.*
import cats.syntax.ext.*
import org.apache.pekko.actor.typed.Scheduler
import planning.engine.common.graph.io.{Action, Observation}
import planning.engine.planner.mpi.actors.ApiBase
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.node.data.parts.ActivePath
import planning.engine.planner.mpi.actors.planner.Planner
import planning.engine.planner.mpi.actors.planner.data.Message
import planning.engine.planner.mpi.model.data.node.StepKey

private[planner] final case class ApiImpl(actor: Actor.Ref) extends ApiBase[Actor.Msg] with Planner:
  import Message.*

  override def conNodesAdded[F[_]: MT](nodes: Set[Node.Con]): F[Unit] = ifNonEmpty((), nodes):
    actor.tellF(ConNodesAdded(nodes))

  override def tick[F[_]: Async](observation: Observation)(using Scheduler): F[Action] =
    actor.askF[F, TickDone](ref => Tick(observation, ref)).map(_.action)

  override def addActivePath[F[_]: MT](path: ActivePath): F[Unit] = actor.tellF(AddActivePath(path))

  override def nextSteps[F[_]: MT](steps: Set[StepKey]): F[Unit] = actor.tellF(NextSteps(steps))

  override def rootSteps[F[_]: MT](steps: Set[StepKey]): F[Unit] = actor.tellF(RootSteps(steps))

  override lazy val toString: String = s"Planner(path = ${actor.path})"
