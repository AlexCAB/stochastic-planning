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

import planning.engine.common.enums.EdgeType
import planning.engine.common.graph.EndsGraph
import planning.engine.common.validation.Validation
import planning.engine.map.samples.sample.SampleData
import planning.engine.common.values.edges.EndIds

final case class DcgSample(
    data: SampleData,
    edges: Set[(EdgeType, EndIds)]
) extends EndsGraph(edges.map(_._2)) with Validation:
  lazy val validationName: String = s"DcgSample(id=${data.id}, name=${data.name.toStr})"

  lazy val validationErrors: List[Throwable] = validations(
    edges.toList.map(_._2).isDistinct("DcgSample edges must have distinct end ids"),
    isConnected -> "DcgSample edges must form a connected graph"
  )

  lazy val repr: String =
    s"""DcgSample(${data.id.vStr}${data.name.repr}, edges:
      |${edges.map { case (et, ends) => s"  ${ends.src.vStr} -${et.repr}-> ${ends.trg.vStr}" }.mkString("\n")}
      |)""".stripMargin

  override lazy val toString: String =   s"DcgSample(${data.id.vStr}${data.name.repr}, edges sizes: ${edges.size})"

