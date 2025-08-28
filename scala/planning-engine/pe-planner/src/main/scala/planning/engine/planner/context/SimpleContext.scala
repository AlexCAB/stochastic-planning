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
import org.typelevel.log4cats.LoggerFactory
import planning.engine.common.values.node.IoIndex
import planning.engine.common.values.text.Name
import planning.engine.planner.dag.{ConcreteStateNode, PlanningDagLike, StateNode}
import planning.engine.map.hidden.node.ConcreteNode
import planning.engine.planner.context.SimpleContext.UpdateConcrete

trait SimpleContextLike[F[_]: Async]:
  def moveNextFoundIntoContext(values: Map[Name, IoIndex]): F[Map[Name, IoIndex]]
  def addObservedConcreteToContextBoundary(nodes: List[ConcreteNode[F]]): F[Unit]

final class SimpleContext[F[_]: {Async, LoggerFactory}](
    maxPathLength: Int,
    planningDag: PlanningDagLike[F],
    state: AtomicCell[F, SimpleContext.State[F]]
) extends SimpleContextLike[F]:

  private val logger = LoggerFactory[F].getLogger

  override def moveNextFoundIntoContext(values: Map[Name, IoIndex]): F[Map[Name, IoIndex]] =
    def findNodesToUpdate(contextBoundary: Set[StateNode[F]]): F[UpdateConcrete[F]] =
      contextBoundary.foldLeft(UpdateConcrete.empty.pure): (acc, node) =>
        for
          up <- acc
          foundChild <- node.findThenChildNodesInValues(values)
          _ <- logger.info(s"For node $node foundChild: $foundChild")
        yield up.copy(
          moveToPresent = up.moveToPresent ++ foundChild,
          moveToPast = if foundChild.nonEmpty then up.moveToPast + node else up.moveToPast
        )

    def updateNodeState(contextBoundary: Set[StateNode[F]], update: UpdateConcrete[F]): F[Set[StateNode[F]]] =
      for
        _ <- update.moveToPast.toList.traverse(_.setPast())
        _ <- logger.info(s"Moved to present nodes: ${update.moveToPast.map(_.id)}")
        _ <- update.moveToPresent.toList.traverse(_.setPresent())
        _ <- logger.info(s"Moved to past nodes: ${update.moveToPresent.map(_.id)}")
      yield (contextBoundary -- update.moveToPast) ++ update.moveToPresent

    // TODO: Here also should be cline up operation to limit context size (by `maxPathLength`)
    // TODO: but it's algorithm is not developed yet.

    planningDag.modifyContextBoundary: contextBoundary =>
      for
        _ <- logger.info(s"Moving nodes for values: $values, current context boundary: ${contextBoundary.map(_.id)}")
        toUpdate <- findNodesToUpdate(contextBoundary)
        newContextBoundary <- updateNodeState(contextBoundary, toUpdate)
        _ <- logger.info(s"New context boundary: ${newContextBoundary.map(_.id)}")
        newValues = values -- toUpdate.moveToPresent.map(_.ioNode.name)
        _ <- logger.info(s"Values for which next plan node not found: $newValues")
      yield (newContextBoundary, newValues)

  override def addObservedConcreteToContextBoundary(nodes: List[ConcreteNode[F]]): F[Unit] = ???

object SimpleContext:
  final case class State[F[_]: MonadThrow]()

  def apply[F[_]: {Async, LoggerFactory}](maxPathLength: Int, planningDag: PlanningDagLike[F]): F[SimpleContext[F]] =
    AtomicCell[F].of(State()).map(state => new SimpleContext[F](maxPathLength, planningDag, state))

  final case class UpdateConcrete[F[_]: MonadThrow](
      moveToPresent: Set[ConcreteStateNode[F]],
      moveToPast: Set[StateNode[F]]
  )

  object UpdateConcrete:
    def empty[F[_]: MonadThrow]: UpdateConcrete[F] = UpdateConcrete(Set(), Set())
