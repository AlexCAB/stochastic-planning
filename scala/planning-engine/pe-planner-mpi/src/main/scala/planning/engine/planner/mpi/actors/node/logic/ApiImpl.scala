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

import planning.engine.planner.mpi.actors.ApiBase
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.common.data.edge.{EdgeData, MeRef}

private[node] class ApiImpl(actorRef: Actor.Ref) extends Node with ApiBase[Actor.Msg]:
  val name: String = actorRef.path.name

  def addEdgeSrc[F[_]](ref: MeRef, data: EdgeData): F[Unit] = ???

  def addEdgeTrg[F[_]](ref: MeRef): F[Unit] = ???
