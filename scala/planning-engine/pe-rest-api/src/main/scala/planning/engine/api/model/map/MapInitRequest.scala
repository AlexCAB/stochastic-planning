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
| created: 2025-04-23 |||||||||||*/

package planning.engine.api.model.map

import cats.effect.kernel.Concurrent
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import cats.MonadThrow
import planning.engine.common.errors.assertionError
import cats.syntax.all.*
import planning.engine.api.model.map.payload.*
import planning.engine.common.values.text.{Description, Name}
import planning.engine.map.graph.MapMetadata
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}
import planning.engine.map.io.variable.*
import planning.engine.api.model.values.*

final case class MapInitRequest(
    name: Option[Name],
    description: Option[Description],
    inputNodes: List[IoNodeApiDef],
    outputNodes: List[IoNodeApiDef]
):
  private def toVariables[F[_]: MonadThrow](definition: IoNodeApiDef): F[IoVariable[F, ?]] = definition match
    case v: BooleanIoNode if v.acceptableValues.nonEmpty => BooleanIoVariable[F](v.acceptableValues).pure
    case v: FloatIoNode if v.min <= v.max                => FloatIoVariable[F](v.min, v.max).pure
    case v: IntIoNode if v.min <= v.max                  => IntIoVariable[F](v.min, v.max).pure
    case v: ListStrIoNode if v.elements.nonEmpty         => ListStrIoVariable[F](v.elements).pure
    case _ => s"Can't convert in/out node definition $definition to variable".assertionError

  private def toNode[F[_]: Concurrent, N <: IoNode[F]](
      definitions: List[IoNodeApiDef],
      makeNode: (Name, IoVariable[F, ?]) => F[N]
  ): F[List[N]] = definitions.traverse: definition =>
    for
      variable <- toVariables[F](definition)
      node <- makeNode(definition.name, variable)
    yield node

  def toMetadata[F[_]: MonadThrow]: F[MapMetadata] = MapMetadata(name, description).pure
  def toInputNodes[F[_]: Concurrent]: F[List[InputNode[F]]] = toNode(inputNodes, InputNode[F](_, _).pure)
  def toOutputNodes[F[_]: Concurrent]: F[List[OutputNode[F]]] = toNode(outputNodes, OutputNode[F](_, _).pure)

object MapInitRequest:
  import io.circe.generic.auto.*

  implicit def decoder[F[_]: Concurrent]: EntityDecoder[F, MapInitRequest] = jsonOf[F, MapInitRequest]
