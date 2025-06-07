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

package planning.engine.map.graph

import cats.MonadThrow
import neotypes.model.types.{Node, Value}
import planning.engine.common.properties.*
import cats.syntax.all.*
import neotypes.query.QueryArg.Param
import planning.engine.common.errors.assertionError
import planning.engine.common.values.text.Description
import planning.engine.common.values.text.Name
import planning.engine.map.database.Neo4jQueries.ROOT_LABEL

final case class MapMetadata(
    name: Option[Name],
    description: Option[Description]
):
  def toQueryParams[F[_]: MonadThrow]: F[Map[String, Param]] =
    paramsOf(PROP_NAME.NAME -> name.map(_.toDbParam), PROP_NAME.DESCRIPTION -> description.map(_.toDbParam))

object MapMetadata:
  def fromNode[F[_]: MonadThrow](node: Node): F[MapMetadata] = node match
    case n if n.is(ROOT_LABEL) =>
      for
        name <- node.getOptional[F, String](PROP_NAME.NAME).flatMap(Name.fromString)
        description <- node.getOptional[F, String](PROP_NAME.DESCRIPTION).flatMap(Description.fromString)
      yield MapMetadata(name, description)
    case _ => s"Not a root node, $node".assertionError
