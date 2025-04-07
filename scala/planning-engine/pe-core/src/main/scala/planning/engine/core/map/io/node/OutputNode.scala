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

class OutputNode[F[_]: MonadThrow](
    val name: String,
    val variable: IoVariable[F, ?],
    protected val hiddenNodes: AtomicCell[F, ConcreteNodeMap[F]]
) extends IoNode[F]

object OutputNode:
  val propertyNodeType: String = "output"

  def apply[F[_]: Concurrent](name: String, variable: IoVariable[F, ?]): F[OutputNode[F]] =
    AtomicCell[F].of[ConcreteNodeMap[F]](Map.empty).map(ac => new OutputNode[F](name, variable, ac))
