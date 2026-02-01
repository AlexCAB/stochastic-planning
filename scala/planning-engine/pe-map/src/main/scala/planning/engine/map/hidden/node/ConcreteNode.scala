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
import planning.engine.common.values.text.Description
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.common.errors.assertionError
import planning.engine.common.values.db.Neo4j.{CONCRETE_LABEL, HN_LABEL}
import planning.engine.common.properties.*
import planning.engine.common.validation.Validation
import planning.engine.common.values.io.{IoIndex, IoName, IoValue}

final case class ConcreteNode[F[_]: MonadThrow](
    id: HnId,
    name: Option[HnName],
    description: Option[Description],
    ioNode: IoNode[F],
    valueIndex: IoIndex
) extends HiddenNode[F]:

  lazy val ioValue: IoValue = IoValue(ioNode.name, valueIndex)

  override lazy val toString: String = s"ConcreteHiddenNode(" +
    s"id = $id, name = $name, description = $description, valueIndex = $valueIndex, ioNode = $ioNode)"

object ConcreteNode:
  final case class New(name: Option[HnName], description: Option[Description], ioNodeName: IoName, valueIndex: IoIndex)
      extends Validation:

    lazy val validationName: String = s"ConcreteNode.New(name=$name, ioNodeName=$ioNodeName, valueIndex=$valueIndex)"

    lazy val validationErrors: List[Throwable] = validations(
      name.forall(_.value.nonEmpty) -> "Name must not be empty if defined",
      ioNodeName.value.nonEmpty -> "IoNode name must not be empty"
    )

    def toProperties[F[_]: MonadThrow](id: HnId, initNextHnIndex: Long): F[Map[String, Param]] = paramsOf(
      PROP.HN_ID -> id.toDbParam,
      PROP.NAME -> name.map(_.toDbParam),
      PROP.DESCRIPTION -> description.map(_.toDbParam),
      PROP.IO_INDEX -> valueIndex.toDbParam,
      PROP.NEXT_HN_INDEX -> initNextHnIndex.toDbParam
    )

  final case class ListNew(list: List[New])

  object ListNew:
    def of(elems: New*): ListNew = ListNew(elems.toList)

  def fromNode[F[_]: MonadThrow](node: Node, ioNode: IoNode[F]): F[ConcreteNode[F]] = node match
    case n if n.is(HN_LABEL) && n.is(CONCRETE_LABEL) =>
      for
        id <- n.getValue[F, Long](PROP.HN_ID).map(HnId.apply)
        name <- n.getOptional[F, String](PROP.NAME).map(_.map(HnName.apply))
        description <- n.getOptional[F, String](PROP.DESCRIPTION).map(_.map(Description.apply))
        valueIndex <- n.getValue[F, Long](PROP.IO_INDEX).map(IoIndex.apply)
        conNode <- ConcreteNode(id, name, description, ioNode, valueIndex).pure
      yield conNode
    case _ => s"Node is not a hidden concrete node: $node".assertionError
