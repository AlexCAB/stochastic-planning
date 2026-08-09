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

import cats.effect.Sync
import org.apache.pekko.actor.typed.Behavior
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}

import planning.engine.planner.mpi.actors.manager.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.visualizer.Visualizer
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.common.repr.Representable

trait Manager:

  // Add nodes command. Create new MnId and add node to the graph for each node in the kit. Reply with NodeData.
  def addNode[F[_]](data: NodeData.Kit): F[Map[MnId, Option[HnName]]]

  // Upsert nodes by name command:
  // - If the node with given name already exists, returns its MnId.
  // - If node does not exist, creates and add new node.
  // Will fail if:
  // - In map found duplicate names.
  // - NodeData.Kit contains NodeData with undefined name field.
  // - Node type of found node does not match the type given in NodeData.
  def upsertNodesByName[F[_]](data: NodeData.Kit): F[Map[MnId, Option[HnName]]]

  // Upsert edges command:
  // - If the edge exists, join indexies to already existing (only `AddEdgeSrc` used).
  // - If edge does not exist, creates it by adding edge source along with data to source node,
  //   and edge target to target node (`AddEdgeSrc` and `AddEdgeTrg` used).
  // Will fail if:
  // - In indexies map found duplicate SampleId (SampleId is unique per edge).
  // (!) Note: This command not have rollback mechanism, so in case of failure, some edges may be added, some not.
  def upsertEdges[F[_]](data: NodeData.Kit): F[Set[MeKey]]

  // Report an error that occurred in a NodeActor.
  def reportError[F[_]](source: Node, msg: Option[Representable], err: Throwable): F[Unit]

object Manager:
  def wrap(ref: Actor.Ref): Manager = new ApiImpl(ref)

  // TODO To rewrite, this method should not allow Actor.Ref leakage.
  def spawn[F[_]: Sync](
      visualizer: Visualizer,
      make: (Behavior[Actor.Msg], String) => Actor.Ref,
  ): Manager = ???

  // Actor.spawn(Definition(visualizer), make)
