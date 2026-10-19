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
| created: 2026-10-20 |||||||||||*/

package planning.engine.planner.mpi.actors.visualizer.logic.modules

import cats.syntax.all.*
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import planning.engine.planner.mpi.actors.visualizer.data.Message
import planning.engine.planner.mpi.actors.visualizer.logic.Actor

private[visualizer] trait Plan:
  self: Actor.type =>
  import Message.*
  
  private[visualizer] def doActiveStepsFound[F[_]: S](msg: ShowActiveStepsFound)(using d: Def, c: Ctx): F[Bhv] =
    for
      _ <- logInfo(s"[ActiveStepsFound] time: ${msg.time}, active forest: ${msg.steps.activeForest}")
      _ <- d.visualization.activeStepsFound(msg.time, msg.steps.activeForest)
    yield Behaviors.same
