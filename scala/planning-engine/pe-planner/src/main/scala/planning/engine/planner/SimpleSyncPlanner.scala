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
import org.typelevel.log4cats.LoggerFactory
import planning.engine.common.graph.io.{Action, Observation}
import planning.engine.planner.config.SimpleSyncPlannerConfig

trait SimpleSyncPlannerLike[F[_]]:
  def step(observation: Observation): F[Action]

// Synchronous planner have 2 key concepts (compared to asynchronous):
//   1. Each step is strictly bound to world time (step time), so plan path locking like:
//      (observation1 at time t) -> (action1 at time t) -> (observation2 at time t+1) -> (action2 at time t+1) -> ...
//      This is mean that if agend got observation1 at time t, it expects observation2 at time t+1, and
//      in case observation2 not observed then whole plan path become invalid.
//      (While asynchronous planner use fuzzy time bounding, so deviation
//      from expected t+1 just make plan path less probable).
//   2. Interaction between agent and world are synchronous, i.e. after receiving observation agent
//      will run planning until action is found (no new observation expected during the planing,
//      which mean world should not change until action executed).
//      (While asynchronous planner receiving stream of observations and run planning continuously and
//      produce stream of actions)
// Synchronous planner is good for tasks with synchronous IO, like chat-based agents,
// where each observation is a message from user and each action is a message to user.
// Asynchronous planner is good for application with asynchronous IO, like robotic/automotive agents,
// where each observation is a sensor reading and each action is a motor command.
class SimpleSyncPlanner[F[_]: Async](
//    mapGraph: MapGraphLake[F],
//    context: SimpleContextLike[F],
//    plan: SimplePlanLike[F]
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
  ): F[SimpleSyncPlanner[F]] = ???
//    for
//      dag <- PlanningDag[F]()
//      context <- SimpleContext[F](config.maxContextPathLength, dag)
//      plan <- SimplePlan[F](dag)
//    yield new SimpleSyncPlanner[F](mapGraph, context, plan)
