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

package planning.engine.planner.map.repr

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.planner.map.data.ActiveAbsDag

trait ActiveAbsDagRepr[F[_]: MonadThrow] extends StructureReprBase[F]:
  self: ActiveAbsDag[F] =>

  lazy val repr: F[String] =
    for
      absLayers <- graph.reprAbsLayers
      backKeysRepr = backwordKeys.reprByTrg
    yield List(
      List(s"ActiveAbsDag(${graph.nodes.size} nodes, ${graph.edges.size} edges, ${graph.samples.size} samples):"),
      absLayers.tab2,
      List("  BACKWARD THEN KEYS:"),
      backKeysRepr.tab4
    ).flatten.mkString("\n")
