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

import cats.MonadThrow
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.ApiBase
import planning.engine.planner.mpi.actors.visualizer.data.Message
import planning.engine.planner.mpi.actors.visualizer.Visualizer

private[visualizer] final case class ApiImpl(actor: Actor.Ref) extends Visualizer with ApiBase[Actor.Msg]:
  import Message.*

  def nodesAdded[F[_]: MonadThrow](ids: Map[MnId, Option[HnName]]): F[Unit] = actor.tellF(ShowNodesAdded(ids))

  def edgesAdded[F[_]: MonadThrow](keys: Set[MeKey]): F[Unit] = actor.tellF(ShowEdgesAdded(keys))
