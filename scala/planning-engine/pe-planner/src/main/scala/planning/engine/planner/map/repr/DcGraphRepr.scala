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

package planning.engine.planner.map.repr

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.graph.GraphTracing.allLinksFilter
import planning.engine.common.graph.edges.MeKey.Link
import planning.engine.common.repr.StructureReprBase
import planning.engine.common.values.node.MnId
import planning.engine.planner.map.dcg.DcGraph

class DcGraphRepr[F[_]: MonadThrow] extends StructureReprBase[F]:
  self: DcGraph[F] =>

  private[repr] def srcNode(id: MnId): String = nodes.get(id).map(_.repr).getOrElse(id.reprNode)

  private[repr] def buildLayerRepr(layer: Set[Link]): List[List[String]] = layer
    .groupBy(_.src).toList.sortBy(_._1.value)
    .map((src, ls) => src -> ls.toList.sortBy(_.trg.value))
    .map((src, ls) => srcNode(src) +: ls.map(l => s"|==>${l.trg.reprNode}"))

  private[repr] def builtTerminalLayer(layers: List[Set[Link]]): List[String] =
    val allLinks = layers.toSet.flatten
    (allLinks.map(_.trg) -- allLinks.map(_.src)).toList.sortBy(_.value).map(srcNode)

  private[repr] def renderNotConnectedNodes: List[String] =
    val notConnectedNodes = mnIds -- edgesMdIds

    if notConnectedNodes.isEmpty then List("---")
    else notConnectedNodes.flatMap(nodes.get).toList.sortBy(_.id.value).map(_.repr)

  lazy val reprAbsLayers: F[List[String]] =
    for
      layers <- structure.traceAbsDagLayers(structure.conMnId, allLinksFilter)
      builtLayers = layers.map(buildLayerRepr)
      terminalLayer = builtTerminalLayer(layers).tab4
      formatedLayers = builtLayers.map(formatLayerRepr)
      renderLayer = renderLayerRepr(formatedLayers).tab2
    yield List(
      List("ABSTRACT LAYERS:"),
      if renderLayer.isEmpty then List("  ---") else renderLayer,
      if terminalLayer.isEmpty then List() else "  Terminal layer:" +: terminalLayer
    ).flatten

  lazy val reprPlanningPath: F[List[String]] =
    for
      paths <- structure.allThenPaths
      (directs, loops, nooses) = groupPaths(paths)
    yield List(
      List("PLANING PATHS:", "  Direct:"),
      renderPathRepr(directs).tab4,
      List("  Loop:"),
      renderPathRepr(loops).tab4,
      List("  Noose:"),
      renderPathRepr(nooses).tab4
    ).flatten

  lazy val reprNotConnectedNodes: List[String] = s"NOT CONNECTED NODES:" +: renderNotConnectedNodes.tab2

  lazy val repr: F[String] =
    for
      absLayers <- reprAbsLayers.map(_.tab2)
      planningPath <- reprPlanningPath.map(_.tab2)
      header = s"DcGraph(${nodes.size} nodes, ${edges.size} edges, ${samples.size} samples):"
    yield (header +: (absLayers ++ planningPath ++ reprNotConnectedNodes)).mkString("\n")
