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
import planning.engine.common.values.text.{Description, Name}
import planning.engine.common.values.node.HnId
import planning.engine.common.properties.*
import neotypes.query.QueryArg.Param
import planning.engine.common.values.db.Neo4j.{ABSTRACT_LABEL, HN_LABEL}
import planning.engine.common.errors.assertionError
import planning.engine.common.validation.Validation

final case class AbstractNode[F[_]: MonadThrow](
    id: HnId,
    name: Option[Name],
    description: Option[Description]
) extends HiddenNode[F]:

  override def toString: String = s"AbstractHiddenNode(id=$id, name=$name, description=$description)"

object AbstractNode:
  final case class New(name: Option[Name], description: Option[Description]) extends Validation:
    lazy val validationName: String = s"AbstractNode.New(name=$name)"

    lazy val validationErrors: List[Throwable] = validations(
      name.forall(_.value.nonEmpty) -> "Name must not be empty if defined"
    )

    def toProperties[F[_]: MonadThrow](id: HnId, initNextHnIndex: Long): F[Map[String, Param]] = paramsOf(
      PROP.HN_ID -> id.toDbParam,
      PROP.NAME -> name.map(_.toDbParam),
      PROP.DESCRIPTION -> description.map(_.toDbParam),
      PROP.NEXT_HN_INDEX -> initNextHnIndex.toDbParam
    )

  final case class ListNew(list: List[New])

  object ListNew:
    def of(elems: New*): ListNew = ListNew(elems.toList)

  def fromNode[F[_]: MonadThrow](node: Node): F[AbstractNode[F]] = node match
    case n if n.is(HN_LABEL) && n.is(ABSTRACT_LABEL) =>
      for
        id <- n.getValue[F, Long](PROP.HN_ID).map(HnId.apply)
        name <- n.getOptional[F, String](PROP.NAME).map(_.map(Name.apply))
        description <- n.getOptional[F, String](PROP.DESCRIPTION).map(_.map(Description.apply))
        absNode <- AbstractNode(id, name, description).pure
      yield absNode
    case _ => s"Node is not a hidden abstract node: $node".assertionError
