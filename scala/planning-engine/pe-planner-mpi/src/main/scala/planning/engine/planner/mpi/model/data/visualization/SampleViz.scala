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
| created: 2026-10-26 |||||||||||*/

package planning.engine.planner.mpi.model.data.visualization

import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.Name
import planning.engine.planner.mpi.model.data.samples.Sample

final case class SampleViz(
    id: SampleId,
    name: Option[Name],
    probabilityCount: Long,
    utility: Double,
    nodes: Set[(MnId, Option[HnName])],
    edges: Set[(MnId, MnId)],
)

object SampleViz:
  def apply(sample: Sample): SampleViz = SampleViz(
    id = sample.id,
    name = sample.info.map(_.name),
    probabilityCount = sample.props.probabilityCount,
    utility = sample.props.utility,
    nodes = sample.nodes.map((mnId, node) => mnId -> node.name).toSet,
    edges = sample.edges.map(key => key.src -> key.trg),
  )
