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
| created: 19.06.2026 |||||||||||*/

package planning.engine.planner.mpi.common.data.node

import planning.engine.planner.mpi.actors.manager.logic.Actor
import planning.engine.planner.mpi.actors.visualizer.VisualizerActor

final case class StaticActors(manager: Actor.Ref, visualizer: VisualizerActor.Ref)

object StaticActors:
  def apply()(using dfn: Actor.Definition, ctx: Actor.Ctx): StaticActors =
    new StaticActors(ctx.self, dfn.visualizer)
