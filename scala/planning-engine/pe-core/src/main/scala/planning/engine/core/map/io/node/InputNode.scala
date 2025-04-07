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

package planning.engine.core.map.io.node

import cats.MonadThrow
import cats.effect.kernel.Concurrent
import cats.effect.std.AtomicCell
import planning.engine.core.map.io.variable.IoVariable
import cats.syntax.all.*

class InputNode[F[_]: MonadThrow](
    val name: String,
    val variable: IoVariable[F, ?],
    protected val hiddenNodes: AtomicCell[F, ConcreteNodeMap[F]]
) extends IoNode[F]

object InputNode:
  val propertyNodeType: String = "input"

  def apply[F[_]: Concurrent](name: String, variable: IoVariable[F, ?]): F[InputNode[F]] =
    AtomicCell[F].of[ConcreteNodeMap[F]](Map.empty).map(ac => new InputNode[F](name, variable, ac))
