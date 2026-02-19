///*|||||||||||||||||||||||||||||||||
//|| 0 * * * * * * * * * ▲ * * * * ||
//|| * ||||||||||| * ||||||||||| * ||
//|| * ||  * * * * * ||       || 0 ||
//|| * ||||||||||| * ||||||||||| * ||
//|| * * ▲ * * 0|| * ||   (< * * * ||
//|| * ||||||||||| * ||  ||||||||||||
//|| * * * * * * * * *   ||||||||||||
//| author: CAB |||||||||||||||||||||
//| website: github.com/alexcab |||||
//| created: 2025-12-25 |||||||||||*/
//
//package planning.engine.planner.map.state
//
//import cats.effect.IO
//import cats.syntax.all.*
//import planning.engine.common.UnitSpecWithData
//import planning.engine.common.values.io.IoName
//import planning.engine.map.data.MapMetadata
//import planning.engine.planner.map.state.MapInfoState
//import planning.engine.planner.map.test.data.MapNodeTestData
//
//class MapInfoStateSpec extends UnitSpecWithData:
//
//  private class CaseData extends Case with MapNodeTestData:
//    lazy val mapInfoState = new MapInfoState[IO](
//      metadata = testMetadata,
//      inNodes = Map(testBoolInNode.name -> testBoolInNode, testIntInNode.name -> testIntInNode),
//      outNodes = Map(boolOutNode.name -> boolOutNode)
//    )
//
//  "MapInfoState.empty" should:
//    "create an empty MapInfoState" in newCase[CaseData]: (tn, data) =>
//      MapInfoState.empty[IO].pure[IO].logValue(tn, "emptyMapInfoState").asserting: info =>
//        info.metadata mustBe MapMetadata.empty
//        info.inNodes mustBe Map.empty
//        info.outNodes mustBe Map.empty
//
//  "MapInfoState.apply" should:
//    "create a valid MapInfoState with distinct IO node names" in newCase[CaseData]: (tn, data) =>
//      MapInfoState[IO](data.testMetadata, data.testInNodes, data.testOutNodes)
//        .logValue(tn, "mapInfoState")
//        .asserting(_ mustBe data.mapInfoState)
//
//    "fail to create MapInfoState with duplicate IO node names" in newCase[CaseData]: (tn, data) =>
//      MapInfoState[IO](data.testMetadata, List(data.testBoolInNode, data.testBoolInNode), List())
//        .logValue(tn, "mapInfoStateWithDuplicates")
//        .assertThrowsError[AssertionError](_.getMessage must include("All IO node names must be distinct"))
//
//  "MapInfoState.allIoNodes" should:
//    "return all IO nodes correctly" in newCase[CaseData]: (tn, data) =>
//      data.mapInfoState.allIoNodes.pure[IO]
//        .logValue(tn, "allIoNodes")
//        .asserting(_ mustBe (data.mapInfoState.inNodes ++ data.mapInfoState.outNodes))
//
//  "MapInfoState.isEmpty" should:
//    "return true for empty MapInfoState" in newCase[CaseData]: (_, data) =>
//      MapInfoState.empty[IO].pure[IO].map(_.isEmpty).asserting(_ mustBe true)
//
//    "return false for non-empty MapInfoState" in newCase[CaseData]: (_, data) =>
//      data.mapInfoState.pure[IO].map(_.isEmpty).asserting(_ mustBe false)
//
//  "MapInfoState.getIoNode" should:
//    "return the correct IO node when it exists" in newCase[CaseData]: (tn, data) =>
//      data.mapInfoState.getIoNode(data.testBoolInNode.name)
//        .logValue(tn, "foundIoNode")
//        .asserting(_ mustBe data.testBoolInNode)
//
//    "fail when the IO node does not exist" in newCase[CaseData]: (tn, data) =>
//      val missingName = IoName("missingNode")
//      data.mapInfoState.getIoNode(missingName)
//        .logValue(tn, "missingIoNode")
//        .assertThrowsError[AssertionError](_.getMessage must include(s"IO node with name $missingName not found"))
