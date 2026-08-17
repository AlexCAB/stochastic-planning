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

package planning.engine.planner.mpi.actors.manager

import cats.MonadThrow
import cats.effect.Async
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.Name
import planning.engine.planner.mpi.actors.manager.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.common.data.samples.Sample
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
  // - SampleId is not found (i.e. sample not exist in map).
  // (!) Note: This command not have rollback mechanism, so in case of failure, some edges may be added, some not.
  def upsertEdges[F[_]: Async](dataKit: EdgeData.Kit)(using ActorSystem[?]): F[Set[MeKey]]

  // Add manually defined samples command:
  // - Lookup nodes by name, create new if not found (same as `upsertNodesByName`).
  // - For each node in sample create set of next HnIndex.
  // - Create new SampleId for each sample.
  // - Add update map edges for each sample (same as `upsertEdges`).
  // - Store full sample data in the manager state. And Values in each node state (for speedup probs calculation).
  // - Notify all nodes with new total number of samples (for probs calculation).
  // - Notify visualizer with new map structure (for visualization).
  def addManSamples[F[_]: Async](samples: Set[Sample.Man])(using ActorSystem[?]): F[Map[SampleId, Name]]

  // Report an error that occurred in a NodeActor.
  // In simple implementation it will terminate the manager actor and all its children nodes actors,
  // but in future it may be extended to support more complex error handling.
  def reportError[F[_]: MonadThrow](source: Node, msg: Option[Representable], err: Throwable): F[Unit]

object Manager:
  type Msg = Actor.Msg

  def spawn[F[_]: MonadThrow](visualizer: Visualizer, make: (Behavior[Msg], String) => ActorRef[Msg]): F[Manager] =
    MonadThrow[F].catchNonFatal(ApiImpl(Actor.spawn(Definition(visualizer), make)))
