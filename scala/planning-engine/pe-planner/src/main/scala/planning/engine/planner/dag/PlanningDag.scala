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
| created: 2025-08-20 |||||||||||*/

package planning.engine.planner.dag

import cats.effect.Async
import cats.effect.std.AtomicCell
import cats.syntax.all.*

trait PlanningDagLike[F[_]: Async]:
  def modifyContextBoundary[R](f: Set[StateNode[F]] => F[(Set[StateNode[F]], R)]): F[R]

final class PlanningDag[F[_]: Async](
    contextBoundary: AtomicCell[F, Set[StateNode[F]]],
    planningBoundary: AtomicCell[F, Set[StateNode[F]]]
) extends PlanningDagLike[F]:
  
  override def modifyContextBoundary[R](f: Set[StateNode[F]] => F[(Set[StateNode[F]], R)]): F[R] = ???

object PlanningDag:
  def apply[F[_]: Async](): F[PlanningDag[F]] =
    for
      contextBoundary <- AtomicCell[F].of(Set[StateNode[F]]())
      planningBoundary <- AtomicCell[F].of(Set[StateNode[F]]())
    yield new PlanningDag(contextBoundary, planningBoundary)
