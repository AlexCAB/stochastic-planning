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
import planning.engine.map.subgraph.ConcreteWithParentIds
import planning.engine.planner.context.SimpleContext.UpdateConcrete
import planning.engine.planner.plan.dag.{ConcreteStateNode, PlanningDagLike, StateNode}

// Simple context (graph) component:
//  - I represent observed current and past state of the agent world (i.e. it contains only observed hidden nodes from
//    the map graph).
//  - Context graph is a subgraph of the map graph.
//  - Context graph can be represented as a set of paths from root nodes (roots is oldest observed events)
//    to leaves (leaves is in context boundary and represents present state of the world).

trait SimpleContextLike[F[_]: Async]:
  def moveNextFoundIntoContext(values: Map[Name, IoIndex]): F[Map[Name, IoIndex]]
  def addObservedConcreteToContextBoundary(nodes: List[ConcreteWithParentIds[F]]): F[Unit]

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

  override def addObservedConcreteToContextBoundary(nodes: List[ConcreteWithParentIds[F]]): F[Unit] = ???
    //TODO: 1) To check of parents in context boundary, if so move them to past and create links.
    //TODO: 2) Add new nodes as present with links to parents to context boundary.
    //TODO:
    //TODO:


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
