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
import planning.engine.common.values.text.Name
import planning.engine.common.values.node.HnId
import planning.engine.common.properties.*
import neotypes.query.QueryArg.Param
import planning.engine.common.values.db.Neo4j.{HN_LABEL, ABSTRACT_LABEL}
import planning.engine.common.errors.assertionError
import planning.engine.map.hidden.edge.EdgeState

final case class AbstractNode[F[_]: MonadThrow](
    id: HnId,
    name: Option[Name],
    parents: List[HiddenNode[F]],
    children: List[EdgeState[F]]
) extends HiddenNode[F]:

  override def toProperties: F[Map[String, Param]] = paramsOf(
    PROP.HN_ID -> id.toDbParam,
    PROP.NAME -> name.map(_.toDbParam)
  )

  override def equals(that: Any): Boolean = that match
    case obj: AbstractNode[?] => this.id == obj.id && this.name == obj.name
    case _                    => false

  override def toString: String = s"AbstractHiddenNode(id=$id, name=$name)"

object AbstractNode:
  final case class New(name: Option[Name])

  def apply[F[_]: MonadThrow](id: HnId, name: Option[Name]): F[AbstractNode[F]] =
    new AbstractNode[F](id, name, parents = List.empty, children = List.empty).pure

  def fromNode[F[_]: MonadThrow](node: Node): F[AbstractNode[F]] = node match
    case n if n.is(HN_LABEL) && n.is(ABSTRACT_LABEL) =>
      for
        id <- n.getValue[F, Long](PROP.HN_ID).map(HnId.apply)
        name <- n.getOptional[F, String](PROP.NAME).map(_.map(Name.apply))
        concreteNode <- AbstractNode(id, name)
      yield concreteNode
    case _ => s"Node is not a hidden abstract node: $node".assertionError
