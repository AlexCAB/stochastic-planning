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
import planning.engine.common.values.node.{HnId, SnId, IoIndex}
import planning.engine.common.values.text.Name
import cats.syntax.all.*
import cats.effect.cps.*
import planning.engine.planner.dag.StateNode.{Kind, Parameters}

class StateNodeSpec extends UnitSpecWithData with DagTestData:

  private class CaseData extends Case:
    lazy val conId: SnId = SnId(1L)
    lazy val absId: SnId = SnId(2L)
    lazy val conHnId: HnId = HnId(123L)
    lazy val absHnId: HnId = HnId(321L)
    lazy val conName: Option[Name] = Some(Name("ConTestNode"))
    lazy val absName: Option[Name] = Some(Name("AbsTestNode"))
    lazy val valueIndex: IoIndex = IoIndex(432L)
    lazy val absParams: Parameters = Parameters.init.copy(kind = Kind.Present)
    lazy val conParams: Parameters = Parameters.init.copy(kind = Kind.Plan)

    lazy val absStateNode = AbstractStateNode[IO](absId, absHnId, absName, Set(), Set(), absParams).unsafeRunSync()

    lazy val conStateNode = ConcreteStateNode[IO]
      .apply(conId, conHnId, conName, intInNode, valueIndex, Set(), Set(absStateNode), conParams)
      .unsafeRunSync()

  "StateNode.isBelongsToIo(...)" should:
    "return false for abstract node" in newCase[CaseData]: (tn, data) =>
      data.absStateNode.isBelongsToIo(intInNode.name, data.valueIndex).pure[IO].asserting(_ mustEqual false)

    "return false for concrete node if params not match" in newCase[CaseData]: (tn, data) =>
      data.conStateNode.isBelongsToIo(Name("OtherIoNode"), IoIndex(999L)).pure[IO].asserting(_ mustEqual false)

    "return true for concrete node if params match" in newCase[CaseData]: (tn, data) =>
      data.conStateNode.isBelongsToIo(intInNode.name, data.valueIndex).pure[IO].asserting(_ mustEqual true)

  "StateNode.markAsPresentIfInValues(...)" should:
    "mark as present if value is in values" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val initValues = Map(intInNode.name -> data.valueIndex, intOutNode.name -> IoIndex(123L))

        val (newValues, opNode) = data.conStateNode.markAsPresentIfInValues(initValues).await

        newValues mustEqual initValues.removed(intInNode.name)
        opNode must not be empty
        opNode.get.hnId mustEqual data.conStateNode.hnId
        opNode.get.getParameters.await.kind mustEqual Kind.Present

    "not mark as present if value is not in values" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val initValues = Map(intOutNode.name -> IoIndex(123L))

        val (newValues, opNode) = data.conStateNode.markAsPresentIfInValues(initValues).await

        newValues mustEqual initValues
        opNode mustBe empty

  "StateNode.markThenChildrenAsPresentIfInValues(...)" should:
    "mark then children as present if their values are in values" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val initValues = Map(intInNode.name -> data.valueIndex, intOutNode.name -> IoIndex(123L))

        data.conStateNode.getStructure.flatMap(st => logInfo(tn, s"Before marking con then children: $st")).await
        data.absStateNode.getStructure.flatMap(st => logInfo(tn, s"Before abs marking then children: $st")).await

        val (newVals, updated) = data.absStateNode.markThenChildrenAsPresentIfInValues(initValues).await

        newVals mustEqual initValues.removed(intInNode.name)
        updated.movedToPresent mustEqual Set(data.conStateNode)
        updated.movedToPast mustEqual Set(data.absStateNode)

        data.conStateNode.getParameters.await.kind mustEqual Kind.Present
        data.absStateNode.getParameters.await.kind mustEqual Kind.Past
