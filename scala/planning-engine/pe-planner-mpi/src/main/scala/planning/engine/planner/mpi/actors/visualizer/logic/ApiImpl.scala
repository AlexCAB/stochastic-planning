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



package planning.engine.planner.mpi.actors.visualizer.logic

import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.ApiBase
import planning.engine.planner.mpi.actors.visualizer.Visualizer

private[visualizer] class ApiImpl(actorRef: Actor.Ref) extends Visualizer with ApiBase[Actor.Msg]:
  def nodesAdded[F[_]](ids: Map[MnId, Option[HnName]]): F[Unit] = ???

  def edgesAdded[F[_]](keys: Set[MeKey]): F[Unit] = ???
