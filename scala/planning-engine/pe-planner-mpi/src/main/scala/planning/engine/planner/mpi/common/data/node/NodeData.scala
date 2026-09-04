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

import planning.engine.common.values.io.{IoIndex, IoName, IoValue}
import planning.engine.common.values.node.HnName
import planning.engine.common.values.text.Description

sealed trait NodeData:
  def name: Option[HnName]
  def description: Option[Description]
  def nodeType: NodeType

object NodeData:
  final case class Con(
      name: Option[HnName],
      description: Option[Description],
      ioName: IoName,
      valueIndex: IoIndex,
  ) extends NodeData:
    val nodeType: NodeType = NodeType.Concrete
    val ioValue: IoValue = IoValue(ioName, valueIndex)
    override lazy val toString: String = s"[${name.repr}, ${ioName.value}]"

  final case class Abs(
      name: Option[HnName],
      description: Option[Description],
  ) extends NodeData:
    val nodeType: NodeType = NodeType.Abstract
    override lazy val toString: String = s"(${name.repr})"

  def apply(ioValue: Option[IoValue]): NodeData = ioValue match
    case Some(io) => Con(None, None, io.name, io.index)
    case None     => Abs(None, None)
