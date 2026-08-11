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
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.common.repr.Representable

private[manager] final case class State(
    // List of all node in map network
    nodeRefMap: Map[MnId, Node],
    nodeNameMap: Map[HnName, Set[MnId]],

    // Next ID to assign to a new node (incremented for each new node)
    nextId: Long,
) extends Representable:

  def withNewNodes[F[_]: MonadThrow](
      dataKit: NodeData.Kit,
      spawn: (Long, NodeData) => F[Node],
  ): F[(List[Node], State)] =
    def extractNames(nodes: List[Node]): Map[HnName, Set[MnId]] = nodes
      .collect { case n if n.name.isDefined => n.name.get -> n.mnId }
      .groupBy(_._1).map((name, ids) => name -> (ids.map(_._2).toSet ++ nodeNameMap.getOrElse(name, Set.empty)))

    def updateState(nodes: List[Node]): State = this.copy(
      nodeRefMap = nodeRefMap ++ nodes.map(n => n.mnId -> n),
      nodeNameMap = nodeNameMap ++ extractNames(nodes),
      nextId = nextId + nodes.size,
    )

    for
      nodes <- dataKit.nodes.zipWithIndex.traverse((nd, i) => spawn(nextId + i, nd))
      mnIds = nodes.map(_.mnId)
      _ <- mnIds.assertDistinct("Duplicate node IDs in new nodes")
      _ <- nodeRefMap.keySet.assertContainsNoneOf(mnIds, "Node IDs already exist in the current state")
    yield (nodes, updateState(nodes))

  def findByName[F[_]: MonadThrow](names: Set[HnName]): F[Map[MnId, HnName]] =
    for
      found <- nodeNameMap.filter((name, ids) => names.contains(name)).pure
      _ <- found.toList.traverse((n, ids) => ids.assertOneElement(s"Expected exactly one node ID for name '$n'"))
      _ <- found.flatMap((_, ids) => ids.toList).assertDistinct("Found duplicate node IDs for names")
    yield found.map((n, ids) => ids.head -> n)

  def getRef[F[_]: MonadThrow](mnId: MnId): F[Node] = nodeRefMap.get(mnId) match
    case Some(ref) => ref.pure
    case None      => s"Node ID $mnId not found in state".assertionError

private[manager] object State:
  val init: State = State(Map.empty, Map.empty, 1L)
