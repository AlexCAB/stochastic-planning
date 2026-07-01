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

package planning.engine.planner.mpi.actors.manager

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.actors.node.NodeActor
import planning.engine.common.errors.*

private[manager] trait States:
  private[manager] final case class State(
      // List of all node in map network
      nodeRefs: Map[MnId, NodeActor.Ref],
      nodeNames: Map[HnName, Set[MnId]],

      // Next ID to assign to a new node (incremented for each new node)
      nextId: Long,
  ):
    private def extractNames(newNodes: Map[NodeActor.Ref, NodeActor.Def]): Map[HnName, Set[MnId]] = newNodes
      .values.collect { case d if d.data.name.isDefined => d.data.name.get -> d.id }
      .groupBy(_._1).map((name, ids) => name -> (ids.map(_._2).toSet ++ nodeNames.getOrElse(name, Set.empty)))

    def withNewNodes[F[_]: MonadThrow](newNodes: Map[NodeActor.Ref, NodeActor.Def]): F[State] =
      for
        _ <- nodeRefs.values.assertContainsNoneOf(newNodes.keySet, "Node IDs already exist in the current state")
        _ <- newNodes.values.map(_.id).assertDistinct("Duplicate node IDs in new nodes")
      yield this.copy(
        nodeRefs = nodeRefs ++ newNodes.map((r, d) => d.id -> r),
        nextId = nextId + newNodes.size,
        nodeNames = nodeNames ++ extractNames(newNodes),
      )

    def findByName[F[_]: MonadThrow](names: Set[HnName]): F[Map[MnId, HnName]] =
      for
        found <- nodeNames.filter((name, ids) => names.contains(name)).pure
        _ <- found.toList.traverse((n, ids) => ids.assertOneElement(s"Expected exactly one node ID for name '$n'"))
        _ <- found.flatMap((_, ids) => ids.toList).assertDistinct("Found duplicate node IDs for names")
      yield found.map((n, ids) => ids.head -> n)

  private[manager] object State:
    val init: State = State(Map.empty, Map.empty, 1L)
