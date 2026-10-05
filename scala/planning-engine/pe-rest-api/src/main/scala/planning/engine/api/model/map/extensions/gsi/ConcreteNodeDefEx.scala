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
| created: 2026-10-05 |||||||||||*/

package planning.engine.api.model.map.extensions.gsi

import cats.MonadThrow
import cats.syntax.all.*
import cats.syntax.ext.*
import planning.engine.api.model.map.payload.ConcreteNodeDef
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.map.hidden.node.ConcreteNode
import planning.engine.map.io.node.IoNode
import planning.engine.map.io.variable.*
import planning.engine.common.errors.assertionError

object ConcreteNodeDefEx:
  extension (node: ConcreteNodeDef)
    def toNew[F[_]: MT](getIoNode: IoName => F[IoNode[F]]): F[ConcreteNode.New] =
      def parseValue(variable: IoVariable[F, ?]): F[IoIndex] = variable match
        case v: BooleanIoVariableLike[F] => MonadThrow[F].fromEither(node.value.as[Boolean]).flatMap(v.indexForValue)
        case v: FloatIoVariableLike[F]   => MonadThrow[F].fromEither(node.value.as[Double]).flatMap(v.indexForValue)
        case v: IntIoVariableLike[F]     => MonadThrow[F].fromEither(node.value.as[Long]).flatMap(v.indexForValue)
        case v: ListStrIoVariableLike[F] => MonadThrow[F].fromEither(node.value.as[String]).flatMap(v.indexForValue)
        case v => s"Unsupported variable type for value: ${node.value}, variable: $v".assertionError

      for
        ioNode <- getIoNode(node.ioNodeName)
        valueIndex <- parseValue(ioNode.variable)
      yield ConcreteNode.New(
        name = Some(node.name),
        description = node.description,
        ioNodeName = node.ioNodeName,
        valueIndex = valueIndex,
      )
