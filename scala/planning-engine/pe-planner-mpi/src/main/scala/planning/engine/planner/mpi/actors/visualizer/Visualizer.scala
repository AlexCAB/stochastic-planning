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

package planning.engine.planner.mpi.actors.visualizer

import cats.effect.Sync
import org.apache.pekko.actor.typed.Behavior
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.visualizer.logic.{Actor, ApiImpl}

trait Visualizer:

  // Save nodes added to the map network for visualization.
  def nodesAdded[F[_]](ids: Map[MnId, Option[HnName]]): F[Unit]

  // Save edges added to the map network for visualization.
  def edgesAdded[F[_]](keys: Set[MeKey]): F[Unit]

object Visualizer:
  def wrap(ref: Actor.Ref): Visualizer = new ApiImpl(ref)

  // TODO To rewrite, this method should not allow Actor.Ref leakage.
  def spawn[F[_]: Sync](make: (Behavior[Actor.Msg], String) => Actor.Ref): Visualizer = ???

  // Actor.spawn(Definition(), make)
