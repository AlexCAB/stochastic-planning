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
| created: 19.06.2026 |||||||||||*/

package planning.engine.planner.mpi.common.data.node

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.HnName
import planning.engine.common.values.text.Description
import planning.engine.common.errors.*

sealed trait NodeData:
  def name: Option[HnName]
  def description: Option[Description]
  def nodeType: NodeType

final case class ConData(
    name: Option[HnName],
    description: Option[Description],
    ioName: IoName,
    valueIndex: IoIndex,
) extends NodeData:
  val nodeType: NodeType = NodeType.Concrete
  override lazy val toString: String = s"[${name.repr}, ${ioName.value}]"

final case class AbsData(
    name: Option[HnName],
    description: Option[Description],
) extends NodeData:
  val nodeType: NodeType = NodeType.Abstract
  override lazy val toString: String = s"(${name.repr})"

object NodeData:
  final case class Kit(
      // List of nodes to be added to the map network
      // Can containe duplicate nodes, for example severall NodeData with empty name and description,
      // which infact mean "just create number of nodes with unique IDs".
      nodes: List[NodeData],
  ):
    def getUniqueNames[F[_]: MonadThrow]: F[Set[HnName]] =
      for
        names <- nodes.flatMap(_.name).pure
        _ <- names.assertDistinct("Node names must be distinct")
      yield names.toSet

    def filterNotByNames(names: Set[HnName]): Kit = Kit(nodes.filterNot(_.name.exists(names.contains)))

  def apply(nodes: NodeData*): NodeData.Kit = NodeData.Kit(nodes.toList)
