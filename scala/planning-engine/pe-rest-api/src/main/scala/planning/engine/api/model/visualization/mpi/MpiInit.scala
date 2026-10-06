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
| created: 2026-10-07 |||||||||||*/

package planning.engine.api.model.visualization.mpi

import planning.engine.api.model.visualization.VisualizationMsg
import planning.engine.common.values.io.IoName
import planning.engine.common.values.text.{Description, Name}

final case class MpiInit(
    name: Name,
    description: Option[Description],
    inVars: Set[IoName],
    outVars: Set[IoName],
) extends VisualizationMsg
