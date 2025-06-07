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
| created: 2025-03-25 |||||||||||*/

package planning.engine.map.io.node

import cats.MonadThrow
import cats.effect.kernel.Concurrent
import cats.syntax.all.*
import planning.engine.common.values.text.Name
import planning.engine.map.io.variable.IoVariable

class InputNode[F[_]: MonadThrow](
    override val name: Name,
    override val variable: IoVariable[F, ?]
) extends IoNode[F]

object InputNode:
  def apply[F[_]: Concurrent](name: Name, variable: IoVariable[F, ?]): F[InputNode[F]] =
    new InputNode[F](name, variable).pure
