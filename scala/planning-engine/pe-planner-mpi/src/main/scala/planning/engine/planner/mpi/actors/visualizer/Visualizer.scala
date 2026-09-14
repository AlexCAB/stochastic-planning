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

package planning.engine.planner.mpi.actors.visualizer

import cats.MonadThrow
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.Visualization
import planning.engine.planner.mpi.actors.visualizer.data.Definition
import planning.engine.planner.mpi.actors.visualizer.logic.{Actor, ApiImpl}

private[mpi] trait Visualizer:

  // Save nodes added to the map network for visualization.
  def nodesAdded[F[_]: MonadThrow](ids: Map[MnId, Option[HnName]]): F[Unit]

  // Save edges added to the map network for visualization.
  def edgesAdded[F[_]: MonadThrow](keys: Set[MeKey]): F[Unit]

private[mpi] object Visualizer:
  type Msg = Actor.Msg

  def spawn[F[_]: MonadThrow](viz: Visualization, make: (Behavior[Msg], String) => ActorRef[Msg]): F[Visualizer] =
    MonadThrow[F].catchNonFatal(ApiImpl(Actor.spawn(Definition(viz), make)))
