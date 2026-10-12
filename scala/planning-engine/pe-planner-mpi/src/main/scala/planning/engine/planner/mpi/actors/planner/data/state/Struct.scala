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
| created: 2026-10-13 |||||||||||*/

package planning.engine.planner.mpi.actors.planner.data.state

import cats.syntax.all.*
import cats.syntax.ext.*
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.repr.Representable

private[planner] final case class Struct(
    // Concrete nodes known to represent each value (HnIndex) of each input variable.
    inputNodes: Struct.InNodes,

    // Concrete node used for each output variable, along with its IoIndex.
    outputNodes: Struct.OutNodes,
) extends Representable:
  import Struct.*

  def withNewInNodes[F[_]: MT](nodes: Set[Node.Con]): F[Struct] =
    def addNode(acc: InNodes, node: Node.Con): InNodes =
      val index = node.ioValue.index
      val name = node.ioValue.name

      acc.get(name) match
        case Some(atIndex) => atIndex.get(index) match
            case Some(nodesAtIndex) => acc.updated(name, atIndex.updated(index, nodesAtIndex + node))
            case None               => acc.updated(name, atIndex.updated(index, Set(node)))
        case None => acc.updated(name, Map(index -> Set(node)))

    copy(inputNodes = nodes.foldLeft(inputNodes)(addNode)).pure

  def withNewOutNodes[F[_]: MT](nodes: Set[Node.Con]): F[Struct] =
    def addNode(acc: OutNodes, node: Node.Con): OutNodes = acc.updated(node.mnId, node)

    copy(outputNodes = nodes.foldLeft(outputNodes)(addNode)).pure

private[planner] object Struct:
  type InNodes = Map[IoName, Map[IoIndex, Set[Node.Con]]]
  type OutNodes = Map[MnId.Con, Node.Con]

  val init: Struct = Struct(Map.empty, Map.empty)
