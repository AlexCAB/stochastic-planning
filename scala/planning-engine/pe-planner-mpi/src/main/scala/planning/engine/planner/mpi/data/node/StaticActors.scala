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

package planning.engine.planner.mpi.data.node

import planning.engine.planner.mpi.actors.manager.ManagerActor
import planning.engine.planner.mpi.actors.visualizer.VisualizerActor

final case class StaticActors(manager: ManagerActor.Ref, visualizer: VisualizerActor.Ref)

object StaticActors:
  def apply()(using dfn: ManagerActor.Definition, ctx: ManagerActor.Ctx): StaticActors =
    new StaticActors(ctx.self, dfn.visualizer)
