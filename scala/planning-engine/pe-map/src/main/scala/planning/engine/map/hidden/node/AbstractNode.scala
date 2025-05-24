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
import planning.engine.common.values.text.Name
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.map.hidden.state.node.HiddenNodeState
import planning.engine.common.properties.*
import neotypes.query.QueryArg.Param
import planning.engine.map.hidden.state.edge.EdgeState

class AbstractNode[F[_]: MonadThrow](
    val id: HnId,
    val name: Option[Name],
    val parents: List[HiddenNode[F]],
    val children: List[EdgeState[F]],
    val nextHnIndex: HnIndex
) extends HiddenNode[F]:

//  private[map] override def init[R](block: => F[R]): F[(HiddenNode[F], R)] = block.map(res => (this, res))
//
//  private[map] override def remove[R](block: => F[R]): F[R] = block
//
//  private[map] override def toProperties: F[Map[String, Param]] = paramsOf(
//    PROP_NAME.HN_ID -> id.toDbParam,
//    PROP_NAME.NAME -> name.map(_.toDbParam),
//    PROP_NAME.NEXT_HN_INEX -> nodeState.get.map(_.nextHnIndex.toDbParam)
//  )
//
//  override def equals(that: Any): Boolean = that match
//    case obj: AbstractNode[?] => this.id == obj.id && this.name == obj.name
//    case _                    => false

  override def toString: String = s"AbstractHiddenNode(id=$id, name=$name)"

object AbstractNode:
  final case class New(name: Option[Name])
  
//  private[map] def apply[F[_]: Concurrent](
//      id: HnId,
//      name: Option[Name],
//      initState: HiddenNodeState[F]
//  ): F[AbstractNode[F]] =
//    for
//      state <- AtomicCell[F].of[HiddenNodeState[F]](initState)
//      node = new AbstractNode[F](id, name, state)
//    yield node
//
//  private[map] def apply[F[_]: Concurrent](id: HnId, name: Option[Name]): F[AbstractNode[F]] =
//    apply[F](id, name, HiddenNodeState.init[F])
//
//  private[map] def fromProperties[F[_]: Concurrent](properties: Map[String, Value]): F[AbstractNode[F]] =
//    for
//      id <- properties.getValue[F, Long](PROP_NAME.HN_ID).map(HnId.apply)
//      name <- properties.getOptional[F, String](PROP_NAME.NAME).map(_.map(Name.apply))
//      state <- HiddenNodeState.fromProperties(properties)
//      concreteNode <- AbstractNode(id, name, state)
//    yield concreteNode
