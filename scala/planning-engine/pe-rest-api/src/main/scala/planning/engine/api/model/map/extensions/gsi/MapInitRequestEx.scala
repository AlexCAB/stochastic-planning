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
| created: 2026-09-17 |||||||||||*/

package planning.engine.api.model.map.extensions.gsi

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.api.model.map.MapInitRequest
import planning.engine.api.model.map.payload.*
import planning.engine.common.errors.assertionError
import planning.engine.common.values.io.IoName
import planning.engine.map.data.MapMetadata
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}
import planning.engine.map.io.variable.*

object MapInitRequestEx:
  extension (req: MapInitRequest)
    private def toVariables[F[_]: MonadThrow](definition: IoNodeApiDef): F[IoVariable[F, ?]] = definition match
      case v: BooleanIoNodeDef if v.acceptableValues.nonEmpty => BooleanIoVariable[F](v.acceptableValues).pure
      case v: FloatIoNodeDef if v.min <= v.max                => FloatIoVariable[F](v.min, v.max).pure
      case v: IntIoNodeDef if v.min <= v.max                  => IntIoVariable[F](v.min, v.max).pure
      case v: ListStrIoNodeDef if v.elements.nonEmpty         => ListStrIoVariable[F](v.elements).pure
      case _ => s"Can't convert in/out node definition $definition to variable".assertionError

    private def toNode[F[_]: MonadThrow, N <: IoNode[F]](
        definitions: List[IoNodeApiDef],
        makeNode: (IoName, IoVariable[F, ?]) => F[N],
    ): F[List[N]] = definitions.traverse: definition =>
      for
        variable <- toVariables[F](definition)
        node <- makeNode(definition.name, variable)
      yield node

    def toMetadata[F[_]: MonadThrow]: F[MapMetadata] = MapMetadata(req.name, req.description).pure
    def toInputNodes[F[_]: MonadThrow]: F[List[InputNode[F]]] = toNode(req.inputNodes, InputNode[F](_, _).pure)
    def toOutputNodes[F[_]: MonadThrow]: F[List[OutputNode[F]]] = toNode(req.outputNodes, OutputNode[F](_, _).pure)
