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
| created: 2026-02-22 |||||||||||*/

package planning.engine.common.graph.io

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.UnitSpecIO
import planning.engine.common.values.io.{IoIndex, IoName, IoValue}
import planning.engine.common.values.node.MnId

class IoValueMapSpec extends UnitSpecIO:
  lazy val n1 = MnId.Con(1)
  lazy val n2 = MnId.Con(2)
  lazy val n3 = MnId.Con(3)
  lazy val newMnId = MnId.Con(1001)

  lazy val ioValue1 = IoValue(IoName("TestIoVariable1"), IoIndex(100))
  lazy val ioValue2 = IoValue(IoName("TestIoVariable2"), IoIndex(200))
  lazy val ioValueNotInMap = IoValue(IoName("NonExisting"), IoIndex(300))

  lazy val rawIoValueMap = Map(ioValue1 -> Set(n1), ioValue2 -> Set(n2, n3))
  lazy val ioValueMap = new IoValueMap[IO](rawIoValueMap)

  "IoValueMap.isEmpty" should:
    "return true for empty map" in: _ =>
      IoValueMap.empty[IO].isEmpty mustBe true

    "return false for non-empty map" in: _ =>
      ioValueMap.isEmpty mustBe false

  "IoValueMap.values" should:
    "return correct sets of MnIds" in: _ =>
      ioValueMap.values must contain theSameElementsAs List(Set(n1), Set(n2, n3))

  "IoValueMap.keySet" should:
    "return correct set of IoValues" in: _ =>
      ioValueMap.keySet must contain theSameElementsAs Set(ioValue1, ioValue2)

  "IoValueMap.allMnIds" should:
    "return correct set of all MnIds" in: _ =>
      ioValueMap.allMnIds must contain theSameElementsAs Set(n1, n2, n3)

  "IoValueMap.contains(...)" should:
    "return true for existing IoValue" in: _ =>
      ioValueMap.contains(ioValue1) mustBe true
      ioValueMap.contains(ioValue2) mustBe true

    "return false for non-existing IoValue" in: _ =>
      ioValueMap.contains(ioValueNotInMap) mustBe false

  "IoValueMap.get(...)" should:
    "return correct set of MnIds for existing IoValue" in: tn =>
      ioValueMap.get(ioValue1).logValue(tn).asserting(_ mustBe Set(n1))
      ioValueMap.get(ioValue2).logValue(tn).asserting(_ mustBe Set(n2, n3))

    "throw an error for non-existing IoValue" in: tn =>
      ioValueMap.get(ioValueNotInMap)
        .logValue(tn).assertThrowsError[AssertionError](_
          .getMessage must include(s"No nodes id found for IoValue $ioValueNotInMap"))

  "IoValueMap.updateOrAddIoValues(...)" should:
    "update existing ioValues" in: tn =>
      async[IO]:
        val (ioValue, conMnIds): (IoValue, Set[MnId.Con]) = ioValueMap.updateOrAddIoValues(ioValue1, newMnId).await
        logInfo(tn, s"ioValue: $ioValue, conMnIds: $conMnIds").await

        ioValue mustBe ioValue1
        conMnIds must contain(newMnId)

    "add new ioValues" in: tn =>
      async[IO]:
        val (ioValue, conMnIds): (IoValue, Set[MnId.Con]) = ioValueMap
          .updateOrAddIoValues(ioValueNotInMap, newMnId)
          .await

        logInfo(tn, s"ioValue: $ioValue, conMnIds: $conMnIds").await

        ioValue mustBe ioValueNotInMap
        conMnIds must contain(newMnId)

    "fail if ioValue already exist for different node" in: tn =>
      ioValueMap.updateOrAddIoValues(ioValue1, n1).logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include("Duplicate node id"))

  "IoValueMap.addIoValues(...)" should:
    "add new ioValues" in: tn =>
      async[IO]:
        val newIoValueMap = ioValueMap.addIoValues(List(ioValueNotInMap -> newMnId)).await
        logInfo(tn, s"newIoValueMap: $newIoValueMap").await

        newIoValueMap.keySet must contain(ioValueNotInMap)
        newIoValueMap.get(ioValueNotInMap).await must contain(newMnId)

  "IoValueMap.empty" should:
    "return empty IoValueMap" in: _ =>
      IoValueMap.empty[IO].valueMap.isEmpty mustBe true

  "IoValueMap.apply(Map[IoValue, Set[Con]])" should:
    "create IoValueMap from given map" in: tn =>
      IoValueMap[IO](rawIoValueMap).logValue(tn).asserting(_ mustBe ioValueMap)

    "fail if two or more IoValues refer to the same MnId" in: tn =>
      val invalidMap = Map(ioValue1 -> Set(n1), ioValue2 -> Set(n1))
      IoValueMap[IO](invalidMap).logValue(tn)
        .assertThrowsError[AssertionError](_
          .getMessage must include("Two or more IoValue cant refer to the same MnId"))
