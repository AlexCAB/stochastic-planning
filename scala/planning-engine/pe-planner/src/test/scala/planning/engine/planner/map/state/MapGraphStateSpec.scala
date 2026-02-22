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
| created: 2025-12-13 |||||||||||*/

package planning.engine.planner.map.state

import cats.effect.IO
import cats.effect.cps.*
import cats.syntax.all.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.io.{IoIndex, IoName, IoValue}
import planning.engine.common.values.node.MnId
import planning.engine.planner.map.dcg.nodes.DcgNode
import planning.engine.planner.map.test.data.DcgStatesTestData

class MapGraphStateSpec extends UnitSpecWithData:

  private class CaseData extends Case with DcgStatesTestData:
    lazy val cMnId1 = MnId.Con(1001)
    lazy val aMnId2 = MnId.Abs(1002)

    lazy val conNode1 = makeConDcgNode(id = cMnId1, valueIndex = IoIndex(102))
    lazy val absNode2 = makeAbsDcgNode(id = aMnId2)

  "MapGraphState.isEmpty" should:
    "return true for empty state" in newCase[CaseData]: (_, data) =>
      data.emptyDcgState.pure[IO].asserting(_.isEmpty mustBe true)

    "return false for non empty state" in newCase[CaseData]: (_, data) =>
      data.initDcgState.pure[IO].asserting(_.isEmpty mustBe false)

  "MapGraphState.ioValuesMnId" should:
    "return ioValues to MnIds mapping" in newCase[CaseData]: (tn, data) =>
      data.initDcgState.ioValuesMnId.pure[IO].logValue(tn)
        .asserting(_ mustBe data.graphWithEdges.conMnId)

//  "MapGraphState.updateOrAddIoValues" should:
//    "update existing ioValues" in newCase[CaseData]: (tn, data) =>
//      import data.*
//      async[IO]:
//        val existIoValues = initDcgState.ioValues.keySet.head
//        existIoValues.name mustBe testBoolInNode.name
//
//        val newNode = makeConDcgNode(MnId.Con(1001), existIoValues.index)
//        val (ioValue, conMnIds): (IoValue, Set[MnId.Con]) = initDcgState.updateOrAddIoValues(newNode).await
//        logInfo(tn, s"ioValue: $ioValue, conMnIds: $conMnIds").await
//
//        ioValue mustBe existIoValues
//        conMnIds must contain(newNode.id)
//
//    "add new ioValues" in newCase[CaseData]: (tn, data) =>
//      import data.*
//      async[IO]:
//        val newNode = makeConDcgNode(MnId.Con(1002), IoIndex(999))
//        val (ioValue, conMnIds): (IoValue, Set[MnId.Con]) = initDcgState.updateOrAddIoValues(newNode).await
//        logInfo(tn, s"ioValue: $ioValue, conMnIds: $conMnIds").await
//
//        ioValue mustBe newNode.ioValue
//        conMnIds must contain(newNode.id)
//
//    "fail if ioValue already exist for different node" in newCase[CaseData]: (tn, data) =>
//      import data.*
//      val existMnId = initDcgState.ioValuesMnId.head
//      val existIoValues = initDcgState.ioValues.keys.head
//      val invalidNode = makeConDcgNode(existMnId, existIoValues.index)
//
//      initDcgState.updateOrAddIoValues(invalidNode).logValue(tn)
//        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate node id"))

  "MapGraphState.addNodes" should:
    "add nodes" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val newNodes = List(conNode1, absNode2)

        val emptyState: MapGraphState[IO] = data.emptyDcgState.addNodes(newNodes).await
        logInfo(tn, s"state.ioValues = ${emptyState.ioValues}, emptyState = $emptyState").await

        emptyState.ioValues.valueMap must contain key conNode1.ioValue
        emptyState.ioValues.valueMap(conNode1.ioValue) must contain(conNode1.id)

        val initState: MapGraphState[IO] = data.initDcgState.addNodes(newNodes).await
        logInfo(tn, s"state.ioValues = ${initState.ioValues}, initState = $initState").await

        initState.ioValues.valueMap must contain key conNode1.ioValue
        initState.ioValues.valueMap(conNode1.ioValue) must contain(conNode1.id)

    "fail if nodes with duplicate ids added" in newCase[CaseData]: (tn, data) =>
      import data.*
      val duplicateNode = makeConDcgNode(conNodes.head.id, IoIndex(999))

      data.initDcgState.addNodes(List(duplicateNode)).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Two or more nodes can't have the same MnId"))

  "MapGraphState.addSamples" should:
    "add new sample" in newCase[CaseData]: (tn, data) =>
      import data.*

      initDcgState.addSamples(List(simpleSampleAdd)).logValue(tn)
        .asserting(_.graph.samples must contain key simpleSampleId)

  "MapGraphState.getConForIoValue" should:
    "get concrete nodes for IoValue" in newCase[CaseData]: (tn, data) =>
      import data.*
      val ioValue = conNodes.head.ioValue

      async[IO]:
        val (foundIoValue, foundConNodes): (IoValue, Set[DcgNode.Concrete[IO]]) = initDcgState
          .getConForIoValue(ioValue)
          .await

        logInfo(tn, s"foundIoValue: $foundIoValue, conNodes: $foundConNodes").await

        foundIoValue mustBe ioValue
        foundConNodes mustBe conNodes.filter(_.ioValue == ioValue).toSet

  "MapGraphState.findConForIoValues" should:
    "get concrete nodes for IoValues" in newCase[CaseData]: (tn, data) =>
      import data.*
      val ioValue1 = conNodes.head.ioValue
      val ioValue2 = IoValue(IoName("not_in_set"), IoIndex(123))

      async[IO]:
        val (map, notFound) = initDcgState.findConForIoValues(Set(ioValue1, ioValue2)).await
        logInfo(tn, s"map: $map, notFound: $notFound").await

        map mustBe Map(ioValue1 -> conNodes.filter(_.ioValue == ioValue1).toSet)
        notFound mustBe Set(ioValue2)

  "MapGraphState.empty" should:
    "return empty state" in newCase[CaseData]: (tn, data) =>
      data.emptyDcgState.pure[IO].asserting(_.isEmpty mustBe true)

  "MapGraphState.apply(Map[IoValue, Set[MnId.Con]], DcgGraph[F])" should:    
    "create new state" in newCase[CaseData]: (tn, data) =>
      import data.*
      async[IO]:
        val newState = MapGraphState(initDcgState.ioValues, initDcgState.graph).await
        logInfo(tn, s"newState.ioValues = ${newState.ioValues}, newState.graph = ${newState.graph}").await

        newState.ioValues mustBe initDcgState.ioValues
        newState.graph mustBe initDcgState.graph
        
//    "fail if two or more IoValue cant refer to the same MnId" in newCase[CaseData]: (tn, data) =>
//        import data.*
//        val invalidIoValues = Map(
//            IoValue(IoName("io1"), IoIndex(1)) -> Set(MnId.Con(1001)),
//            IoValue(IoName("io2"), IoIndex(2)) -> Set(MnId.Con(1001))
//        )
//    
//        MapGraphState(invalidIoValues, initDcgState.graph).logValue(tn)
//            .assertThrowsError[AssertionError](_.getMessage must include("Two or more IoValue cant refer to the same MnId"))