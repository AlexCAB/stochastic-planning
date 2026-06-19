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

package planning.engine.planner.mpi.data.node

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.values.io.IoIndex
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.text.Description
import planning.engine.map.io.node.IoNode
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.actors.node.Node.{AbsDef, ConDef}

sealed trait NodeData:
  def name: Option[HnName]
  def description: Option[Description]
  def tp: NodeType

  private[mpi] def toDefinition[B[_]: MonadThrow](rawId: Long, actors: StaticActors): B[Node.Def]

final case class ConData[F[_]: MonadThrow](
    name: Option[HnName],
    description: Option[Description],
    ioNode: IoNode[F],
    valueIndex: IoIndex,
) extends NodeData:
  val tp: NodeType = NodeType.Concrete
  override lazy val toString: String = s"[${name.repr}, ${ioNode.name.value}]"

  private[mpi] def toDefinition[B[_]: MonadThrow](rawId: Long, actors: StaticActors): B[Node.Def] =
    ConDef(MnId.Con(rawId), this, actors).asInstanceOf[Node.Def].pure

final case class AbsData(
    name: Option[HnName],
    description: Option[Description],
) extends NodeData:
  val tp: NodeType = NodeType.Abstract
  override lazy val toString: String = s"(${name.repr})"

  private[mpi] def toDefinition[B[_]: MonadThrow](rawId: Long, actors: StaticActors): B[Node.Def] =
    AbsDef(MnId.Abs(rawId), this, actors).asInstanceOf[Node.Def].pure
