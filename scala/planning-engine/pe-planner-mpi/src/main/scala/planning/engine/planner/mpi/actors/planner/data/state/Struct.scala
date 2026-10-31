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
import planning.engine.common.errors.*
import planning.engine.common.values.io.IoValue
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.repr.Representable

private[planner] final case class Struct(
    // Concrete nodes known to represent each value (HnIndex) of each input variable.
    inputNodes: Map[IoValue, Node.Con],

    // Concrete node used for each output variable, along with its IoIndex.
    outputNodes: Map[MnId.Con, Node.Con],
) extends Representable:

  def withNewInNodes(nodes: Set[Node.Con]): Struct =
    copy(inputNodes = nodes.foldLeft(inputNodes)((acc, node) => acc.updated(node.data.ioValue, node)))

  def withNewOutNodes(nodes: Set[Node.Con]): Struct =
    copy(outputNodes = nodes.foldLeft(outputNodes)((acc, node) => acc.updated(node.mnId, node)))

  def findInNodes[F[_]: MT](values: Set[IoValue]): F[Set[Node.Con]] =
    for
      _ <- inputNodes.keySet.assertContainsAllOf(values, "Struct.findInNodes: missing input nodes")
      nodes = inputNodes.view.filterKeys(values.contains).values.toSet
    yield nodes

private[planner] object Struct:
  val init: Struct = Struct(Map.empty, Map.empty)
