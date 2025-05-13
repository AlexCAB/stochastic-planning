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
import planning.engine.common.values.{OpDescription, OpName}
import planning.engine.common.properties.*
import cats.syntax.all.*
import neotypes.query.QueryArg.Param
import planning.engine.common.errors.assertionError
import planning.engine.map.database.Neo4jQueries.ROOT_LABEL

final case class Metadata(
    name: OpName,
    description: OpDescription
):
  def toQueryParams[F[_]: MonadThrow]: F[Map[String, Param]] =
    paramsOf(PROP_NAME.NAME -> name.value.toDbParam, PROP_NAME.DESCRIPTION -> description.value.toDbParam)

object Metadata:
  def fromProperties[F[_]: MonadThrow](props: Map[String, Value]): F[Metadata] =
    for
      name <- props.getOptional[F, String](PROP_NAME.NAME).map(OpName.apply)
      description <- props.getOptional[F, String](PROP_NAME.DESCRIPTION).map(OpDescription.apply)
    yield Metadata(name, description)

  def withName(name: Option[String]): Metadata = Metadata(OpName.fromOption(name), OpDescription.empty)

  def apply(name: String, description: String): Metadata =
    Metadata(OpName.fromString(name), OpDescription.fromString(description))

  def fromNode[F[_]: MonadThrow](node: Node): F[Metadata] = node match
    case Node(_, labels, props) if labels.exists(_.equalsIgnoreCase(ROOT_LABEL)) => fromProperties[F](props)
    case _ => s"Not a root node, $node".assertionError
