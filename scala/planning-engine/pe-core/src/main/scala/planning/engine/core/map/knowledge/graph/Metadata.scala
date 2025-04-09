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

package planning.engine.core.map.knowledge.graph

import cats.{Monad, MonadThrow}
import neotypes.model.types.Value
import planning.engine.common.values.Name
import planning.engine.common.properties.*
import cats.syntax.all.*

final case class Metadata(
    name: Name,
    description: Option[String]
):
  def toProperties[F[_]: Monad]: F[Map[String, Value]] =
    propsOf("name" -> name.value.map(v => Value.Str(v)), "description" -> description.map(v => Value.Str(v)))

object Metadata:
  def fromProperties[F[_]: MonadThrow](props: Map[String, Value]): F[Metadata] =
    for
      name <- props.getOptional[F, String]("name").map(Name.apply)
      description <- props.getOptional[F, String]("description")
    yield Metadata(name, description)
