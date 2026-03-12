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
| created: 2026-02-25 |||||||||||*/

package planning.engine.planner.map.repr

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.graph.GraphTracing.allLinksFilter
import planning.engine.common.graph.edges.EdgeKey.Link
import planning.engine.planner.map.dcg.samples.DcgSample

trait DcgSampleRepr[F[_]: MonadThrow] extends StructureReprBase[F]:
  self: DcgSample[F] =>

  protected def buildLayerRepr(layer: Set[Link]): List[List[String]] = layer
    .groupBy(_.src)
    .toList.sortBy(_._1.value)
    .map((src, ls) => src.reprNode +: ls.toList.sortBy(_.trg.value).map(l => s"|${l.reprArrow}${l.trg.reprNode}"))

  lazy val repr: F[String] =
    for
      layers <- structure.traceAbsDagLayers(structure.conMnId, allLinksFilter)
      builtLayers = layers.map(buildLayerRepr)
      formatedLayers = builtLayers.map(formatLayerRepr)
      paths <- structure.allThenPaths
      (directs, loops, nooses) = groupPaths(paths)
    yield List(
      List(s"DcgSample(${data.id.vStr}${data.name.map(n => ", " + n.value).getOrElse("")}):", "  ABSTRACT LAYERS:"),
      renderLayerRepr(formatedLayers).tab4,
      List("  PLANING PATHS:", "    Direct:"),
      renderPathRepr(directs).tab6,
      List("    Loop:"),
      renderPathRepr(loops).tab6,
      List("    Noose:"),
      renderPathRepr(nooses).tab6,
    ).flatten.mkString("\n")
