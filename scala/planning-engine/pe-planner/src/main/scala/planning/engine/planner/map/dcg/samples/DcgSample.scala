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
| created: 2026-01-26 |||||||||||*/

package planning.engine.planner.map.dcg.samples

import planning.engine.common.graph.EndsGraph
import planning.engine.common.validation.Validation
import planning.engine.map.samples.sample.SampleData
import planning.engine.common.values.edges.Edge
import planning.engine.planner.map.dcg.repr.DcgSampleRepr

final case class DcgSample(
    data: SampleData,
    edges: Set[Edge]
) extends EndsGraph(edges) with Validation with DcgSampleRepr:
  lazy val validationName: String = s"DcgSample(id=${data.id}, name=${data.name.toStr})"

  lazy val validationErrors: List[Throwable] = validations(
    isConnected -> "DcgSample edges must form a connected graph"
  )

  override lazy val toString: String = s"DcgSample(${data.id.vStr}${data.name.repr}, edges sizes: ${edges.size})"
