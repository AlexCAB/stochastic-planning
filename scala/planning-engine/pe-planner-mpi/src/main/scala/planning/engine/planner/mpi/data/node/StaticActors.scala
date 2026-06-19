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

import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.visualizer.Visualizer

final case class StaticActors(manager: Manager.Ref, visualizer: Visualizer.Ref)

object StaticActors:
  def apply()(using dfn: Manager.Definition, ctx: Manager.Ctx): StaticActors =
    new StaticActors(ctx.self, dfn.visualizer)
