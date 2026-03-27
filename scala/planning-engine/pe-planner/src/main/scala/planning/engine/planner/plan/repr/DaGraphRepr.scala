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
| created: 2026-03-13 |||||||||||*/

package planning.engine.planner.plan.repr

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.graph.edges.PeKey.Link
import planning.engine.common.repr.StructureReprBase
import planning.engine.common.values.io.IoTime
import planning.engine.common.values.node.PnId
import planning.engine.planner.plan.dag.DaGraph

trait DaGraphRepr[F[_]: MonadThrow] extends StructureReprBase[F]:
  self: DaGraph[F] =>

  private[repr] def srcNode(id: PnId): String = nodes.get(id).map(_.repr).getOrElse(id.repr)

  // Builds the string representation of a layer of the DAG, in next format:
  // List of lists of columns sorted by time[
  //   List of columns sorted by source node ID[
  //     List of colum rows where first element is source node and the rest are target nodes sorted by ID]]
  private[repr] def buildLayerRepr(layer: Set[Link]): List[(IoTime, Columns)] = layer
    .groupBy(_.src).toList
    .map((src, rows) => src -> rows.toList.sortBy(r => (r.trg.time.value, r.trg.mnId.value)))
    .map((src, rows) => src -> (srcNode(src) +: rows.map(r => s"|==>${r.trg.repr}")))
    .groupBy(_._1.time).toList.sortBy(_._1.value).map((t, cols) => t -> cols.map(_._2))

  // Builds the string representation of terminal layer (abstract DAG leaf nodes), in next format:
  // List of lists of columns sorted by time[
  //   List of columns sorted by source node ID[
  //     String leaf nodes representation]]
  private[repr] def builtTerminalLayer(layers: List[Set[Link]]): List[(IoTime, Columns)] =
    val links = layers.toSet.flatten
    val termNodes = links.map(_.trg) -- links.map(_.src)

    termNodes
      .groupBy(_.time).toList.sortBy(_._1.value)
      .map((t, cols) => t -> cols.toList.sortBy(_.mnId.value).map(id => List(srcNode(id))))

  private[repr] def formatLayers(layer: List[(IoTime, Columns)], addTime: Boolean): List[Rows] =
    val height = layer.flatMap(_._2.map(_.size)).maxOption.getOrElse(0)
    val width = layer.flatMap(_._2.flatMap(_.map(_.length))).maxOption.getOrElse(0)

    val formattedRows: List[(IoTime, Rows)] = layer
      .map((t, cols) => t -> cols.map(col => formatColumn(col, height, width)).transpose.map(_.mkString(" " * 2)))

    formattedRows.map: (t, rows) =>
      val timeStr = if addTime then ":t" + t.value.toString else ""
      val splitBar = (timeStr + ":" * (rows.size - timeStr.length)).take(rows.size)
      rows.zip(splitBar).map((row, s) => s.toString + " " + row)

  private[repr] def formatTimes(layers: Layers): Rows =
    def countColWidth(ls: List[Columns]): List[Int] =
      val cw = ls
        .map(cols => cols.headOption.toList.flatMap(_.map(_.length)).headOption.maxOption.getOrElse(0))
        .maxOption.getOrElse(0)

      ls.map(_.tail) match
        case l if l.forall(_.isEmpty) => List(cw)
        case l                        => cw +: countColWidth(l)

    val colWidths = countColWidth(layers)

    layers
      .map(_.zip(colWidths).map((col, w) => col.map(row => row + " " * (w - row.length))))
      .map(_.transpose.map(_.mkString(" ")))
      .zipWithIndex.flatMap((rows, i) => s"Layer $i:" +: rows.tab2)

  lazy val reprAbsLayers: F[List[String]] =
    for
      layers <- traceAbsDagLayers(self.conPnId)
      layerRows = layers.map(buildLayerRepr).zipWithIndex.map((l, i) => formatLayers(l, i == 0))
      terminalRow = formatLayers(builtTerminalLayer(layers), addTime = false)
      allRows = formatTimes(layerRows :+ terminalRow).tab2
    yield "ABSTRACT LAYERS:" +: (if allRows.isEmpty then List("  ---") else allRows)














  lazy val repr: F[String] =
    for
      absLayers <- reprAbsLayers.map(_.tab2)
      // planningPath <- reprPlanningPath.map(_.tab2)
      header = s"DaGraph(${nodes.size} nodes, ${edges.size} edges):"
    yield (header +: absLayers /*++ planningPath ++ reprNotConnectedNodes */ ).mkString("\n")
