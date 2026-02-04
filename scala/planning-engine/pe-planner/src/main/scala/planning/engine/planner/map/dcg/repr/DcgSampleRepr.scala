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
| created: 2026-02-02 |||||||||||*/

package planning.engine.planner.map.dcg.repr

import planning.engine.common.graph.EndsGraph
import planning.engine.planner.map.dcg.samples.DcgSample

trait DcgSampleRepr:
  self: DcgSample & EndsGraph =>
  
  

  
  
  
  
  lazy val repr: String = ???
//    s"""DcgSample(${data.id.vStr}${data.name.repr}, edges:
//       |${edges.map { case (et, ends) => s"  ${ends.src.vStr} -${et.repr}-> ${ends.trg.vStr}" }.mkString("\n")}
//       |)""".stripMargin
