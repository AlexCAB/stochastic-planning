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
    def withNewNodes[F[_]: MonadThrow](newNodes: Map[NodeActor.Ref, NodeActor.Def]): F[State] =
      for
          _ <- nodeRefs.assertContainsNoneOf(newNodes.keySet, "Node IDs already exist in the current state.")
      yield this.copy(
        nodeRefs = nodeRefs ++ newNodes.map((r, d) => d.id -> r),
        nextId = nextId + newNodes.size,
        nodeNames = nodeNames ++ newNodes
          .map((_, d) => (d.id, d.data.name))
          .collect { case (id, Some(name)) => name -> (nodeNames.getOrElse(name, Set.empty) + id) },
      )

    def findByName[F[_]: MonadThrow](names: Set[HnName]): F[Map[MnId, HnName]] =
      for
        found <- nodeNames.filter((name, ids) => names.contains(name)).pure
        _ <- found.toList.traverse((n, ids) => ids.assertOneElement(s"Expected exactly one node ID for name '$n'"))
        _ <- found.flatMap((_, ids) => ids.toList).assertDistinct("Found duplicate node IDs for names")
      yield found.map((n, ids) => ids.head -> n)

  private[manager] object State:
    val init: State = State(Map.empty, Map.empty, 1L)
