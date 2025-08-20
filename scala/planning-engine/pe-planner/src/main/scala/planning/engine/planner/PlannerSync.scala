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
import planning.engine.planner.io.{Action, Observation}

trait PlannerSyncLike[F[_]]:
  def step(observation: Observation): F[Action]


class PlannerSync[F[_] : Async](mapGraph: MapGraphLake[F]) extends PlannerSyncLike[F]:
  
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
         