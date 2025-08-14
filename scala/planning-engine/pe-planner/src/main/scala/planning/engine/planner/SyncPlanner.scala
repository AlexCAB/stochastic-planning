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
import planning.engine.planner.io.Observation

import java.awt.Desktop.Action

trait SyncPlannerLike[F[_]]:
  def step(observation: Observation): F[Action]


class SyncPlanner[F[_] : Async] extends SyncPlannerLike[F]:
  override def step(observation: Observation): F[Action] = ???
         