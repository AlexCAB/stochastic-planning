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
| created: 08.06.2026 |||||||||||*/

package planning.engine.planner.mpi.data

import cats.MonadThrow
import planning.engine.common.values.io.IoIndex
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.text.Description
import planning.engine.map.io.node.IoNode

object MapNode:
  enum NodeType:
    case Concrete, Abstract

  sealed trait Data:
    def name: Option[HnName]
    def description: Option[Description]
    def tp: NodeType

    private[mpi] def toDefinition(rawId: Long): Definition

  final case class ConData[F[_]: MonadThrow](
      name: Option[HnName],
      description: Option[Description],
      ioNode: IoNode[F],
      valueIndex: IoIndex
  ) extends Data:
    val tp: NodeType = NodeType.Concrete
    override lazy val toString: String = s"[${name.repr}, ${ioNode.name.value}]"

    private[mpi] def toDefinition(rawId: Long): Definition = ConDef(MnId.Con(rawId), this)

  final case class AbsData(
      name: Option[HnName],
      description: Option[Description]
  ) extends Data:
    val tp: NodeType = NodeType.Abstract
    override lazy val toString: String = s"(${name.repr})"

    private[mpi] def toDefinition(rawId: Long): Definition = AbsDef(MnId.Abs(rawId), this)

  sealed trait Definition:
    def id: MnId
    def data: Data

  final case class ConDef[F[_]: MonadThrow](
      id: MnId.Con,
      data: ConData[F]
  ) extends Definition:
    override lazy val toString: String = s"[${id.reprValue}, ${data.name.repr}]"

  final case class AbsDef(
      id: MnId.Abs,
      data: AbsData
  ) extends Definition:
    override lazy val toString: String = s"(${id.reprValue}, ${data.name.repr})"
