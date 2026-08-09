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
import planning.engine.planner.mpi.actors.WrapperBase
import planning.engine.planner.mpi.actors.manager.data.Definition
import planning.engine.planner.mpi.actors.manager.logic.Actor
import planning.engine.planner.mpi.actors.visualizer.VisualizerActor
import planning.engine.planner.mpi.common.data.node.NodeData

trait ManagerLike[F[_]]:

  // Add nodes command. Create new MnId and add node to the graph for each node in the kit. Reply with NodeData.
  def addNode(data: NodeData.Kit): F[Map[MnId, Option[HnName]]]

  // Upsert nodes by name command:
  // - If the node with given name already exists, returns its MnId.
  // - If node does not exist, creates and add new node.
  // Will fail if:
  // - In map found duplicate names.
  // - NodeData.Kit contains NodeData with undefined name field.
  // - Node type of found node does not match the type given in NodeData.
  def upsertNodesByName(data: NodeData.Kit): F[Map[MnId, Option[HnName]]]

  // Upsert edges command:
  // - If the edge exists, join indexies to already existing (only `AddEdgeSrc` used).
  // - If edge does not exist, creates it by adding edge source along with data to source node,
  //   and edge target to target node (`AddEdgeSrc` and `AddEdgeTrg` used).
  // Will fail if:
  // - In indexies map found duplicate SampleId (SampleId is unique per edge).
  // (!) Note: This command not have rollback mechanism, so in case of failure, some edges may be added, some not.
  def upsertEdges(data: NodeData.Kit): F[Set[MeKey]]

private[manager] class Manager[F[_]: Sync](actorRef: Actor.Ref) extends ManagerLike[F] with WrapperBase[F, Actor.Msg]:
  def addNode(data: NodeData.Kit): F[Map[MnId, Option[HnName]]] = ???

  def upsertNodesByName(data: NodeData.Kit): F[Map[MnId, Option[HnName]]] = ???

  def upsertEdges(data: NodeData.Kit): F[Set[MeKey]] = ???

object Manager:

  // TODO To rewrite, this method should not allow Actor.Ref leakage.
  def spawn[F[_]: Sync](
      visualizer: VisualizerActor.Ref,
      make: (Behavior[Actor.Msg], String) => Actor.Ref,
  ): ManagerLike[F] = ???

  // Actor.spawn(Definition(visualizer), make)
