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
| created: 2025-07-04 |||||||||||*/

package planning.engine.integration.tests.database

import cats.effect.{IO, Resource}
import planning.engine.common.properties.*
import planning.engine.common.values.node.{HnId, HnIndex}
import planning.engine.integration.tests.*
import planning.engine.map.database.Neo4jDatabase
import cats.effect.cps.*
import org.scalatest.Assertion
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.sample.SampleId
import planning.engine.integration.tests.MapGraphIntegrationTestData.TestHiddenNodes
import planning.engine.map.samples.sample.{Sample, SampleEdge}

class Neo4jDatabaseSamplesIntegrationSpec
    extends IntegrationSpecWithResource[(WithItDb.ItDb, Neo4jDatabase[IO], TestHiddenNodes)]
    with WithItDb with TestItDbQuery with MapGraphIntegrationTestData:

  override val removeDbAfterTest: Boolean = true

  override val resource: Resource[IO, (WithItDb.ItDb, Neo4jDatabase[IO], TestHiddenNodes)] =
    for
      itDb <- makeDb()
      neo4jdb <- createRootNodesInDb(itDb.config, itDb.dbName)
      nodes <- initHiddenNodesInDb(neo4jdb, makeNames("concrete", 2), makeNames("abstract", 2))
    yield (itDb, neo4jdb, nodes)

  private class Init(nodes: TestHiddenNodes)(implicit db: WithItDb.ItDb):
    val hnId1 = nodes.allNodeIds(1)
    val hnId2 = nodes.allNodeIds(3)
    val hnId3 = nodes.allNodeIds(5)
    val thenEdge = EdgeType.THEN
    val linkEdge = EdgeType.LINK
    val nextSampleId: SampleId = SampleId(getNextSampleId.logValue("Init", "nextSampleId").unsafeRunSync())
    val nextHnIndex1: HnIndex = HnIndex(getNextHnIndex(hnId1).logValue("Init", "nextHnIndex1").unsafeRunSync())
    val nextHnIndex2: HnIndex = HnIndex(getNextHnIndex(hnId2).logValue("Init", "nextHnIndex2").unsafeRunSync())
    val nextHnIndex3: HnIndex = HnIndex(getNextHnIndex(hnId3).logValue("Init", "nextHnIndex3").unsafeRunSync())

  private def checkIndexesRise(init: Init, sampleId: Int, hnIndex1: Int, hnIndex2: Int, hnIndex3: Int)(implicit
      db: WithItDb.ItDb
  ): IO[Assertion] =
    for
      nextSampleId <- getNextSampleId.logValue("checkIndexesRise", "nextSampleId")
      nextHnIndex1 <- getNextHnIndex(init.hnId1).logValue("checkIndexesRise", "nextHnIndex1")
      nextHnIndex2 <- getNextHnIndex(init.hnId2).logValue("checkIndexesRise", "nextHnIndex2")
      nextHnIndex3 <- getNextHnIndex(init.hnId3).logValue("checkIndexesRise", "nextHnIndex3")
    yield
      (init.nextSampleId.value + sampleId) mustEqual nextSampleId
      (init.nextHnIndex1.value + hnIndex1) mustEqual nextHnIndex1
      (init.nextHnIndex2.value + hnIndex2) mustEqual nextHnIndex2
      (init.nextHnIndex3.value + hnIndex3) mustEqual nextHnIndex3

  private def checkSampleNode(init: Init, sampleId: SampleId)(implicit db: WithItDb.ItDb): IO[Assertion] =
    for
        sampleNode <- getSampleNode(sampleId).logValue("checkSampleNode", "sampleNode")
    yield
      sampleNode.getLongProperty(PROP.SAMPLE_ID) mustEqual sampleId.value
      sampleNode.getLongProperty(PROP.PROBABILITY_COUNT) mustEqual newSample.probabilityCount
      sampleNode.getDoubleProperty(PROP.UTILITY) mustEqual newSample.utility
      sampleNode.getStringProperty(PROP.NAME) mustEqual newSample.name.get.value
      sampleNode.getStringProperty(PROP.DESCRIPTION) mustEqual newSample.description.get.value

  private def checkSampleEdge(
      sampleId: SampleId,
      edgeId: String,
      sourceHn: HnId,
      targetHn: HnId,
      edgeType: EdgeType,
      sourceValue: HnIndex,
      targetValue: HnIndex
  )(implicit db: WithItDb.ItDb): IO[Assertion] =
    for
      sampleRawEdge <- getEdge(sourceHn, targetHn, edgeType, edgeId)
        .logValue("checkSampleEdge", "sampleRawEdge")
      sampleEdge <- SampleEdge.fromEdgeBySampleId[IO](sampleRawEdge, sampleId, sourceHn, targetHn)
        .logValue("checkSampleEdge", "sampleEdge")
    yield
      sampleEdge.sourceHn mustEqual sourceHn
      sampleEdge.targetHn mustEqual targetHn
      sampleEdge.sourceValue mustEqual sourceValue
      sampleEdge.targetValue mustEqual targetValue
      sampleEdge.edgeType mustEqual edgeType
      sampleEdge.sampleId mustEqual sampleId

  "Neo4jDatabase.createSamples(...)" should:
    "create loop edge sample" in: (itDb, neo4jdb, nodes) =>
      given WithItDb.ItDb = itDb
      val init = new Init(nodes)
      async[IO]:
        val params = Sample.ListNew.of(
          newSample.copy(edges = Set(SampleEdge.New(init.hnId1, init.hnId1, init.thenEdge)))
        )

        val (sampleIds, edgeIds): (List[SampleId], List[String]) = neo4jdb
          .createSamples(params).logValue("loop edge", "result").await

        sampleIds.size mustEqual 1
        edgeIds.size mustEqual 1

        checkIndexesRise(init, sampleId = 1, hnIndex1 = 1, hnIndex2 = 0, hnIndex3 = 0).await
        checkSampleNode(init, sampleIds.head).await

        checkSampleEdge(
          sampleId = sampleIds.head,
          edgeId = edgeIds.head,
          sourceHn = init.hnId1,
          targetHn = init.hnId1,
          edgeType = init.thenEdge,
          sourceValue = init.nextHnIndex1,
          targetValue = init.nextHnIndex1
        ).await

    "create one edge sample" in: (itDb, neo4jdb, nodes) =>
      given WithItDb.ItDb = itDb
      val init = new Init(nodes)
      async[IO]:
        val params = Sample.ListNew.of(
          newSample.copy(edges = Set(SampleEdge.New(init.hnId1, init.hnId2, init.thenEdge)))
        )

        val (sampleIds, edgeIds): (List[SampleId], List[String]) = neo4jdb
          .createSamples(params).logValue("one edge", "result").await

        sampleIds.size mustEqual 1
        edgeIds.size mustEqual 1

        checkIndexesRise(init, sampleId = 1, hnIndex1 = 1, hnIndex2 = 1, hnIndex3 = 0).await
        checkSampleNode(init, sampleIds.head).await

        checkSampleEdge(
          sampleId = sampleIds.head,
          edgeId = edgeIds.head,
          sourceHn = init.hnId1,
          targetHn = init.hnId2,
          edgeType = init.thenEdge,
          sourceValue = init.nextHnIndex1,
          targetValue = init.nextHnIndex2
        ).await

    "create two edge sample" in: (itDb, neo4jdb, nodes) =>
      given WithItDb.ItDb = itDb
      val init = new Init(nodes)
      async[IO]:
        val params = Sample.ListNew.of(newSample.copy(edges =
          Set(
            SampleEdge.New(init.hnId1, init.hnId2, init.thenEdge),
            SampleEdge.New(init.hnId2, init.hnId3, init.linkEdge)
          )
        ))

        val (sampleIds, edgeIds): (List[SampleId], List[String]) = neo4jdb
          .createSamples(params).logValue("two edge", "result").await

        sampleIds.size mustEqual 1
        edgeIds.size mustEqual 2

        checkIndexesRise(init, sampleId = 1, hnIndex1 = 1, hnIndex2 = 1, hnIndex3 = 1).await
        checkSampleNode(init, sampleIds.head).await

        checkSampleEdge(
          sampleId = sampleIds.head,
          edgeId = edgeIds.head,
          sourceHn = init.hnId1,
          targetHn = init.hnId2,
          edgeType = init.thenEdge,
          sourceValue = init.nextHnIndex1,
          targetValue = init.nextHnIndex2
        ).await

        checkSampleEdge(
          sampleId = sampleIds.head,
          edgeId = edgeIds(1),
          sourceHn = init.hnId2,
          targetHn = init.hnId3,
          edgeType = init.linkEdge,
          sourceValue = init.nextHnIndex2,
          targetValue = init.nextHnIndex3
        ).await

    "create three edge sample" in: (itDb, neo4jdb, nodes) =>
      given WithItDb.ItDb = itDb
      val init = new Init(nodes)
      async[IO]:
        val params = Sample.ListNew.of(newSample.copy(edges =
          Set(
            SampleEdge.New(init.hnId1, init.hnId2, init.thenEdge),
            SampleEdge.New(init.hnId2, init.hnId3, init.linkEdge),
            SampleEdge.New(init.hnId3, init.hnId1, init.thenEdge)
          )
        ))

        val (sampleIds, edgeIds): (List[SampleId], List[String]) = neo4jdb
          .createSamples(params).logValue("two edge", "result").await

        sampleIds.size mustEqual 1
        edgeIds.size mustEqual 3

        checkIndexesRise(init, sampleId = 1, hnIndex1 = 1, hnIndex2 = 1, hnIndex3 = 1).await
        checkSampleNode(init, sampleIds.head).await

        checkSampleEdge(
          sampleId = sampleIds.head,
          edgeId = edgeIds.head,
          sourceHn = init.hnId1,
          targetHn = init.hnId2,
          edgeType = init.thenEdge,
          sourceValue = init.nextHnIndex1,
          targetValue = init.nextHnIndex2
        ).await

        checkSampleEdge(
          sampleId = sampleIds.head,
          edgeId = edgeIds(1),
          sourceHn = init.hnId2,
          targetHn = init.hnId3,
          edgeType = init.linkEdge,
          sourceValue = init.nextHnIndex2,
          targetValue = init.nextHnIndex3
        ).await

        checkSampleEdge(
          sampleId = sampleIds.head,
          edgeId = edgeIds(2),
          sourceHn = init.hnId3,
          targetHn = init.hnId1,
          edgeType = init.thenEdge,
          sourceValue = init.nextHnIndex3,
          targetValue = init.nextHnIndex1
        ).await

    "create multiple samples" in: (itDb, neo4jdb, nodes) =>
      given WithItDb.ItDb = itDb
      val init = new Init(nodes)
      async[IO]:
        val params = Sample.ListNew.of(
          newSample.copy(edges = Set(SampleEdge.New(init.hnId1, init.hnId2, init.thenEdge))),
          newSample.copy(edges =
            Set(
              SampleEdge.New(init.hnId1, init.hnId2, init.thenEdge),
              SampleEdge.New(init.hnId2, init.hnId3, init.linkEdge)
            )
          ),
          newSample.copy(edges =
            Set(
              SampleEdge.New(init.hnId1, init.hnId2, init.thenEdge),
              SampleEdge.New(init.hnId2, init.hnId3, init.linkEdge),
              SampleEdge.New(init.hnId3, init.hnId1, init.thenEdge)
            )
          )
        )

        val (sampleIds, edgeIds): (List[SampleId], List[String]) = neo4jdb
          .createSamples(params).logValue("multiple samples", "result").await

        sampleIds.size mustEqual 3
        edgeIds.size mustEqual 6

        checkIndexesRise(init, sampleId = 3, hnIndex1 = 3, hnIndex2 = 3, hnIndex3 = 2).await
        checkSampleNode(init, sampleIds.head).await

  "Neo4jDatabase.countSamples" should :
    "return total number of samples" in: (itDb, neo4jdb, _) =>
      given WithItDb.ItDb = itDb
  
      async[IO]:
        val nextSampleId = getNextSampleId.logValue("count samples", "nextSampleId").await
        val sampleCount = getSampleCount.logValue("count samples", "sampleCount").await
        val numOfSamples = neo4jdb.countSamples.logValue("count samples", "numOfSamples").await
        numOfSamples mustEqual (nextSampleId - 1L)
        numOfSamples mustEqual sampleCount
  