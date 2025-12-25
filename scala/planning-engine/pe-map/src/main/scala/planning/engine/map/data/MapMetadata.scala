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

package planning.engine.map.data

import cats.MonadThrow
import cats.syntax.all.*
import neotypes.model.types.{Node, Value}
import neotypes.query.QueryArg.Param
import planning.engine.common.errors.assertionError
import planning.engine.common.properties.*
import planning.engine.common.values.db.Neo4j.ROOT_LABEL
import planning.engine.common.values.text.{Description, Name}

final case class MapMetadata(
    name: Option[Name],
    description: Option[Description]
):
  def toQueryParams[F[_]: MonadThrow]: F[Map[String, Param]] =
    paramsOf(PROP.NAME -> name.map(_.toDbParam), PROP.DESCRIPTION -> description.map(_.toDbParam))

object MapMetadata:
  def fromNode[F[_]: MonadThrow](node: Node): F[MapMetadata] = node match
    case n if n.is(ROOT_LABEL) =>
      for
        name <- node.getOptional[F, String](PROP.NAME).flatMap(Name.fromString)
        description <- node.getOptional[F, String](PROP.DESCRIPTION).flatMap(Description.fromString)
      yield MapMetadata(name, description)
    case _ => s"Not a root node, $node".assertionError
    
  lazy val empty: MapMetadata = MapMetadata(None, None)
