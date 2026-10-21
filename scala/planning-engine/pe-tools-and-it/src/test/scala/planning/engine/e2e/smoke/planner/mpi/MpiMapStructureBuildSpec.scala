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
| created: 2026-10-21 |||||||||||*/

package planning.engine.e2e.smoke.planner.mpi

import cats.effect.cps.*
import cats.effect.{IO, Resource}
import cats.syntax.all.*
import cats.syntax.ext.MT
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.{timeout, verify}
import org.mockito.scalatest.AsyncIdiomaticMockito
import planning.engine.common.UnitSpecWithResource
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.e2e.smoke.planner.mpi.data.ManSamplesData
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.planner.mpi.model.io.IoVars
import planning.engine.planner.mpi.{MapMpi, Visualization}

class MpiMapStructureBuildSpec
    extends UnitSpecWithResource[(MapMpi[IO], Visualization)] with AsyncIdiomaticMockito with ManSamplesData:

  private val callTimeoutMs = 3000L // Visualization is called asynchronously, so verification waits for it.
  private val noCallWaitMs = 500L // Time to wait to make sure visualization is not called.

  override val resource: Resource[IO, (MapMpi[IO], Visualization)] =
    for
      visualization <- Resource.eval(IO.delay(makeVisualizationMock()))
      mapMpi <- MapMpi[IO](Some(visualization))
    yield (mapMpi, visualization)

  // All stubbing must be done before the map initialized: the visualization is called from the visualizer
  // actor thread, which races with Mockito stubbing done from the test thread.
  // `activeStepsFound` is not stubbed: it is called only on planner tick, which is not part of this scenario
  // (also `*` can't be used for value class parameter `IoTime`, it leads to NPE).
  private def makeVisualizationMock(): Visualization =
    val visualization = mock[Visualization]
    visualization.init[IO](*, *) returns IO.unit
    visualization.nodesAdded[IO](*) returns IO.unit
    visualization.edgesAdded[IO](*) returns IO.unit
    visualization

  // Wait till `Visualization.nodesAdded` called `times` in total and return the nodes of the last call.
  private def lastNodesAdded(visualization: Visualization, times: Int): Map[MnId, NodeData] =
    val captor = ArgumentCaptor.forClass[Map[MnId, NodeData], Map[MnId, NodeData]](classOf[Map[MnId, NodeData]])
    verify(visualization, timeout(callTimeoutMs).times(times)).nodesAdded[IO](captor.capture())(using any[MT[IO]])
    captor.getValue

  private def namesOf(nodes: Map[MnId, NodeData]): Map[HnName, MnId] =
    nodes.collect { case (id, data) if data.name.isDefined => data.name.get -> id }

  // Replace sample Nim IDs with IDs of the map nodes, looked up by node names.
  private def resolveKeys(sample: ManSample, names: Map[HnName, MnId]): IO[Set[MeKey]] =
    def findId(data: NodeData): IO[MnId] =
      IO.fromOption(data.name.flatMap(names.get))(new AssertionError(s"Node $data not found in $names"))

    for
      idsMap <- sample.nodes.toList.traverse((nim, data) => findId(data).map(nim -> _)).map(_.toMap)
      keys <- sample.samples.toList.flatMap(_.edges).traverse(_.resolve[IO](idsMap))
    yield keys.toSet

  "MapMpi" should:
    "build map structure step by step and notify visualization about each change" in: (mapMpi, visualization) =>
      async[IO]:
        // Step 1: init map network
        mapMpi.init(testMetadata, inVars, outVars).logValue("init").await
        val expectedVars = IoVars[IO](inVars, outVars).await

        verify(visualization, timeout(callTimeoutMs)).init[IO](testMetadata, expectedVars)

        // Step 2: add single node
        val absSingleId = mapMpi.addNode(absSingle).logValue("addNode").await

        verify(visualization, timeout(callTimeoutMs)).nodesAdded[IO](Map(absSingleId -> absSingle))
        absSingleId mustBe a[MnId.Abs]

        // Step 3: upsert node with new name, should be added
        val conPositionId = mapMpi.upsertNodesByName(conPosition3).logValue("upsertNodesByName (new)").await

        verify(visualization, timeout(callTimeoutMs)).nodesAdded[IO](Map(conPositionId -> conPosition3))
        conPositionId mustBe a[MnId.Con]

        // Step 4: upsert node with existing name, should not be added again
        val existingId = mapMpi.upsertNodesByName(conPosition3).logValue("upsertNodesByName (exist)").await

        verify(visualization, Mockito.after(noCallWaitMs).times(2)).nodesAdded[IO](any[Map[MnId, NodeData]])(using
          any[MT[IO]],
        )
        existingId mustBe conPositionId

        // Step 5.1: add avoid obstacle samples, `conPosition3` node should be reused
        val avoidSamples = mapMpi.addSamples(avoidObstacleSample.samples, avoidObstacleSample.nodes)
          .logValue("addSamples (avoid obstacle)").await

        val avoidNodes = lastNodesAdded(visualization, times = 3)
        val namesAfterAvoid = namesOf(Map(absSingleId -> absSingle, conPositionId -> conPosition3) ++ avoidNodes)
        val avoidKeys = resolveKeys(avoidObstacleSample, namesAfterAvoid).await

        verify(visualization, timeout(callTimeoutMs)).edgesAdded[IO](avoidKeys)
        avoidSamples.values.toSet mustBe avoidObstacleSample.samples
        avoidNodes.values.toSet mustBe Set(conObstacleTrue, absAvoid, conTurnLeft)
        avoidNodes.keySet.intersect(Set(absSingleId, conPositionId)) mustBe empty

        // Step 5.2: add move forward samples, `conObstacleTrue` and `absAvoid` nodes should be reused
        val moveSamples = mapMpi.addSamples(moveForwardSample.samples, moveForwardSample.nodes)
          .logValue("addSamples (move forward)").await

        val moveNodes = lastNodesAdded(visualization, times = 4)
        val namesAfterMove = namesAfterAvoid ++ namesOf(moveNodes)
        val moveKeys = resolveKeys(moveForwardSample, namesAfterMove).await

        verify(visualization, timeout(callTimeoutMs)).edgesAdded[IO](moveKeys)
        moveSamples.values.toSet mustBe moveForwardSample.samples
        moveSamples.keySet.intersect(avoidSamples.keySet) mustBe empty
        moveNodes.values.toSet mustBe Set(conSpeedHalf, absMove, conStep2)
        moveNodes.keySet.intersect(namesAfterAvoid.values.toSet) mustBe empty

        // Step 6: add edge between standalone node and node added with samples
        val newKey = MeKey.Then(absSingleId, namesAfterMove(absMove.name.get))
        val addedKey = mapMpi.addEdge(newKey, moveSamples.keySet).logValue("addEdge").await

        verify(visualization, timeout(callTimeoutMs)).edgesAdded[IO](Set(newKey))
        addedKey mustBe newKey
