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
import planning.engine.core.map.knowledge.graph.Metadata
import planning.engine.core.map.io.node.{InputNode, IoNode, OutputNode}
import planning.engine.common.errors.assertionError
import planning.engine.core.map.io.variable.*
import cats.syntax.all.*
import planning.engine.common.values.{Name, OpDescription, OpName}

final case class MapInitRequest(
    name: Option[String],
    description: Option[String],
    inputNodes: Vector[IoNodeApiDef],
    outputNodes: Vector[IoNodeApiDef]
):
  private def toVariables[F[_]: MonadThrow](definition: IoNodeApiDef): F[IoVariable[F, ?]] = definition match
    case v: BooleanIoNode if v.acceptableValues.nonEmpty => BooleanIoVariable[F](v.acceptableValues).pure
    case v: FloatIoNode if v.min <= v.max                => FloatIoVariable[F](v.min, v.max).pure
    case v: IntIoNode if v.min <= v.max                  => IntIoVariable[F](v.min, v.max).pure
    case v: ListStrIoNode if v.elements.nonEmpty         => ListStrIoVariable[F](v.elements).pure
    case _ => s"Can't convert in/out node definition $definition to variable".assertionError

  private def toNode[F[_]: Concurrent, N <: IoNode[F]](
      definitions: Vector[IoNodeApiDef],
      makeNode: (Name, IoVariable[F, ?]) => F[N]
  ): F[Vector[N]] = definitions.traverse: definition =>
    for
      variable <- toVariables[F](definition)
      name <- Name.fromString(definition.name)
      node <- makeNode(name, variable)
    yield node

  def toMetadata[F[_]: MonadThrow]: F[Metadata] = Metadata
    .apply(
      name = OpName.fromOption(name),
      description = OpDescription.fromOption(description)
    )
    .pure[F]

  def toInputNodes[F[_]: Concurrent]: F[Vector[InputNode[F]]] = toNode(inputNodes, InputNode[F](_, _))
  def toOutputNodes[F[_]: Concurrent]: F[Vector[OutputNode[F]]] = toNode(outputNodes, OutputNode[F](_, _))

object MapInitRequest:
  import io.circe.generic.auto.*

  implicit def decoder[F[_]: Concurrent]: EntityDecoder[F, MapInitRequest] = jsonOf[F, MapInitRequest]
