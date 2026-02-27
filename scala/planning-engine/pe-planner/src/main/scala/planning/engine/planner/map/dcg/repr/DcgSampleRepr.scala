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

package planning.engine.planner.map.dcg.repr

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.graph.edges.EdgeKey.Link
import planning.engine.planner.map.dcg.samples.DcgSample

trait DcgSampleRepr[F[_]: MonadThrow]:
  self: DcgSample[F] =>

  private[repr] def buildLayerRepr(layer: Set[Link]): List[List[String]] = layer
    .groupBy(_.src)
    .toList.sortBy(_._1.value)
    .map((src, ls) => src.reprNode +: ls.toList.sortBy(_.trg.value).map(l => s"|${l.reprArrow}${l.trg.reprNode}"))

  private[repr] def formatLayerRepr(layer: List[List[String]]): List[String] =
    val maxColSize = layer.map(_.size).max
    layer
      .map(column => column ++ List.fill(maxColSize - column.size)(""))
      .map: column =>
        val maxRecLen = column.map(_.length).max
        column.map(s => s + " " * (maxRecLen - s.length))
      .transpose
      .map(_.mkString(" "))

  private[repr] def renderLayerRepr(layers: List[List[String]]): List[String] = layers
    .map(lines => lines.map(l => "    " + l).mkString("\n"))
    .zipWithIndex
    .map((l, i) => s"    Layer $i:\n" + l + "\n")

  lazy val repr: F[String] =
    for
      layers <- structure.traceAbsForestLayers(structure.conMnId)
      builtLayers = layers.map(buildLayerRepr)
      formatedLayers = builtLayers.map(formatLayerRepr)
    yield List(
      s"DcgSample(${data.id.vStr}${data.name.map(n => ", " + n.value).getOrElse("")}):",
      "  ABSTRACT LAYERS:",
      renderLayerRepr(formatedLayers).mkString("\n")
    ).mkString("\n")
