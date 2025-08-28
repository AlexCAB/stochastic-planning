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
| created: 2025-08-22 |||||||||||*/

package planning.engine.planner.dag

import cats.effect.IO
import cats.effect.cps.async
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.node.IoIndex
import cats.effect.cps.*
import planning.engine.planner.dag.StateNode.{Kind, Parameters}

class StateNodeSpec extends UnitSpecWithData with DagTestData:

  private class CaseData extends Case:
    lazy val absParams: Parameters = Parameters.init.copy(kind = Kind.Present)
    lazy val conParams: Parameters = Parameters.init.copy(kind = Kind.Plan)

    lazy val absStateNode = AbstractStateNode[IO](absId, absHnId, absName, Set(), Set(), absParams).unsafeRunSync()

    lazy val conStateNode = ConcreteStateNode[IO]
      .apply(conId, conHnId, conName, intInNodes.head, valueIndex, Set(), Set(absStateNode), conParams)
      .unsafeRunSync()

  "StateNode.isInObservedValues(...)" should:
    "return this node in case it concrete and it is in values" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val initValues = Map(intInNodes.head.name -> valueIndex, intOutNode.name -> IoIndex(123L))
        val opNode = data.conStateNode.isInObservedValues(initValues).await
        opNode must not be empty
        opNode.get mustEqual data.conStateNode

    "return node if it abstract or not in values" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val initValues = Map(intOutNode.name -> IoIndex(123L))
        data.absStateNode.isInObservedValues(Map(intInNodes.head.name -> valueIndex)).await mustBe empty
        data.conStateNode.isInObservedValues(Map(intOutNode.name -> IoIndex(123L))).await mustBe empty

  "StateNode.findThenChildNodesInValues(...)" should:
    "find concrete children as present if their values are in values" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val initValues = Map(intInNodes.head.name -> valueIndex, intOutNode.name -> IoIndex(123L))
        data.conStateNode.getStructure.flatMap(st => logInfo(tn, s"Before marking con then children: $st")).await
        data.absStateNode.getStructure.flatMap(st => logInfo(tn, s"Before abs marking then children: $st")).await
        data.absStateNode.findThenChildNodesInValues(initValues).await mustEqual Set(data.conStateNode)

  "StateNode.addLinkChild(...)" should:
    "to add link child" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        data.conStateNode.addLinkChild(data.absStateNode).await
        data.conStateNode.getStructure.await.linkChildren mustEqual Set(data.absStateNode)

  "StateNode.addThenChild(...)" should:
    "to add then child" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        data.conStateNode.addThenChild(data.absStateNode).await
        data.conStateNode.getStructure.await.thenChildren mustEqual Set(data.absStateNode)

  "StateNode.setPresent(...)" should :
    "set node kind to Present if it is have Plan kind" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        data.conStateNode.getParameters.await.kind mustEqual Kind.Plan
        data.conStateNode.setPresent().await
        data.conStateNode.getParameters.await.kind mustEqual Kind.Present

  "StateNode.setPast(...)" should :
    "set node kind to Past if it is have Present kind" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        data.absStateNode.getParameters.await.kind mustEqual Kind.Present
        data.absStateNode.setPast().await
        data.absStateNode.getParameters.await.kind mustEqual Kind.Past
