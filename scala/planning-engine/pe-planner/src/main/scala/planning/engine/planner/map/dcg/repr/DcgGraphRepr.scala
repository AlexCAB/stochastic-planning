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
| created: 2026-02-10 |||||||||||*/

package planning.engine.planner.map.dcg.repr

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.planner.map.dcg.DcgGraph

class DcgGraphRepr[F[_]: MonadThrow] extends StructureReprBase[F]:
  self: DcgGraph[F] =>

  lazy val repr: F[String] =
    for
      layers <- structure.traceAbsForestLayers(structure.conMnId)
      builtLayers = layers.map(buildLayerRepr)
      formatedLayers = builtLayers.map(formatLayerRepr)
      paths <- structure.allThenPaths
      (directs, loops, nooses) = groupPaths(paths)
    yield List(
      s"DcgGraph(${nodes.size} nodes, ${edges.size} edges, ${samples.size} samples):",
      "  ABSTRACT LAYERS:",
      renderLayerRepr(formatedLayers).mkString("\n"),
      "  PLANING PATHS:",
      "    Direct:",
      renderPathRepr(directs),
      "    Loop:",
      renderPathRepr(loops),
      "    Noose:",
      renderPathRepr(nooses)
    ).mkString("\n")
