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
| created: 09.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.visualizer.logic

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.ApiBase
import planning.engine.planner.mpi.actors.visualizer.Visualizer
import planning.engine.planner.mpi.actors.visualizer.data.Message

private[visualizer] final case class ApiImpl(actor: Actor.Ref) extends Visualizer with ApiBase[Actor.Msg]:
  import Message.*

  override def nodesAdded[F[_]: MonadThrow](ids: Map[MnId, Option[HnName]]): F[Unit] =
    if ids.nonEmpty then actor.tellF(ShowNodesAdded(ids)) else ().pure

  override def edgesAdded[F[_]: MonadThrow](keys: Set[MeKey]): F[Unit] =
    if keys.nonEmpty then actor.tellF(ShowEdgesAdded(keys)) else ().pure

  override lazy val toString: String = s"Visualizer(path = ${actor.path})"
