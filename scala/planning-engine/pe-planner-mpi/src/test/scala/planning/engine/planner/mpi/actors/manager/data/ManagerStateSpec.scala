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
| created: 30.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.manager.data

import cats.effect.IO
import cats.effect.cps.*
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.{Description, Name}
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.common.data.node.NodeData
import planning.engine.planner.mpi.common.data.samples.Sample
import planning.engine.planner.mpi.test.data.MapNodeTestData
import planning.engine.planner.mpi.test.data.MapEdgeTestData

class ManagerStateSpec extends UnitSpecWithData with AsyncMockFactory with MapNodeTestData with MapEdgeTestData:
  private class CaseData extends Case with WithMapNode with WithMapEdge:
    def spawnNode(rawId: Long, data: NodeData): IO[Node] = IO.pure(makeNodeStub(data.nodeType.toMnId(rawId), data.name))

    lazy val stateEmpty: State = State.init

    lazy val (conNodesMap, stateWithConNode): (Map[MnId.Nim, Node], State) = stateEmpty
      .withNewNodes[IO](Map(nim1 -> conNodeData), spawnNode)
      .unsafeRunSync()

    lazy val conNode: Node = conNodesMap(nim1)

    lazy val (nodesMap, stateWithNodes): (Map[MnId.Nim, Node], State) = stateEmpty
      .withNewNodes[IO](Map(nim1 -> conNodeData, nim2 -> absNodeData), spawnNode)
      .unsafeRunSync()

    lazy val absNode: Node = nodesMap(nim2)

    lazy val sampleProps1: Sample.Props = makePropVals(3)
    lazy val sampleProps2: Sample.Props = makePropVals(7)

    lazy val sampleInfo1: Sample.Info = Sample.Info(Name("Sample 1"), Some(Description("A test sample")))

    lazy val manSample1: Sample.Man = Sample.Man(sampleProps1, sampleInfo1, Set.empty)
    lazy val genSample1: Sample.Gen = Sample.Gen(sampleProps2, Set.empty)

  "State.withNewNodes(...)" should:
    "add a named node to nodeRefMap and nodeNameMap, and increment nextMnId" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val (nodes, state) = stateEmpty
          .withNewNodes[IO](Map(nim1 -> conNodeData, nim2 -> absNodeData), spawnNode)
          .await

        nodes.keySet mustBe Set(nim1, nim2)
        state.nodeRefMap.keySet mustBe Set(conMnId, absMnId)
        state.nodeRefMap.values.toSet mustBe nodes.values.toSet
        state.nextMnId mustBe 3L

        state.nodeNameMap mustBe Map(
          conNodeData.name.get -> Set(conMnId),
          absNodeData.name.get -> Set(absMnId),
        )

    "add nodes with duplicate data" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val id1 = MnId.Con(1L)
        val id2 = MnId.Con(2L)

        val (_, state) = stateEmpty
          .withNewNodes[IO](Map(nim1 -> conNodeData, nim2 -> conNodeData), spawnNode)
          .await

        state.nodeRefMap.keySet mustBe Set(id1, id2)
        state.nextMnId mustBe 3L
        state.nodeNameMap mustBe Map(conNodeData.name.get -> Set(id1, id2))

    "not add to nodeNameMap for a node without a name" in newCase[CaseData]: (_, data) =>
      import data.*
      val unnamedData = absNodeData.copy(name = None)

      stateEmpty.withNewNodes[IO](Map(nim1 -> unnamedData), spawnNode)
        .asserting((_, state) => state.nodeNameMap mustBe Map.empty)

    "raise an error when a node ID already exists in state" in newCase[CaseData]: (_, data) =>
      import data.*
      val conflictingState =
        stateWithNodes.copy(nextMnId = 1L) // re-assigning from 1 collides with the existing conMnId entry

      conflictingState.withNewNodes[IO](Map(nim1 -> conNodeData), spawnNode)
        .assertThrowsError[AssertionError](_.getMessage must include("Node IDs already exist in the current state"))

  "State.withNewManSamples(...)" should:
    "add manual samples to sampleDataMap and increment nextSampleId" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val (samples, state) = stateEmpty.withNewManSamples[IO](Set(manSample1)).await

        samples.values.toSet mustBe Set(manSample1)
        val id = samples.keySet.head
        state.sampleDataMap mustBe Map(id -> State.SampleData(manSample1.props, Some(manSample1.info)))
        state.nextSampleId mustBe 2L

    "raise an error when a sample ID already exists in state" in newCase[CaseData]: (_, data) =>
      import data.*

      stateEmpty.withNewManSamples[IO](Set(manSample1))
        .flatMap((_, state) => state.copy(nextSampleId = 1L).withNewManSamples[IO](Set(manSample1)))
        .assertThrowsError[AssertionError](_
          .getMessage must include("Sample IDs already exist in the current state"))

  "State.withNewGenSamples(...)" should:
    "add generated samples to sampleDataMap with no info and inc nextSampleId" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val (samples, state) = stateEmpty.withNewGenSamples[IO](Set(genSample1)).await

        samples.values.toSet mustBe Set(genSample1)
        val id = samples.keySet.head
        state.sampleDataMap mustBe Map(id -> State.SampleData(genSample1.props, None))
        state.nextSampleId mustBe 2L

  "State.getNode(...)" should:
    "return the node for a known MnId" in newCase[CaseData]: (_, data) =>
      import data.*
      stateWithNodes.getNode[IO](conMnId).asserting(_ mustBe nodesMap(nim1))

    "raise an error for an unknown MnId" in newCase[CaseData]: (_, data) =>
      import data.*
      stateEmpty.getNode[IO](conMnId)
        .assertThrowsError[AssertionError](_.getMessage must include(s"Node ID $conMnId not found in state"))

  "State.getSamples(...)" should:
    "return sample data for known sample IDs" in newCase[CaseData]: (_, data) =>
      import data.*
      async[IO]:
        val (manSamples, stateWithMan) = stateEmpty.withNewManSamples[IO](Set(manSample1)).await
        val (genSamples, stateWithBoth) = stateWithMan.withNewGenSamples[IO](Set(genSample1)).await
        val manId = manSamples.keySet.head
        val genId = genSamples.keySet.head
        val result = stateWithBoth.getSamples[IO](Set(manId, genId)).await

        result mustBe Map(
          manId -> State.SampleData(manSample1.props, Some(manSample1.info)),
          genId -> State.SampleData(genSample1.props, None),
        )

    "raise an error when some sample IDs are not found in state" in newCase[CaseData]: (_, _) =>
      State.init.getSamples[IO](Set(SampleId(1L)))
        .assertThrowsError[AssertionError](_.getMessage must include("Some sample IDs not found in state"))

  "State.findByName(...)" should:
    "return Some(node) for a found name" in newCase[CaseData]: (_, data) =>
      import data.*
      stateWithConNode.findByName[IO](conNodeData.name.get).asserting(_ mustBe Some(conNode))

    "return None when no name matches" in newCase[CaseData]: (_, data) =>
      import data.*
      stateWithConNode.findByName[IO](absNodeData.name.get).asserting(_ mustBe None)

    "return the correct node when multiple different names exist" in newCase[CaseData]: (_, data) =>
      import data.*
      stateWithNodes.findByName[IO](absNodeData.name.get).asserting(_ mustBe Some(absNode))

    "raise an error when a name maps to more than one node ID" in newCase[CaseData]: (_, data) =>
      val duplicateNameState = State.init.copy(
        nodeNameMap = Map(data.conNodeData.name.get -> Set(MnId.Con(1L), MnId.Con(2L))),
        nextMnId = 3L,
      )

      duplicateNameState.findByName[IO](data.conNodeData.name.get)
        .assertThrowsError[AssertionError](_.getMessage must include("Expected exactly one node ID for name"))
