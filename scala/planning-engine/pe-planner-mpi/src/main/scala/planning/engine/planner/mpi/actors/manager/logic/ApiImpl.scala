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
| created: 11.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.manager.logic

import cats.MonadThrow
import cats.effect.Async
import cats.syntax.all.*
import org.apache.pekko.actor.typed.ActorSystem
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.ApiBase
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.manager.data.Message
import planning.engine.planner.mpi.common.data.edge.EdgeData
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.common.repr.Representable

private[manager] final case class ApiImpl(actor: Actor.Ref) extends Manager with ApiBase[Actor.Msg]:
  import Message.*

  def addNodes[F[_]: Async](dataKit: NodeData.Kit)(using ActorSystem[?]): F[Map[MnId, Option[HnName]]] =
    actor.askF[F, NodesAdded](ref => AddNodes(dataKit, ref)).map(_.ids)

  def upsertNodesByName[F[_]: Async](dataKit: NodeData.Kit)(using ActorSystem[?]): F[Map[MnId, Option[HnName]]] =
    actor.askF[F, NodesUpserted](ref => UpsertNodesByName(dataKit, ref)).map(_.ids)

  def upsertEdges[F[_]: Async](dataKit: EdgeData.Kit)(using ActorSystem[?]): F[Set[MeKey]] =
    actor.askF[F, EdgesUpserted](ref => UpsertEdges(dataKit, ref)).map(_.keys)

  def reportError[F[_]: MonadThrow](source: Node, msg: Option[Representable], err: Throwable): F[Unit] =
    actor.tellF(NodeActorError(source, msg, err))

  override lazy val toString: String = s"Manager(path = ${actor.path})"
