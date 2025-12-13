//package planning.engine.planner.plan.dag

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
//| created: 2025-08-26 |||||||||||*/
//
//package planning.engine.planner.dag
//
//import cats.effect.IO
//import cats.effect.cps.async
//import planning.engine.common.UnitSpecWithData
//import cats.effect.cps.*
//import planning.engine.common.values.io.IoIndex
//import planning.engine.common.values.text.Name
//import planning.engine.planner.plan.dag.{ConcreteStateNode, StateNode}
//
//class ConcreteStateNodeSpec extends UnitSpecWithData with DagTestData:
//
//  private class CaseData extends Case:
//    lazy val conStateNode = ConcreteStateNode[IO]
//      .apply(conId, conHnId, conName, intInNodes.head, valueIndex, Set(), Set(), StateNode.Parameters.init)
//      .unsafeRunSync()
//
//  "ConcreteStateNode.isBelongsToIo(...)" should:
//    "return true for if params match and false otherwise" in newCase[CaseData]: (_, data) =>
//      async[IO]:
//        data.conStateNode.isBelongsToIo(intInNodes.head.name, valueIndex) mustEqual true
//        data.conStateNode.isBelongsToIo(intInNodes.head.name, IoIndex(999L)) mustEqual false
//        data.conStateNode.isBelongsToIo(Name("OtherIoNode"), valueIndex) mustEqual false
