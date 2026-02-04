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

import planning.engine.planner.map.dcg.edges.DcgEdgeData

trait DcgEdgeDataRepr:
  self: DcgEdgeData =>

  lazy val repr: String = s"(${ends.src.vStr}) -[${links.repr}${thens.repr}]-> (${ends.trg.vStr})"
  lazy val reprTarget: String = s"| -[${links.repr}${thens.repr}]-> (${ends.trg.vStr})"
