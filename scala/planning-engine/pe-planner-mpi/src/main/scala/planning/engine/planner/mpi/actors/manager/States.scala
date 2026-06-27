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
import org.apache.pekko.actor.typed.ActorRef
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.node.NodeActor
import planning.engine.common.errors.*

private[manager] trait States:
  private[manager] final case class State(
      // List of all node in map network
      nodes: Map[MnId, ActorRef[NodeActor.Message]],

      // Next ID to assign to a new node (incremented for each new node)
      nextId: Long,
  ):
    def withNewNodes[F[_]: MonadThrow](newNodes: Map[MnId, ActorRef[NodeActor.Message]]): F[State] =
      for
          _ <- nodes.assertContainsNoneOf(newNodes.keySet, "Node IDs already exist in the current state.")
      yield this.copy(nodes = nodes ++ newNodes, nextId = nextId + newNodes.size)

  private[manager] object State:
    val init: State = State(Map.empty, 1L)
