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
import cats.syntax.all.*
import neotypes.model.types.{Node, Value}
import neotypes.query.QueryArg.Param
import planning.engine.map.io.node.IoNode
import planning.engine.common.values.text.Name
import planning.engine.common.values.node.{HnId, IoIndex}
import planning.engine.common.errors.assertionError
import planning.engine.common.values.db.Neo4j.{HN_LABEL, CONCRETE_LABEL}
import planning.engine.common.properties.*

final case class ConcreteNode[F[_]: MonadThrow](
    id: HnId,
    name: Option[Name],
    ioNode: IoNode[F],
    valueIndex: IoIndex
) extends HiddenNode[F]:

  override def toProperties: F[Map[String, Param]] = paramsOf(
    PROP.HN_ID -> id.toDbParam,
    PROP.NAME -> name.map(_.toDbParam),
    PROP.IO_INDEX -> valueIndex.toDbParam
  )

  override def equals(that: Any): Boolean = that match
    case obj: ConcreteNode[?] =>
      this.id == obj.id && this.name == obj.name && this.ioNode == obj.ioNode && this.valueIndex == obj.valueIndex
    case _ => false

  override def toString: String = s"ConcreteHiddenNode(id=$id, name=$name, valueIndex=$valueIndex, ioNode=$ioNode)"

object ConcreteNode:
  final case class New(name: Option[Name], ioNodeName: Name, valueIndex: IoIndex)

  def fromNode[F[_]: MonadThrow](node: Node, ioNode: IoNode[F]): F[ConcreteNode[F]] = node match
    case n if n.is(HN_LABEL) && n.is(CONCRETE_LABEL) =>
      for
        id <- n.getValue[F, Long](PROP.HN_ID).map(HnId.apply)
        name <- n.getOptional[F, String](PROP.NAME).map(_.map(Name.apply))
        valueIndex <- n.getValue[F, Long](PROP.IO_INDEX).map(IoIndex.apply)
        concreteNode <- ConcreteNode(id, name, ioNode, valueIndex).pure
      yield concreteNode
    case _ => s"Node is not a hidden concrete node: $node".assertionError
