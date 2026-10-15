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
| created: 2026-10-15 |||||||||||*/

package planning.engine.planner.mpi.actors.node.data.parts

import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.planner.Planner
import planning.engine.planner.mpi.actors.visualizer.Visualizer

final case class Actors(
    manager: Manager,
    visualizer: Option[Visualizer],
    planner: Planner,
)
