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



package planning.engine.planner.mpi.actors.node.logic

import cats.MonadThrow
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.ApiBase
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.node.data.Message
import planning.engine.planner.mpi.common.data.edge.{EdgeData, MeRef}

private[node] final case class ApiImpl(mnId: MnId, name: Option[HnName], actor: Actor.Ref) 
  extends Node with ApiBase[Actor.Msg]:
  import Message.*

  def addEdgeSrc[F[_]: MonadThrow](ref: MeRef, data: EdgeData): F[Unit] = actor.tellF(AddEdgeSrc(ref, data))

  def addEdgeTrg[F[_]: MonadThrow](ref: MeRef): F[Unit] = actor.tellF(AddEdgeTrg(ref))
