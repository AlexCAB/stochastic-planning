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
| created: 18.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.manager.data

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.errors.*
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.node.Node
//import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.common.data.samples.Sample
import planning.engine.planner.mpi.common.repr.Representable

private[manager] final case class State(
    // Next ID to assign to a new entities (incremented for each new entity)
    // In simple implementation `nextSampleId` and `nextHnIndexies` is in Manager state.
    // But in future `nextSampleId` it can be moved to a separate actor which will handle samples adding.
    nextMnId: Long,
    nextSampleId: Long,

    // List of all node in map network
    nodeRefMap: Map[MnId, Node],

    // Mapping from node names to node IDs. Used for finding nodes by name.
    nodeNameMap: Map[HnName, Set[MnId]],

    // List of all samples in map network.
    // In simple implementation it is in Manager state, but in future there should be some separate storage for it.
    sampleDataMap: Map[SampleId, State.SampleData],
) extends Representable:

//  def withNewNodes[F[_]: MonadThrow](
//      dataKit: NodeData.Kit,
//      spawn: (Long, NodeData) => F[Node],
//  ): F[(List[Node], State)] =
//    def extractNames(nodes: List[Node]): Map[HnName, Set[MnId]] = nodes
//      .collect { case n if n.name.isDefined => n.name.get -> n.mnId }
//      .groupBy(_._1).map((name, ids) => name -> (ids.map(_._2).toSet ++ nodeNameMap.getOrElse(name, Set.empty)))
//
//    def updateState(nodes: List[Node]): State = this.copy(
//      nodeRefMap = nodeRefMap ++ nodes.map(n => n.mnId -> n),
//      nodeNameMap = nodeNameMap ++ extractNames(nodes),
//      nextMnId = nextMnId + nodes.size,
//    )
//
//    for
//      nodes <- dataKit.nodes.zipWithIndex.traverse((nd, i) => spawn(nextMnId + i, nd))
//      mnIds = nodes.map(_.mnId)
//      _ <- mnIds.assertDistinct("Duplicate node IDs in new nodes")
//      _ <- nodeRefMap.keySet.assertContainsNoneOf(mnIds, "Node IDs already exist in the current state")
//    yield (nodes, updateState(nodes))
//
//  def findByName[F[_]: MonadThrow](names: Set[HnName]): F[Map[MnId, HnName]] =
//    for
//      found <- nodeNameMap.filter((name, ids) => names.contains(name)).pure
//      _ <- found.toList.traverse((n, ids) => ids.assertOneElement(s"Expected exactly one node ID for name '$n'"))
//      _ <- found.flatMap((_, ids) => ids.toList).assertDistinct("Found duplicate node IDs for names")
//    yield found.map((n, ids) => ids.head -> n)

  def getRef[F[_]: MonadThrow](mnId: MnId): F[Node] = nodeRefMap.get(mnId) match
    case Some(ref) => ref.pure
    case None      => s"Node ID $mnId not found in state".assertionError

private[manager] object State:
  final case class SampleData(props: Sample.Props, info: Option[Sample.Info])

  val init: State = State(1L, 1L, Map.empty, Map.empty, Map.empty)
