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
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.text.Description
import planning.engine.planner.mpi.actors.node.NodeActor
import planning.engine.planner.mpi.actors.node.NodeActor.{AbsDef, ConDef}
import planning.engine.common.errors.*

sealed trait NodeData:
  def name: Option[HnName]
  def description: Option[Description]
  def tp: NodeType

  private[mpi] def toDefinition[B[_]: MonadThrow](rawId: Long, actors: StaticActors): B[NodeActor.Def]

final case class ConData(
    name: Option[HnName],
    description: Option[Description],
    ioName: IoName,
    valueIndex: IoIndex,
) extends NodeData:
  val tp: NodeType = NodeType.Concrete
  override lazy val toString: String = s"[${name.repr}, ${ioName.value}]"

  private[mpi] def toDefinition[B[_]: MonadThrow](rawId: Long, actors: StaticActors): B[NodeActor.Def] =
    ConDef(MnId.Con(rawId), this, actors).asInstanceOf[NodeActor.Def].pure

final case class AbsData(
    name: Option[HnName],
    description: Option[Description],
) extends NodeData:
  val tp: NodeType = NodeType.Abstract
  override lazy val toString: String = s"(${name.repr})"

  private[mpi] def toDefinition[B[_]: MonadThrow](rawId: Long, actors: StaticActors): B[NodeActor.Def] =
    AbsDef(MnId.Abs(rawId), this, actors).asInstanceOf[NodeActor.Def].pure

object NodeData:
  final case class Kit(nodes: List[NodeData]):
    def toDefinitions[B[_]: MonadThrow](initRawId: Long, actors: StaticActors): B[List[NodeActor.Def]] =
      for
        _ <- nodes.assertDistinct("Node data must be distinct")
        refs <- nodes.zipWithIndex.traverse((node, i) => node.toDefinition(initRawId + i, actors))
      yield refs

  def apply(nodes: NodeData*): NodeData.Kit = NodeData.Kit(nodes.toList)
