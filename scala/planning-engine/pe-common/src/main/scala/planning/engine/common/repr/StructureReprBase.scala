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

package planning.engine.common.repr

import cats.MonadThrow
import planning.engine.common.graph.paths.Path

trait StructureReprBase[F[_]: MonadThrow]:
  import Path.{Direct, Loop, Noose}

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
    .map(_.tab2).zipWithIndex.flatMap((ls, i) => s"Layer $i:" +: ls)

  protected def groupPaths(paths: Set[Path]): (List[Direct], List[Loop], List[Noose]) = paths
    .foldLeft((List[Direct](), List[Loop](), List[Noose]())):
      case ((ds, ls, ns), d: Direct) => (d +: ds, ls, ns)
      case ((ds, ls, ns), l: Loop)   => (ds, l +: ls, ns)
      case ((ds, ls, ns), n: Noose)  => (ds, ls, n +: ns)

  protected def renderPathRepr(paths: List[Path]): List[String] =
    if paths.isEmpty then List("---")
    else paths.sortBy(_.walk.length).reverse.map(_.reprChain)

  extension (list: List[String])
    def tab2: List[String] = list.map("  " + _)
    def tab4: List[String] = list.map("    " + _)
    def tab6: List[String] = list.map("      " + _)
