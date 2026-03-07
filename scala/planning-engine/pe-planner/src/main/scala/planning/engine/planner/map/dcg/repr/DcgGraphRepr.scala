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
import planning.engine.common.graph.GraphTracing.allLinksFilter
import planning.engine.common.graph.edges.EdgeKey.Link
import planning.engine.common.values.node.MnId
import planning.engine.planner.map.dcg.DcgGraph

class DcgGraphRepr[F[_]: MonadThrow] extends StructureReprBase[F]:
  self: DcgGraph[F] =>

  private[repr] def srcNode(id: MnId): String = nodes.get(id).map(_.repr).getOrElse(id.reprNode)

  private[repr] def buildLayerRepr(layer: Set[Link]): List[List[String]] = layer
    .groupBy(_.src).toList.sortBy(_._1.value)
    .map((src, ls) => src -> ls.toList.sortBy(_.trg.value))
    .map((src, ls) => srcNode(src) +: ls.map(l => s"|==>${l.trg.reprNode}"))

  private[repr] def builtTerminalLayer(layers: List[Set[Link]]): List[String] =
    val allLinks = layers.toSet.flatten
    (allLinks.map(_.trg) -- allLinks.map(_.src)).toList.sortBy(_.value).map(srcNode)

  private[repr] def renderNotConnectedNodes: String =
    val notConnectedNodes = mnIds -- edgesMdIds

    if notConnectedNodes.isEmpty then "---"
    else notConnectedNodes.flatMap(nodes.get).toList.sortBy(_.id.value).map(_.repr).mkString("\n    ")

  lazy val repr: F[String] =
    for
      layers <- structure.traceAbsForestLayers(structure.conMnId, allLinksFilter)
      builtLayers = layers.map(buildLayerRepr)
      terminalLayer = builtTerminalLayer(layers)
      formatedLayers = builtLayers.map(formatLayerRepr)
      paths <- structure.allThenPaths
      (directs, loops, nooses) = groupPaths(paths)
    yield List(
      s"DcgGraph(${nodes.size} nodes, ${edges.size} edges, ${samples.size} samples):",
      "  ABSTRACT LAYERS:",
      renderLayerRepr(formatedLayers).mkString("\n"),
      "    Terminal layer:",
      s"     ${terminalLayer.mkString(" ")}",
      "  PLANING PATHS:",
      "    Direct:",
      renderPathRepr(directs),
      "    Loop:",
      renderPathRepr(loops),
      "    Noose:",
      renderPathRepr(nooses),
      s"  NOT CONNECTED NODES:\n    $renderNotConnectedNodes"
    ).mkString("\n")
