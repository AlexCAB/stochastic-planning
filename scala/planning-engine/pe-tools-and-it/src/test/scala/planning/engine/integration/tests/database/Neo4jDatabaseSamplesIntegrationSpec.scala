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
import planning.engine.common.values.text.Name
import planning.engine.integration.tests.MapGraphIntegrationTestData.TestHiddenNodes
import planning.engine.map.samples.sample.{Sample, SampleEdge}
import planning.engine.map.subgraph.NextSampleEdge

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

  private def checkSampleNode(init: Init, sampleId: SampleId, name: Name)(implicit db: WithItDb.ItDb): IO[Assertion] =
    for
        sampleNode <- getSampleNode(sampleId).logValue("checkSampleNode", "sampleNode")
    yield
      sampleNode.getLongProperty(PROP.SAMPLE_ID) mustEqual sampleId.value
      sampleNode.getLongProperty(PROP.PROBABILITY_COUNT) mustEqual newSample.probabilityCount
      sampleNode.getDoubleProperty(PROP.UTILITY) mustEqual newSample.utility
      sampleNode.getStringProperty(PROP.NAME) mustEqual name.value
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
      sampleEdge <- SampleEdge.fromEdgeBySampleId[IO](sampleRawEdge, sampleId)
        .logValue("checkSampleEdge", "sampleEdge")
    yield
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
        checkSampleNode(init, sampleIds.head, newSample.name.get).await

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
        checkSampleNode(init, sampleIds.head, newSample.name.get).await

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
        checkSampleNode(init, sampleIds.head, newSample.name.get).await

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
        checkSampleNode(init, sampleIds.head, newSample.name.get).await

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
        val params = makeFourNewSamples(init.hnId1, init.hnId2, init.hnId3)

        val (sampleIds, edgeIds): (List[SampleId], List[String]) = neo4jdb
          .createSamples(params).logValue("multiple samples", "result").await

        sampleIds.size mustEqual 4
        edgeIds.size mustEqual 7

        checkIndexesRise(init, sampleId = 4, hnIndex1 = 4, hnIndex2 = 3, hnIndex3 = 2).await
        checkSampleNode(init, sampleIds.head, params.list.head.name.get).await

  "Neo4jDatabase.countSamples" should:
    "return total number of samples" in: (itDb, neo4jdb, _) =>
      given WithItDb.ItDb = itDb
      async[IO]:
        val nextSampleId = getNextSampleId.logValue("count samples", "nextSampleId").await
        val sampleCount = getSampleCount.logValue("count samples", "sampleCount").await
        val numOfSamples = neo4jdb.countSamples.logValue("count samples", "numOfSamples").await
        numOfSamples mustEqual (nextSampleId - 1L)
        numOfSamples mustEqual sampleCount

  "Neo4jDatabase.getNextSampleEdge(...)" should:
    "return next sample edges" in: (itDb, neo4jdb, nodes) =>
      given WithItDb.ItDb = itDb
      async[IO]:
        val List(hnId1, hnId2, hnId3) = nodes.allNodeIds.take(3)
        val params = makeFourNewSamples(hnId1, hnId2, hnId3)

        val (sampleIds, _): (List[SampleId], Any) = neo4jdb
          .createSamples(params).logValue("next edges", "created samples").await

        sampleIds.size mustEqual 4

        def runCall(hnId: HnId): IO[Map[SampleId, NextSampleEdge[IO]]] =
          for
            edges <- neo4jdb.getNextSampleEdge(hnId, nodes.getIoNode).logValue("next edges", s"for $hnId")
            edgesMap = edges.filter(e => sampleIds.contains(e.sampleData.id)).map(e => e.sampleData.id -> e).toMap
          yield edgesMap

        def getNewSampleData(edges: Map[SampleId, NextSampleEdge[IO]]): Map[SampleId, Sample.New] = edges
          .map((id, e) =>
            id -> params.list.find(_.name == e.sampleData.name)
              .getOrElse(fail(s"Sample not found for name: ${e.sampleData.name}"))
          ).toMap

        def checkEdgeData(
            sampleIds: List[SampleId],
            resForHnId: Map[SampleId, NextSampleEdge[IO]],
            currentHnId: HnId,
            nextHnIds: Set[HnId]
        ): Unit =
          val newSampleData = getNewSampleData(resForHnId)

          sampleIds.size mustEqual resForHnId.size
          sampleIds.size mustEqual newSampleData.size

          sampleIds.foreach: id =>
            val edgeData = resForHnId(id)
            val sampleData = newSampleData(id)
            val edges = sampleData.edges.filter(_.source == currentHnId)

            edges.size mustEqual 1
            edgeData.sampleData.id mustEqual id
            edgeData.sampleData.probabilityCount mustEqual sampleData.probabilityCount
            edgeData.sampleData.utility mustEqual sampleData.utility
            edgeData.sampleData.name mustEqual sampleData.name
            edgeData.sampleData.description mustEqual sampleData.description
            edgeData.nextHn.id mustEqual edges.head.target
            edgeData.edgeType mustEqual edges.head.edgeType

        def checkValues(
            source: Map[SampleId, NextSampleEdge[IO]],
            target: Map[SampleId, NextSampleEdge[IO]]
        ): Assertion =
          val ids = source.keySet & target.keySet
          ids.foreach: id =>
            source(id).nextValue mustEqual target(id).currentValue
          ids must not be empty

        val resForHnId1: Map[SampleId, NextSampleEdge[IO]] = runCall(hnId1).await

        resForHnId1.size mustEqual 4
        resForHnId1.map((_, e) => e.sampleData.name).toSet mustEqual params.list.map(_.name).toSet
        checkEdgeData(sampleIds, resForHnId1, hnId1, Set(hnId1, hnId2))

        val resForHnId2: Map[SampleId, NextSampleEdge[IO]] = runCall(hnId2).await

        resForHnId2.size mustEqual 2
        checkEdgeData(resForHnId2.keys.toList, resForHnId2, hnId2, Set(hnId3))
        checkValues(resForHnId1, resForHnId2)

        val resForHnId3: Map[SampleId, NextSampleEdge[IO]] = runCall(hnId3).await

        resForHnId3.size mustEqual 1
        checkEdgeData(resForHnId3.keys.toList, resForHnId3, hnId3, Set(hnId1))
        checkValues(resForHnId2, resForHnId3)
