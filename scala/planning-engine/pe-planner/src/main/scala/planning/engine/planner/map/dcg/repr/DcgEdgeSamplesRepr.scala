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

import planning.engine.planner.map.dcg.edges.DcgEdgeSamples

trait DcgEdgeSamplesRepr:
  self: DcgEdgeSamples =>

  lazy val repr: String = if indexies.nonEmpty then if isInstanceOf[DcgEdgeSamples.Links] then "L" else "T" else "_"
