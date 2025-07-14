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
import planning.engine.map.samples.sample.{Sample, SampleData, SampleEdge}
import cats.effect.cps.*
import neotypes.model.types.Node
import planning.engine.common.enums.EdgeType
import planning.engine.common.properties.PROP
import planning.engine.common.values.text.Name
import planning.engine.map.subgraph.NextSampleEdgeMap

class MapGraphSamplesIntegrationSpec extends IntegrationSpecWithResource[TestMapGraph]
    with WithItDb with TestItDbQuery with MapGraphIntegrationTestData:

  override val resource: Resource[IO, TestMapGraph] = logResource(
    for
      itDb <- makeDb()
      neo4jdb <- createRootNodesInDb(itDb.config, itDb.dbName)
      concreteNames = makeNames("concrete", 3)
      abstractNames = makeNames("abstract", 3)
      nodes <- initHiddenNodesInDb(neo4jdb, concreteNames, abstractNames)
      samplesHnIds = List(nodes.allNodeIds(1), nodes.allNodeIds(3), nodes.allNodeIds(5))
      namedSamples = makeFourNewSamples(samplesHnIds.head, samplesHnIds(1), samplesHnIds(2))
      noNameSamples = makeTwoNoNameNewSamples(samplesHnIds.head, samplesHnIds(1))
      samples <- initSampleInDb(neo4jdb, namedSamples.appendAll(noNameSamples))
      graph <- loadTestMapGraph(neo4jdb)
    yield TestMapGraph(itDb, neo4jdb, nodes, samples, graph)
  )

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
          newSample.copy(edges = List(SampleEdge.New(hnId1, hnId2, thenEdge))),
          newSample.copy(edges = List(SampleEdge.New(hnId1, hnId2, thenEdge), SampleEdge.New(hnId2, hnId3, linkEdge))),
          newSample.copy(edges =
            List(
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

  "MapGraph.nextSampleEdges" should:
    "return next sample edges" in: res =>
      async[IO]:
        val testHnId = res.samples.allHnIds.head
        val result: NextSampleEdgeMap[IO] = res.graph
          .nextSampleEdges(testHnId).logValue("next sample edges", "result").await

        val testSampleIds = res.samples.samples.values
          .filter(_.edges.exists(_.source.hnId == testHnId))
          .map(_.data.id)
          .toSet

        val gotSampleIds = result.sampleEdges
          .map(_.sampleData.id)
          .filter(id => testSampleIds.contains(id))
          .toSet

        result.currentNodeId mustEqual testHnId
        gotSampleIds mustEqual testSampleIds

  "MapGraph.getSampleNames" should:
    "return get sample names" in: res =>
      async[IO]:
        val result: Map[SampleId, Option[Name]] = res.graph
          .getSampleNames(res.samples.allSampleIds.toList).logValue("get sample names", "result").await

        result mustEqual res.samples.samples.view.mapValues(_.data.name).toMap

  "MapGraph.getSamplesData" should:
    "return get sample data" in: res =>
      async[IO]:
        val result: Map[SampleId, SampleData] = res.graph
          .getSamplesData(res.samples.allSampleIds.toList).logValue("get sample data", "result").await

        result mustEqual res.samples.samples.view.mapValues(_.data).toMap

  "MapGraph.getSamples" should:
    "return get samples" in: res =>
      async[IO]:
        val result: Map[SampleId, Sample] = res.graph
          .getSamples(res.samples.allSampleIds.toList).logValue("get samples", "result").await

        result mustEqual res.samples.samples
