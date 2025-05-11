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
import cats.effect.std.AtomicCell
import cats.syntax.all.*
import planning.engine.common.values.Name
import planning.engine.map.io.variable.IoVariable

class InputNode[F[_]: MonadThrow](
    override val name: Name,
    override val variable: IoVariable[F, ?],
    override protected val hiddenNodes: AtomicCell[F, ConcreteNodeMap[F]]
) extends IoNode[F]

object InputNode:
  val IN_NODE_TYPE: String = "input"

  def apply[F[_]: Concurrent](name: Name, variable: IoVariable[F, ?]): F[InputNode[F]] =
    AtomicCell[F].of[ConcreteNodeMap[F]](Map.empty).map(ac => new InputNode[F](name, variable, ac))
