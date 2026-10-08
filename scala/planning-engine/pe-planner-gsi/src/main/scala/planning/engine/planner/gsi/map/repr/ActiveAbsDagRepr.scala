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
| created: 2026-03-08 |||||||||||*/

package planning.engine.planner.gsi.map.repr

import cats.syntax.all.*
import cats.syntax.ext.*
import planning.engine.common.repr.StructureReprBase
import planning.engine.planner.gsi.map.data.ActiveAbsDag

trait ActiveAbsDagRepr[F[_]: MT] extends StructureReprBase:
  self: ActiveAbsDag[F] =>

  lazy val repr: F[String] =
    for
      absLayers <- graph.reprAbsLayers
      backKeysRepr = backwordKeys.reprByTrg
    yield List(
      List(s"ActiveAbsDag(${graph.nodes.size} nodes, ${graph.edges.size} edges, ${graph.samples.size} samples):"),
      absLayers.tab2,
      List("  BACKWARD THEN KEYS:"),
      if backKeysRepr.isEmpty then List("    ---") else backKeysRepr.tab4,
      graph.reprNotConnectedNodes.tab2,
    ).flatten.mkString("\n")
