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

import cats.MonadThrow
import cats.syntax.all.*
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.node.data.Definition
import planning.engine.planner.mpi.actors.node.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.actors.visualizer.Visualizer
import planning.engine.planner.mpi.common.data.edge.{EdgeData, MeRef}
import planning.engine.planner.mpi.common.data.node.NodeData

trait Node:
  def mnId: MnId
  def name: Option[HnName]

  // Add source end of an edge to this node.
  def addEdgeSrc[F[_]: MonadThrow](ref: MeRef, data: EdgeData): F[Unit]

  // Add target end of an edge to this node.
  def addEdgeTrg[F[_]: MonadThrow](ref: MeRef): F[Unit]

object Node:
  type Msg = Actor.Msg
  
  def spawn[F[_]: MonadThrow](
     id: MnId,
     data: NodeData,
     manager: Manager, 
     visualizer: Visualizer,
     make: (Behavior[Msg], String) => ActorRef[Msg],
  ): F[Node] =
    for 
      definition <- Definition(id, data, Definition.Actors(manager, visualizer))
    yield ApiImpl(id, data.name, Actor.spawn(definition, make))
