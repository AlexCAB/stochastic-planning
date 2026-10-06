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
import cats.syntax.ext.*
import org.apache.pekko.actor.typed.scaladsl.ActorContext
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.Visualization
import planning.engine.planner.mpi.actors.visualizer.data.Definition
import planning.engine.planner.mpi.actors.visualizer.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.model.data.map.Metadata
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.planner.mpi.model.io.IoVars

private[mpi] trait Visualizer:

  // Save nodes added to the map network for visualization.
  def nodesAdded[F[_]: MT](ids: Map[MnId, NodeData]): F[Unit]

  // Save edges added to the map network for visualization.
  def edgesAdded[F[_]: MT](keys: Set[MeKey]): F[Unit]

private[mpi] object Visualizer:
  type Msg = Actor.Msg

  def spawn[F[_]: MT](mt: Metadata, vars: IoVars, viz: Visualization, ctx: ActorContext[?]): F[Visualizer] =
    MonadThrow[F].catchNonFatal(ApiImpl(Actor.spawn(Definition(mt, vars, viz), (b, n) => ctx.spawn(b, n))))
