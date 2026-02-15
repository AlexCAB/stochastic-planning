///*|||||||||||||||||||||||||||||||||
//|| 0 * * * * * * * * * ▲ * * * * ||
//|| * ||||||||||| * ||||||||||| * ||
//|| * ||  * * * * * ||       || 0 ||
//|| * ||||||||||| * ||||||||||| * ||
//|| * * ▲ * * 0|| * ||   (< * * * ||
//|| * ||||||||||| * ||  ||||||||||||
//|| * * * * * * * * *   ||||||||||||
//| author: CAB |||||||||||||||||||||
//| website: github.com/alexcab |||||
//| created: 2026-02-10 |||||||||||*/
//
//
//
//package planning.engine.planner.map.dcg.repr
//
//import planning.engine.common.values.edges.Key
//import planning.engine.common.values.node.HnId
//import planning.engine.planner.map.dcg.nodes.DcgNode
//
//class DcgGraphRepr {
//
//}
//
//private[map] def sortedForwardEdges(srtIds: Set[HnId]): Map[HnId, List[Key]] = edgesMapping
//  .findForward(srtIds).groupBy(_.src)
//  .view.mapValues(_.filter(e => edgesData.get(e).forall(_.isLink)).toList.sortBy(_.trg.value))
//  .toMap
//
//private[map] def nodeRendered(nodes: List[DcgNode[F]], edgeMap: Map[HnId, List[Key]]): List[String] =
//  val cols = nodes.sortBy(_.id.value).map: node =>
//    val edges = edgeMap.getOrElse(node.id, List()).flatMap(e => edgesData.get(e))
//    val edgesRepr = edges.map(_.reprTarget)
//    val colRepr = node.repr +: edgesRepr
//    val rowLength = colRepr.map(_.length).max
//    rowLength -> colRepr.map(r => r + " " * (rowLength - r.length))
//
//  val colHeight = cols.map(_._2.length).max
//
//  cols
//    .map((rowLength, col) => col ++ List.fill(colHeight - col.length)(" " * rowLength))
//    .transpose
//    .map(_.mkString(" "))
//
//private[map] def renderedAbstract(nextIds: Set[HnId], i: Int): List[String] =
//  ??? // assert(i <= 100, "Max recursion depth exceeded in renderedAbstract")
//  val levelTitle = List("-" * 20, s"Abstract Level $i:")
//
//  sortedForwardEdges(nextIds) match
//    case edges if edges.isEmpty => List()
//    case edges => levelTitle ++ nodeRendered(
//      absNodes.filter((id, _) => nextIds.contains(id)).values.toList,
//      edges
//    ) ++ renderedAbstract(edges.flatMap((_, e) => e.map(_.trg)).toSet, i + 1)
//
//lazy val repr: String =
//  val conEdges = sortedForwardEdges(conNodes.keySet)
//
//  val rendered = nodeRendered(
//    conNodes.values.toList,
//    conEdges
//  ) ++ renderedAbstract(conEdges.flatMap((_, e) => e.map(_.trg)).toSet, i = 1)
//
//  "\nConcrete Level 0:\n" + rendered.mkString("\n")