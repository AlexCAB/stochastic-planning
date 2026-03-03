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
| created: 2026-03-03 |||||||||||*/

package planning.engine.planner.map.dcg.repr

import cats.MonadThrow
import planning.engine.common.graph.edges.EdgeKey
import planning.engine.common.graph.paths.Path

trait StructureReprBase[F[_]: MonadThrow]:
  import EdgeKey.Link
  import Path.{Direct, Loop, Noose}

  protected def buildLayerRepr(layer: Set[Link]): List[List[String]] = layer
    .groupBy(_.src)
    .toList.sortBy(_._1.value)
    .map((src, ls) => src.reprNode +: ls.toList.sortBy(_.trg.value).map(l => s"|${l.reprArrow}${l.trg.reprNode}"))

  protected def formatLayerRepr(layer: List[List[String]]): List[String] =
    val maxColSize = layer.map(_.size).max
    layer
      .map(column => column ++ List.fill(maxColSize - column.size)(""))
      .map: column =>
        val maxRecLen = column.map(_.length).max
        column.map(s => s + " " * (maxRecLen - s.length))
      .transpose
      .map(_.mkString(" "))

  protected def renderLayerRepr(layers: List[List[String]]): List[String] = layers
    .map(lines => lines.map(l => "      " + l).mkString("\n"))
    .zipWithIndex
    .map((l, i) => s"    Layer $i:\n" + l)

  protected def groupPaths(paths: Set[Path]): (List[Direct], List[Loop], List[Noose]) = paths
    .foldLeft((List[Direct](), List[Loop](), List[Noose]())):
      case ((ds, ls, ns), d: Direct) => (d +: ds, ls, ns)
      case ((ds, ls, ns), l: Loop)   => (ds, l +: ls, ns)
      case ((ds, ls, ns), n: Noose)  => (ds, ls, n +: ns)

  protected def renderPathRepr(paths: List[Path]): String =
    if paths.isEmpty then "      ---"
    else paths.sortBy(_.walk.length).reverse.map(p => s"      ${p.reprChain}").mkString("\n")
