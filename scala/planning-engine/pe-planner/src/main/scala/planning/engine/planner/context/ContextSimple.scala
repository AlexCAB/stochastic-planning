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
import cats.syntax.all.*
import planning.engine.common.values.node.IoIndex
import planning.engine.common.values.text.Name
import planning.engine.planner.dag.{PlanningDagLike, StateNode}
import planning.engine.common.errors.assertNoSameElems

trait ContextLike[F[_]: Async]:
  def moveNextFoundIntoContext(values: Map[Name, IoIndex]): F[Map[Name, IoIndex]]

final class ContextSimple[F[_]: Async](
    maxPathLength: Int,
    planningDag: PlanningDagLike[F],
    state: AtomicCell[F, ContextSimple.State[F]]
) extends ContextLike[F]:

  override def moveNextFoundIntoContext(values: Map[Name, IoIndex]): F[Map[Name, IoIndex]] =
    def updateNodesKind(contextBoundary: Set[StateNode[F]]): F[(Map[Name, IoIndex], Set[StateNode[F]])] =
      contextBoundary.foldLeft((values, Set[StateNode[F]]()).pure): (acc, node) =>
        for
          (vs, found) <- acc
          (newVs, moved) <- node.markThenChildrenAsPresentIfInValues(vs)
          _ <- (moved.movedToPresent, moved.movedToPast)
            .assertNoSameElems("Seems bug: State node cannot be moved to present and past at the same time")
        yield (newVs, (found ++ moved.movedToPresent) -- moved.movedToPast)
        
    // TODO: Here also should be cline up operation to limit context size (by `maxPathLength`)
    // TODO: but it's algorithm is not developed yet.
    planningDag.modifyContextBoundary: contextBoundary =>
      for
        (newValues, movedNodes) <- updateNodesKind(contextBoundary)
      yield (contextBoundary ++ movedNodes, newValues) 

object ContextSimple:
  final case class State[F[_]: MonadThrow]()

  def apply[F[_]: Async](maxPathLength: Int, planningDag: PlanningDagLike[F]): F[ContextSimple[F]] =
    AtomicCell[F].of(State()).map(state => new ContextSimple[F](maxPathLength, planningDag, state))
