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
//import planning.engine.common.UnitSpecWithData
//
//import cats.syntax.all.*
//
//class AbstractStateNodeSpec extends UnitSpecWithData with DagTestData:
//
//  private class CaseData extends Case:
//    lazy val absStateNode = AbstractStateNode[IO]
//      .apply(absId, absHnId, absName, Set(), Set(), StateNode.Parameters.init)
//      .unsafeRunSync()
//
//  "AbstractStateNode.isBelongsToIo(...)" should:
//    "return false" in newCase[CaseData]: (_, data) =>
//      data.absStateNode.isBelongsToIo(intInNodes.head.name, valueIndex).pure[IO].asserting(_ mustEqual false)
