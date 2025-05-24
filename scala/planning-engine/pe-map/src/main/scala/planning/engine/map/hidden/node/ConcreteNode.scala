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
| created: 2025-04-06 |||||||||||*/

package planning.engine.map.hidden.node

import cats.MonadThrow
import cats.effect.kernel.Concurrent
import cats.effect.std.AtomicCell
import cats.syntax.all.*
import neotypes.model.types.Value
import neotypes.query.QueryArg.Param
import planning.engine.map.hidden.state.node.HiddenNodeState
import planning.engine.map.io.node.IoNode
import planning.engine.common.properties.*
import planning.engine.common.values.text.Name
import planning.engine.common.values.node.{HnId, HnIndex, IoIndex}
import planning.engine.map.hidden.state.edge.EdgeState

class ConcreteNode[F[_]: MonadThrow](
    val id: HnId,
    val name: Option[Name],
    val parents: List[HiddenNode[F]],
    val children: List[EdgeState[F]],
    val nextHnIndex: HnIndex,
    val ioNode: IoNode[F],
    val valueIndex: IoIndex
) extends HiddenNode[F]:

//  private[map] override def init[R](block: => F[R]): F[(HiddenNode[F], R)] =
//    ioNode.addConcreteNode(this, block).map((n, r) => (n.asInstanceOf[HiddenNode[F]], r))
//
//  private[map] override def remove[R](block: => F[R]): F[R] = ioNode.removeConcreteNode(this, block)
//
//  private[map] override def toProperties: F[Map[String, Param]] = paramsOf(
//    PROP_NAME.HN_ID -> id.toDbParam,
//    PROP_NAME.NAME -> name.map(_.toDbParam),
//    PROP_NAME.IO_INDEX -> valueIndex.toDbParam,
//    PROP_NAME.NEXT_HN_INEX -> nodeState.get.map(_.nextHnIndex.toDbParam)
//  )
//
//  override def equals(that: Any): Boolean = that match
//    case obj: ConcreteNode[?] =>
//      this.id == obj.id && this.name == obj.name && this.ioNode == obj.ioNode && this.valueIndex == obj.valueIndex
//    case _ => false

  override def toString: String = s"ConcreteHiddenNode(id=$id, name=$name, valueIndex=$valueIndex, ioNode=$ioNode)"

object ConcreteNode:
  final case class New(name: Option[Name], ioNodeName: Name, valueIndex: IoIndex)

  private[map] def apply[F[_]: Concurrent](
      id: HnId,
      name: Option[Name],
      ioNode: IoNode[F],
      valueIndex: IoIndex
  ): F[ConcreteNode[F]] = new ConcreteNode[F](
    id,
    name,
    parents = List.empty,
    children = List.empty,
    nextHnIndex = HnIndex.init,
    ioNode,
    valueIndex
  ).pure

//
//  private[map] def apply[F[_]: MonadThrow](
//      id: HnId,
//      name: Option[Name],
//      ioNode: IoNode[F],
//      valueIndex: IoIndex
//  ): F[ConcreteNode[F]] = apply(id, name, ioNode, valueIndex, HiddenNodeState.init[F])
//
//  private[map] def fromProperties[F[_]: Concurrent](
//      properties: Map[String, Value],
//      ioNode: IoNode[F]
//  ): F[ConcreteNode[F]] =
//    for
//      id <- properties.getValue[F, Long](PROP_NAME.HN_ID).map(HnId.apply)
//      name <- properties.getOptional[F, String](PROP_NAME.NAME).map(_.map(Name.apply))
//      valueIndex <- properties.getValue[F, Long](PROP_NAME.IO_INDEX).map(IoIndex.apply)
//      state <- HiddenNodeState.fromProperties(properties)
//      concreteNode <- ConcreteNode(id, name, ioNode, valueIndex, state)
//    yield concreteNode
