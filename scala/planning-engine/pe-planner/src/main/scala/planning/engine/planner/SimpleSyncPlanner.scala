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
| created: 2025-08-13 |||||||||||*/

package planning.engine.planner

import cats.effect.Async
import planning.engine.map.MapGraphLake
import planning.engine.planner.context.{SimpleContext, SimpleContextLike}
import planning.engine.planner.io.{Action, Observation}
import planning.engine.planner.plan.{SimplePlan, SimplePlanLike}
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.planner.config.SimpleSyncPlannerConfig
import planning.engine.planner.plan.dag.PlanningDag

trait SimpleSyncPlannerLike[F[_]]:
  def step(observation: Observation): F[Action]

class SimpleSyncPlanner[F[_]: Async](
    mapGraph: MapGraphLake[F],
    context: SimpleContextLike[F],
    plan: SimplePlanLike[F]
) extends SimpleSyncPlannerLike[F]:

  // Implement the step method to process the observation and return an action
  // General algorithm:
  //   1. Load observed hidden nodes to context:
  //       - Fond already loaded during planning process
  //       - Load new nodes from mapGraph
  //   2. Run planning process until action is found:
  //       - Run abstraction up to top level
  //       - Run concretization down to action
  //       - If action is found, return it, otherwise return empty (client agent will have to pick random action)
  override def step(observation: Observation): F[Action] = ???
//    for
//      valuesNotInContext <- context.moveNextFoundIntoContext(observation.values)
//      observedConcreteNodes <- mapGraph.findConcreteNodesByIoValues(valuesNotInContext)
//      _ <- context.addObservedConcreteToContextBoundary(observedConcreteNodes)
//
//    // TODO Here should be planning process to find action
//    yield Action.empty

object SimpleSyncPlanner:
  def apply[F[_]: {Async, LoggerFactory}](
      config: SimpleSyncPlannerConfig,
      mapGraph: MapGraphLake[F]
  ): F[SimpleSyncPlanner[F]] =
    for
      dag <- PlanningDag[F]()
      context <- SimpleContext[F](config.maxContextPathLength, dag)
      plan <- SimplePlan[F](dag)
    yield new SimpleSyncPlanner[F](mapGraph, context, plan)
