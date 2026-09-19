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
import cats.syntax.ext.*
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.ApiBase
import planning.engine.planner.mpi.actors.visualizer.Visualizer
import planning.engine.planner.mpi.actors.visualizer.data.Message

private[visualizer] final case class ApiImpl(actor: Actor.Ref) extends ApiBase[Actor.Msg] with Visualizer:
  import Message.*

  override def nodesAdded[F[_]: MonadThrow](ids: Map[MnId, Option[HnName]]): F[Unit] = ifNonEmpty((), ids):
    actor.tellF(ShowNodesAdded(ids))

  override def edgesAdded[F[_]: MonadThrow](keys: Set[MeKey]): F[Unit] = ifNonEmpty((), keys):
    actor.tellF(ShowEdgesAdded(keys))

  override lazy val toString: String = s"Visualizer(path = ${actor.path})"
