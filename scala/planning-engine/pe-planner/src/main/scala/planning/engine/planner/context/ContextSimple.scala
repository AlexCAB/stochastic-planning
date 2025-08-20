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

trait ContextLike[F[_]: Async]:
  def moveNextFoundIntoContext(values: Map[Name, IoIndex]): F[Map[Name, IoIndex]]

final class ContextSimple[F[_]: Async](
    maxPathLength: Int,
    planningDag: PlanningDagLike[F],
    state: AtomicCell[F, ContextSimple.State[F]]
) extends ContextLike[F]:

  override def moveNextFoundIntoContext(values: Map[Name, IoIndex]): F[Map[Name, IoIndex]] =
    planningDag.modifyContextBoundary: contextBoundary =>

      contextBoundary.foldLeft(values, Set[StateNode[F]]())
    
     
      
     

object ContextSimple:
  final case class State[F[_]: MonadThrow]()

  def apply[F[_]: Async](maxPathLength: Int, planningDag: PlanningDagLike[F]): F[ContextSimple[F]] =
    AtomicCell[F].of(State()).map(state => new ContextSimple[F](maxPathLength, planningDag, state))
