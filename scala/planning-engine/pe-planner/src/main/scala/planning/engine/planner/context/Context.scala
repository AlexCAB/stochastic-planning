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
| created: 2025-08-17 |||||||||||*/

package planning.engine.planner.context

import cats.MonadThrow
import cats.effect.std.AtomicCell
import cats.effect.Async
import planning.engine.planner.dag.StateNode
import cats.syntax.all.*

trait ContextLike[F[_]: Async]

final class Context[F[_]: Async](maxPathLength: Int, state: AtomicCell[F, Context.State[F]]) extends ContextLike[F]

object Context:
  final case class State[F[_]: MonadThrow](

      // The `present` represents the current world state observed by the agent.
      // All parent nodes behind represents agent world past.
      present: List[StateNode[F]]
  )

  def apply[F[_]: Async](maxPathLength: Int): F[Context[F]] =
    AtomicCell[F].of(State(List.empty)).map(state => new Context[F](maxPathLength, state))


