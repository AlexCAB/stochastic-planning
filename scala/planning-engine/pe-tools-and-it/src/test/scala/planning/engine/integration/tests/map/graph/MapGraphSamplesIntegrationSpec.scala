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
| created: 2025-07-05 |||||||||||*/

package planning.engine.integration.tests.map.graph

import cats.effect.{IO, Resource}
import planning.engine.common.values.sample.SampleId
import planning.engine.integration.tests.MapGraphIntegrationTestData.TestMapGraph
import planning.engine.integration.tests.*
import planning.engine.map.samples.sample.{Sample, SampleEdge}
import cats.effect.cps.*
import neotypes.model.types.Node
import planning.engine.common.enums.EdgeType
import planning.engine.common.properties.PROP

class MapGraphSamplesIntegrationSpec extends IntegrationSpecWithResource[TestMapGraph]
    with WithItDb with TestItDbQuery with MapGraphIntegrationTestData:

  override val resource: Resource[IO, TestMapGraph] =
    for
      itDb <- makeDb()
      neo4jdb <- createRootNodesInDb(itDb.config, itDb.dbName)
      concreteNames = makeNames("concrete", 3)
      abstractNames = makeNames("abstract", 3)
      nodes <- initHiddenNodesInDb(neo4jdb, concreteNames, abstractNames)
      graph <- loadTestMapGraph(neo4jdb)
    yield TestMapGraph(itDb, neo4jdb, nodes, graph)

  "MapGraph.createSamples(...)" should:
    "create multiple samples" in: data =>
      given WithItDb.ItDb = data.itDb
      async[IO]:
        val hnId1 = data.nodes.allNodeIds(1)
        val hnId2 = data.nodes.allNodeIds(3)
        val hnId3 = data.nodes.allNodeIds(5)
        val thenEdge = EdgeType.THEN
        val linkEdge = EdgeType.LINK
        val nextSampleId: Long = getNextSampleId.logValue("multiple samples", "nextSampleId").await
        val nextHnIndex1: Long = getNextHnIndex(hnId1).logValue("multiple samples", "nextHnIndex1").await
        val nextHnIndex2: Long = getNextHnIndex(hnId2).logValue("multiple samples", "nextHnIndex2").await
        val nextHnIndex3: Long = getNextHnIndex(hnId3).logValue("multiple samples", "nextHnIndex3").await

        val params = Sample.ListNew.of(
          newSample.copy(edges = Set(SampleEdge.New(hnId1, hnId2, thenEdge))),
          newSample.copy(edges = Set(SampleEdge.New(hnId1, hnId2, thenEdge), SampleEdge.New(hnId2, hnId3, linkEdge))),
          newSample.copy(edges =
            Set(
              SampleEdge.New(hnId1, hnId2, thenEdge),
              SampleEdge.New(hnId2, hnId3, linkEdge),
              SampleEdge.New(hnId3, hnId1, thenEdge)
            )
          )
        )

        val sampleIds: List[SampleId] = data.graph.addNewSamples(params).logValue("multiple samples", "result").await

        sampleIds.size mustEqual 3

        val sampleRaw1: Node = getSampleNode(sampleIds.head).logValue("multiple samples", "sampleRaw1").await

        sampleRaw1.getLongProperty(PROP.SAMPLE_ID) mustEqual sampleIds.head.value
        sampleRaw1.getLongProperty(PROP.PROBABILITY_COUNT) mustEqual newSample.probabilityCount
        sampleRaw1.getDoubleProperty(PROP.UTILITY) mustEqual newSample.utility
        sampleRaw1.getStringProperty(PROP.NAME) mustEqual newSample.name.get.value
        sampleRaw1.getStringProperty(PROP.DESCRIPTION) mustEqual newSample.description.get.value

        (nextSampleId + 3L) mustEqual getNextSampleId.await
        (nextHnIndex1 + 3L) mustEqual getNextHnIndex(hnId1).await
        (nextHnIndex2 + 3L) mustEqual getNextHnIndex(hnId2).await
        (nextHnIndex3 + 2L) mustEqual getNextHnIndex(hnId3).await

  "MapGraph.countSamples" should:
    "return total number of samples" in: res =>
      given WithItDb.ItDb = res.itDb

      async[IO]:
        val gotCount: Long = res.graph.countSamples.logValue("count samples", "gotCount").await
        val testCount = getSampleCount.logValue("count samples", "testCount").await

        gotCount mustEqual testCount
