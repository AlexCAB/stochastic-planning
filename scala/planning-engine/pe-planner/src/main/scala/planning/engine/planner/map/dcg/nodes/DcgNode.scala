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
| created: 2025-12-01 |||||||||||*/

package planning.engine.planner.map.dcg.nodes

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.values.io.{IoIndex, IoName, IoValue}
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.text.Description
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.io.node.IoNode
import planning.engine.planner.map.dcg.repr.DcgNodeRepr

sealed trait DcgNode[F[_]: MonadThrow] extends DcgNodeRepr[F]:
  def id: MnId
  def name: Option[HnName]
  def asConcrete: Option[DcgNode.Concrete[F]]
  def asDcgNode: DcgNode[F] = this

  protected lazy val idRepr: String = s"${id.value}${name.map(n => s", \'${n.value}\'").getOrElse("")}"

object DcgNode:
  final case class Concrete[F[_]: MonadThrow](
      id: MnId.Con,
      name: Option[HnName],
      description: Option[Description],
      ioNode: IoNode[F],
      valueIndex: IoIndex
  ) extends DcgNode[F]:
    lazy val ioValue: IoValue = IoValue(ioNode.name, valueIndex)
    lazy val asConcrete: Option[DcgNode.Concrete[F]] = Some(this)
    override lazy val toString: String = s"[C, $idRepr, ${ioValue.toString}]"

  object Concrete:
    def apply[F[_]: MonadThrow](node: ConcreteNode[F]): F[Concrete[F]] = new Concrete[F](
      id = node.id.asCon,
      name = node.name,
      description = node.description,
      ioNode = node.ioNode,
      valueIndex = node.valueIndex
    ).pure

    def apply[F[_]: MonadThrow](
        hnId: MnId.Con,
        node: ConcreteNode.New,
        getIoNode: IoName => F[IoNode[F]]
    ): F[Concrete[F]] =
      for
          ioNode <- getIoNode(node.ioNodeName)
      yield new Concrete[F](
        id = hnId,
        name = node.name,
        description = node.description,
        ioNode = ioNode,
        valueIndex = node.valueIndex
      )

  final case class Abstract[F[_]: MonadThrow](
      id: MnId.Abs,
      name: Option[HnName],
      description: Option[Description]
  ) extends DcgNode[F]:
    lazy val asConcrete: Option[DcgNode.Concrete[F]] = None
    override lazy val toString: String = s"(A, $idRepr)"

  object Abstract:
    def apply[F[_]: MonadThrow](node: AbstractNode[F]): F[Abstract[F]] = new Abstract[F](
      id = node.id.asAbs,
      name = node.name,
      description = node.description
    ).pure

    def apply[F[_]: MonadThrow](hnId: MnId.Abs, node: AbstractNode.New): F[Abstract[F]] = new Abstract[F](
      id = hnId,
      name = node.name,
      description = node.description
    ).pure
