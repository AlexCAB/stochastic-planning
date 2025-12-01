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

package planning.engine.planner.plan.dag

import cats.effect.Async
import cats.effect.std.AtomicCell
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory

trait PlanningDagLike[F[_]: Async]:
  def modifyContextBoundary[R](f: Set[StateNode[F]] => F[(Set[StateNode[F]], R)]): F[R]

final class PlanningDag[F[_]: {Async, LoggerFactory}](
    contextBoundary: AtomicCell[F, Set[StateNode[F]]],
    planningBoundary: AtomicCell[F, Set[StateNode[F]]]
) extends PlanningDagLike[F]:

  private val logger = LoggerFactory[F].getLogger

  def getContextBoundary: F[Set[StateNode[F]]] = contextBoundary.get
  def getPlanningBoundary: F[Set[StateNode[F]]] = planningBoundary.get
  
  override def modifyContextBoundary[R](f: Set[StateNode[F]] => F[(Set[StateNode[F]], R)]): F[R] =
    contextBoundary.evalModify: cb => 
      for 
        (newCb, r) <- f(cb)
        added = newCb -- cb
        removed = cb -- newCb
        _ <- logger.info(s"Updated context boundary: added = $added, removed = $removed, new boundary = $newCb")
      yield (newCb, r)


object PlanningDag:
  def apply[F[_]: {Async, LoggerFactory}](): F[PlanningDag[F]] =
    for
      contextBoundary <- AtomicCell[F].of(Set[StateNode[F]]())
      planningBoundary <- AtomicCell[F].of(Set[StateNode[F]]())
    yield new PlanningDag(contextBoundary, planningBoundary)
