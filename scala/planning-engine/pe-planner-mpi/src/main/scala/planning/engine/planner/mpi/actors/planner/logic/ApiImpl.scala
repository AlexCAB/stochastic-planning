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

import cats.MonadThrow
import cats.effect.Async
import cats.syntax.all.*
import cats.syntax.ext.*
import org.apache.pekko.actor.typed.Scheduler
import planning.engine.common.graph.io.{Action, Observation}
import planning.engine.planner.mpi.actors.ApiBase
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.planner.Planner
import planning.engine.planner.mpi.actors.planner.data.Message

private[planner] final case class ApiImpl(actor: Actor.Ref) extends ApiBase[Actor.Msg] with Planner:
  import Message.*

  override def conNodesAdded[F[_]: MonadThrow](nodes: Set[Node.Con]): F[Unit] = ifNonEmpty((), nodes):
    actor.tellF(ConNodesAdded(nodes))

  override def step[F[_]: Async](observation: Observation)(using Scheduler): F[Action] =
    actor.askF[F, StepDone](ref => Step(observation, ref)).map(_.action)

  override lazy val toString: String = s"Planner(path = ${actor.path})"
