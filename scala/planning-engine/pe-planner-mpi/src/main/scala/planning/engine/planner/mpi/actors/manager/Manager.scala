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

package planning.engine.planner.mpi.actors.manager

import cats.MonadThrow
import cats.effect.Async
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.manager.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.manager.data.Definition
import planning.engine.planner.mpi.actors.visualizer.Visualizer
import planning.engine.planner.mpi.common.data.edge.EdgeData
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.common.repr.Representable

trait Manager:

  // Add nodes command. Create new MnId and add node to the graph for each node in the kit. Reply with NodeData.
  def addNodes[F[_]: Async](dataKit: NodeData.Kit)(using ActorSystem[?]): F[Map[MnId, Option[HnName]]]

  // Upsert nodes by name command:
  // - If the node with given name already exists, returns its MnId.
  // - If node does not exist, creates and add new node.
  // Will fail if:
  // - In map found duplicate names.
  // - NodeData.Kit contains NodeData with undefined name field.
  // - Node type of found node does not match the type given in NodeData.
  def upsertNodesByName[F[_]: Async](dataKit: NodeData.Kit)(using ActorSystem[?]): F[Map[MnId, Option[HnName]]]

  // Upsert edges command:
  // - If the edge exists, join indexies to already existing (only `AddEdgeSrc` used).
  // - If edge does not exist, creates it by adding edge source along with data to source node,
  //   and edge target to target node (`AddEdgeSrc` and `AddEdgeTrg` used).
  // Will fail if:
  // - In indexies map found duplicate SampleId (SampleId is unique per edge).
  // (!) Note: This command not have rollback mechanism, so in case of failure, some edges may be added, some not.
  def upsertEdges[F[_]: Async](dataKit: EdgeData.Kit)(using ActorSystem[?]): F[Set[MeKey]]

  // Report an error that occurred in a NodeActor.
  def reportError[F[_]: MonadThrow](source: Node, msg: Option[Representable], err: Throwable): F[Unit]

object Manager:
  type Msg = Actor.Msg

  def spawn[F[_]: MonadThrow](visualizer: Visualizer, make: (Behavior[Msg], String) => ActorRef[Msg]): F[Manager] =
    MonadThrow[F].catchNonFatal(ApiImpl(Actor.spawn(Definition(visualizer), make)))
