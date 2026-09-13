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
| created: 31.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.planner.data

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.model.repr.Representable

private[planner] final case class State(
    // Concrete nodes known to represent each value (HnIndex) of each input variable.
    inputNodes: State.InNodes,

    // Concrete node used for each output variable, along with its IoIndex.
    outputNodes: State.OutNodes,
) extends Representable:
  import State.*

  def withNewInNodes[F[_]: MonadThrow](nodes: Set[Node.Con]): F[State] =
    def addNode(acc: InNodes, node: Node.Con): InNodes =
      val index = node.ioValue.index
      val name = node.ioValue.name

      acc.get(name) match
        case Some(atIndex) => atIndex.get(index) match
            case Some(nodesAtIndex) => acc.updated(name, atIndex.updated(index, nodesAtIndex + node))
            case None               => acc.updated(name, atIndex.updated(index, Set(node)))
        case None => acc.updated(name, Map(index -> Set(node)))

    copy(inputNodes = nodes.foldLeft(inputNodes)(addNode)).pure

  def withNewOutNodes[F[_]: MonadThrow](nodes: Set[Node.Con]): F[State] =
    def addNode(acc: OutNodes, node: Node.Con): OutNodes = acc.updated(node.mnId, node)

    copy(outputNodes = nodes.foldLeft(outputNodes)(addNode)).pure

private[planner] object State:
  type InNodes = Map[IoName, Map[IoIndex, Set[Node.Con]]]
  type OutNodes = Map[MnId.Con, Node.Con]

  val init: State = State(Map.empty, Map.empty)
