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

import cats.syntax.ext.MT
import planning.engine.api.model.visualization.VisualizationMsg
import planning.engine.common.values.io.IoName
import planning.engine.common.values.text.{Description, Name}
import planning.engine.planner.mpi.model.data.map.Metadata
import planning.engine.planner.mpi.model.io.IoVars

final case class MpiInit(
    name: Name,
    description: Option[Description],
    inVars: Set[IoName],
    outVars: Set[IoName],
) extends VisualizationMsg

object MpiInit:
  def apply[F[_]: MT](metadata: Metadata, variables: IoVars): F[MpiInit] = MT(
    new MpiInit(
      name = metadata.name,
      description = metadata.description,
      inVars = variables.in.keySet,
      outVars = variables.out.keySet,
    ),
  )
