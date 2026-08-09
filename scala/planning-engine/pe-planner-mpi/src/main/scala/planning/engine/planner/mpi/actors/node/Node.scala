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

package planning.engine.planner.mpi.actors.node

import cats.effect.Sync
import org.apache.pekko.actor.typed.Behavior
import planning.engine.planner.mpi.actors.node.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.common.data.edge.{EdgeData, MeRef}

trait Node:

  // Identifying name of this node, for logging purposes.
  def name: String

  // Add source end of an edge to this node.
  def addEdgeSrc[F[_]](ref: MeRef, data: EdgeData): F[Unit]

  // Add target end of an edge to this node.
  def addEdgeTrg[F[_]](ref: MeRef): F[Unit]

object Node:
  type Msg = Actor.Msg

  def wrap(ref: Actor.Ref): Node = new ApiImpl(ref)

  // TODO To rewrite, this method should not allow Actor.Ref leakage.
  def spawn[F[_]: Sync](
      definitions: List[Actor.Def],
      make: (Behavior[Msg], String) => Actor.Ref,
  ): List[Node] = Actor.spawn(definitions, make).map((ref, _) => wrap(ref)).toList
