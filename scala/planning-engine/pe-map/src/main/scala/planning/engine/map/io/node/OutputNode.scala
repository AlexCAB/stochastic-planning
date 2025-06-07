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

class OutputNode[F[_]: MonadThrow](
    override val name: Name,
    override val variable: IoVariable[F, ?]
) extends IoNode[F]

object OutputNode:
  def apply[F[_]: Concurrent](name: Name, variable: IoVariable[F, ?]): F[OutputNode[F]] =
    new OutputNode[F](name, variable).pure
