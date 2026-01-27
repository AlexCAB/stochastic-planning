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
| created: 2025-12-22 |||||||||||*/

package planning.engine.planner.map.test.data

import cats.effect.IO
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.io.IoIndex
import planning.engine.common.values.node.{HnId, HnIndex, HnName}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.Description
import planning.engine.planner.map.dcg.DcgGraph
import planning.engine.common.values.edges.EndIds
import planning.engine.planner.map.dcg.edges.{DcgEdgeData, DcgEdgeSamples}
import planning.engine.planner.map.dcg.edges.DcgEdgeSamples.Indexies
import planning.engine.planner.map.test.data.MapNodeTestData
import planning.engine.planner.map.dcg.nodes.DcgNode

trait MapDcgTestData extends MapNodeTestData:
  def makeConcreteDcgNode(
      id: HnId = HnId(3000005),
      valueIndex: IoIndex = IoIndex(102)
  ): DcgNode.Concrete[IO] = DcgNode.Concrete[IO](
    id = id,
    name = HnName.some(s"Con DCG Node $id"),
    description = Description.some(s"A concrete DCG node for testing, $id"),
    ioNode = testBoolInNode,
    valueIndex = valueIndex
  )

  def makeAbstractDcgNode(id: HnId = HnId(3000006)): DcgNode.Abstract[IO] = DcgNode.Abstract[IO](
    id = id,
    name = HnName.some(s"Abs DCG Node $id"),
    description = Description.some(s"An abstract DCG node for testing, $id")
  )

  def makeDcgEdgeData(
      srcId: HnId,
      trgId: HnId,
      links: Map[SampleId, Indexies] = Map(),
      thens: Map[SampleId, Indexies] = Map()
  ): DcgEdgeData = DcgEdgeData(
    ends = EndIds(srcId, trgId),
    links = DcgEdgeSamples.Links(links),
    thens = DcgEdgeSamples.Thens(thens)
  )

  def makeDcgEdgeData(ends: EndIds): DcgEdgeData = makeDcgEdgeData(ends.src, ends.trg)

  extension (graph: DcgGraph[IO])
    def addSample(sampleId: SampleId, edges: Set[(EdgeType, EndIds)]): DcgGraph[IO] =
      val ends = edges.map(_._2)
      val allHdIds = ends.flatMap(e => Set(e.src, e.trg))
      val edgesDef = ends -- graph.edgesData.keySet
      val hdIdsDef = allHdIds -- graph.allHnIds

      assert(edgesDef.isEmpty, s"For sample $sampleId to non-existing edges: $edgesDef")
      assert(hdIdsDef.isEmpty, s"For sample $sampleId to edges with non-existing HnIds: $hdIdsDef")

      val newIndexies = graph.edgesData.values.toList
        .flatMap(e => List(e.ends.src -> e.srcHnIndex, e.ends.trg -> e.trgHnIndex))
        .filter((hdId, _) => allHdIds.contains(hdId))
        .groupBy(_._1)
        .map((hdId, lst) => hdId -> lst.flatMap(_._2).map(_.value).maxOption)
        .map((hdId, opMax) => hdId -> opMax.map(i => HnIndex(i + 1)).getOrElse(HnIndex.init))

//      val edgesDataMap = edges
//        .map((et, ends) => et -> (graph.edgesData.get(ends), newIndexies.get(ends.src), newIndexies.get(ends.trg)))
//        .map:
//          case (LINK, (Some(data), Some(srcInd), Some(trgInd))) => data.copy(
//              links = DcgEdgeSamples.Links(
//                data.links.indexies + (sampleId -> Indexies(srcInd, trgInd))
//              )
//            )

      ???
