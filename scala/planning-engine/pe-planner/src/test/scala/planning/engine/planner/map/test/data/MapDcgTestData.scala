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
import planning.engine.common.values.node.{AbsId, HnId, HnIndex, HnName}
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
      id: ConId = ConId(3000005),
      valueIndex: IoIndex = IoIndex(102)
  ): DcgNode.Concrete[IO] = DcgNode.Concrete[IO](
    id = id,
    name = HnName.some(s"Con DCG Node $id"),
    description = Description.some(s"A concrete DCG node for testing, $id"),
    ioNode = testBoolInNode,
    valueIndex = valueIndex
  )

  def makeAbstractDcgNode(id: AbsId = AbsId(3000006)): DcgNode.Abstract[IO] = DcgNode.Abstract[IO](
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
