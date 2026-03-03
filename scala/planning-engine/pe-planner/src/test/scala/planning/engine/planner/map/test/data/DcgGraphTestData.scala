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
import cats.effect.unsafe.IORuntime
import planning.engine.common.values.node.MnId
import planning.engine.common.values.sample.SampleId
import planning.engine.common.graph.GraphStructure
import planning.engine.map.samples.sample.SampleData
import planning.engine.planner.map.dcg.DcgGraph
import planning.engine.planner.map.dcg.edges.DcgEdge
import planning.engine.planner.map.dcg.samples.DcgSample

trait DcgGraphTestData extends DcgNodeTestData with DcgEdgeTestData with DcgSampleTestData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  def makeEdgLink(srcId: MnId, trgId: MnId, ids: Set[SampleId]): DcgEdge[IO] =
    makeDcgEdgeLink(srcId, trgId, makeIndexiesForSampleIds(allMnId, ids.toSeq*))

  def makeEdgThen(srcId: MnId, trgId: MnId, ids: Set[SampleId]): DcgEdge[IO] =
    makeDcgEdgeThen(srcId, trgId, makeIndexiesForSampleIds(allMnId, ids.toSeq*))

  lazy val nuHnId = MnId.Abs(-1)
  lazy val emptyDcgGraph = DcgGraph.empty[IO]
  lazy val allNodes = conNodes ++ absNodes

  lazy val graphWithNodes = emptyDcgGraph
    .copy(nodes = allNodes.map(n => n.id -> n).toMap)

  lazy val lEdge1 = makeEdgLink(mnId1, mnId3, Set(sampleId1, sampleId2))
  lazy val lEdge2 = makeEdgLink(mnId2, mnId4, Set(sampleId1))
  lazy val lEdge3 = makeEdgLink(mnId4, mnId5, Set(sampleId2))
  lazy val lEdge4 = makeEdgLink(mnId2, mnId5, Set(sampleId2))

  lazy val tEdge1 = makeEdgThen(mnId2, mnId1, Set(sampleId2))
  lazy val tEdge2 = makeEdgThen(mnId5, mnId1, Set(sampleId1, sampleId2))

  lazy val dcgEdges: List[DcgEdge[IO]] = List(lEdge1, lEdge2, lEdge3, lEdge4, tEdge1, tEdge2)
  lazy val sampleData: List[SampleData] = List(sampleId1, sampleId2).map(id => makeDcgSampleData(id = id))

  lazy val graphWithEdges: DcgGraph[IO] = graphWithNodes.copy(
    edges = dcgEdges.map(e => e.key -> e).toMap,
    samples = sampleData.map(s => s.id -> s).toMap,
    structure = GraphStructure[IO](dcgEdges.map(_.key).toSet)
  )

  extension (graph: DcgGraph[IO])
    def makeAndAddNodesFromIds(mnIds: Set[MnId]): DcgGraph[IO] = graph
      .addNodes(
        mnIds.map:
          case id: MnId.Con => makeConDcgNode(id)
          case id: MnId.Abs => makeAbsDcgNode(id)
      ).unsafeRunSync()

    def addTestDcgSample(sample: DcgSample[IO]): DcgGraph[IO] = graph
      .addSamples(List(DcgSample.Add[IO](sample, makeDcgIndexMap(sample.data.id, sample.structure.mnIds))))
      .unsafeRunSync()
