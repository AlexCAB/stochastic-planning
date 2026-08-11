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

package planning.engine.planner.mpi.actors.manager.data

import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.manager.logic.{Actor, ApiImpl}
import planning.engine.planner.mpi.actors.visualizer.Visualizer

private[manager] final case class Definition(
    visualizer: Visualizer,
):
  def self(using ctx: Actor.Ctx): Manager = ApiImpl(ctx.self)
