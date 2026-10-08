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
| created: 2026-10-08 |||||||||||*/

package planning.engine.planner.mpi.repr

import cats.syntax.all.*
import cats.syntax.ext.MT
import planning.engine.common.graph.GraphStructure
import planning.engine.common.graph.GraphTracing.allLinksFilter
import planning.engine.common.graph.edges.MeKey.Link
import planning.engine.common.repr.StructureReprBase
import planning.engine.planner.mpi.model.data.samples.Sample

trait SampleRepr extends Representable with StructureReprBase:
  self: Sample =>

  private lazy val structure: GraphStructure = GraphStructure(edges.map(_.key))

  private def buildLayerRepr(layer: Set[Link]): List[List[String]] = layer
    .groupBy(_.src)
    .toList.sortBy(_._1.value)
    .map((src, ls) => src.reprNode +: ls.toList.sortBy(_.trg.value).map(l => s"|${l.reprArrow}${l.trg.reprNode}"))

  def repr[F[_]: MT]: F[String] =
    for
      layers <- structure.traceAbsDagLayers[F](structure.conMnId, allLinksFilter)
      builtLayers = layers.map(buildLayerRepr)
      formatedLayers = builtLayers.map(l => formatLayerRepr(l))
      paths <- structure.allThenPaths[F]
      (directs, loops, nooses) = groupPaths(paths)
    yield List(
      List(s"Sample(${id.vStr}${info.map(i => ", " + i.name.value).getOrElse("")}):", "  ABSTRACT LAYERS:"),
      renderLayerRepr(formatedLayers).tab4,
      List("  PLANING PATHS:", "    Direct:"),
      renderPathRepr(directs).tab6,
      List("    Loop:"),
      renderPathRepr(loops).tab6,
      List("    Noose:"),
      renderPathRepr(nooses).tab6,
    ).flatten.mkString("\n")
