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
| created: 2025-12-25 |||||||||||*/

package planning.engine.api.service.map

import cats.effect.IO
import cats.effect.cps.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.node.{HnId, HnName}

class MapServiceBaseSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val hnId1 = HnId(1)
    lazy val hnId2 = HnId(2)
    lazy val hnId3 = HnId(3)
    lazy val hnId4 = HnId(4)

    lazy val hnName1 = HnName("Node_1")
    lazy val hnName2 = HnName("Node_2")
    lazy val hnName3 = HnName("Node_3")
    lazy val hnName4 = HnName("Node_4")

    lazy val foundHnIdMap = Map(hnName1 -> Set(hnId1), hnName2 -> Set(hnId2))
    lazy val newConHnIds = Map(hnId3 -> Some(hnName3))
    lazy val newAbsHnIds = Map(hnId4 -> Some(hnName4))

    val mapServiceBase = new MapServiceBase[IO] {}

  "MapServiceBase.composeHnIdMap(...)" should:
    "compose hn name to id map from found and new hn ids" in newCase[CaseData]: (tn, data) =>
      async[IO]:
        val hnIdMap = data.mapServiceBase
          .composeHnIdMap(data.foundHnIdMap, data.newConHnIds ++ data.newAbsHnIds)
          .logValue(tn)
          .await

        hnIdMap mustBe Map(
          data.hnName1 -> data.hnId1,
          data.hnName2 -> data.hnId2,
          data.hnName3 -> data.hnId3,
          data.hnName4 -> data.hnId4
        )

    "fail if more then one HnId found" in newCase[CaseData]: (tn, data) =>
      data.mapServiceBase
        .composeHnIdMap(Map(data.hnName1 -> Set(data.hnId1, HnId(99))), data.newConHnIds ++ data.newAbsHnIds)
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include(s"Expect exactly one variable"))

    "fail if no name for new HnId" in newCase[CaseData]: (tn, data) =>
      data.mapServiceBase
        .composeHnIdMap(data.foundHnIdMap, Map(data.hnId3 -> None) ++ data.newAbsHnIds)
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include(s"No name found for hnId"))

    "fail if duplicate hn names in found ids" in newCase[CaseData]: (tn, data) =>
      data.mapServiceBase
        .composeHnIdMap(data.foundHnIdMap, Map(data.hnId1 -> Some(data.hnName1)) ++ data.newAbsHnIds)
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include(s"Hn names must be distinct"))
