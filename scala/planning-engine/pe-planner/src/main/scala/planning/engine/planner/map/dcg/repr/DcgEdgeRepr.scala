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

import cats.MonadThrow
import planning.engine.planner.map.dcg.edges.DcgEdge

trait DcgEdgeRepr[F[_]: MonadThrow]:
  self: DcgEdge[F] =>

  lazy val repr: String = s"(${ends.src.vStr}) -[${links.repr}${thens.repr}]-> (${ends.trg.vStr})"
  lazy val reprTarget: String = s"| -[${links.repr}${thens.repr}]-> (${ends.trg.vStr})"
