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
| created: 09.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.visualizer.data

import planning.engine.planner.mpi.Visualization
import planning.engine.planner.mpi.model.data.map.Metadata
import planning.engine.planner.mpi.model.io.IoVars

private[visualizer] final case class Definition(
    metadata: Metadata,
    variables: IoVars,
    visualization: Visualization,
)
