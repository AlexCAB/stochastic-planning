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
| created: 09-Aug-26 |||||||||||*/



package planning.engine.planner.mpi.actors.manager.logic

import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.ApiBase
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.common.repr.Representable

private[manager] class ApiImpl(ref: Actor.Ref) extends Manager with ApiBase[Actor.Msg]:
  def addNode[F[_]](data: NodeData.Kit): F[Map[MnId, Option[HnName]]] = ???

  def upsertNodesByName[F[_]](data: NodeData.Kit): F[Map[MnId, Option[HnName]]] = ???

  def upsertEdges[F[_]](data: NodeData.Kit): F[Set[MeKey]] = ???

  def reportError[F[_]](source: Node, msg: Option[Representable], err: Throwable): F[Unit] = ???