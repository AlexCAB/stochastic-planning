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
| created: 2025-04-10 |||||||||||*/

package planning.engine.map.knowledge.graph

import cats.MonadThrow
import neotypes.model.types.{Node, Value}
import planning.engine.common.properties.*
import cats.syntax.all.*
import neotypes.query.QueryArg.Param
import planning.engine.common.errors.assertionError
import planning.engine.common.values.text.Description
import planning.engine.common.values.text.Name
import planning.engine.map.database.Neo4jQueries.ROOT_LABEL

final case class Metadata(
    name: Option[Name],
    description: Option[Description]
):
  def toQueryParams[F[_]: MonadThrow]: F[Map[String, Param]] =
    paramsOf(PROP_NAME.NAME -> name.map(_.toDbParam), PROP_NAME.DESCRIPTION -> description.map(_.toDbParam))

object Metadata:
  def fromProperties[F[_]: MonadThrow](props: Map[String, Value]): F[Metadata] =
    for
      name <- props.getOptional[F, String](PROP_NAME.NAME).map(Name.fromOptionString)
      description <- props.getOptional[F, String](PROP_NAME.DESCRIPTION).map(Description.fromOptionString)
    yield Metadata(name, description)

  def withName(name: Option[String]): Metadata = Metadata(Name.fromOptionString(name), None)

  def apply(name: String, description: String): Metadata =
    Metadata(Name.fromStringOptional(name), Description.fromStringOptional(description))

  def fromNode[F[_]: MonadThrow](node: Node): F[Metadata] = node match
    case Node(_, labels, props) if labels.exists(_.equalsIgnoreCase(ROOT_LABEL)) => fromProperties[F](props)
    case _ => s"Not a root node, $node".assertionError
