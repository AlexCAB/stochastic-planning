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
| created: 2025-07-12 |||||||||||*/

package planning.engine.api.model.map

import cats.effect.IO
import planning.engine.api.model.map.payload.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.{HnId, IoIndex}
import planning.engine.common.values.text.{Description, Name}
import cats.effect.cps.*

class MapAddSamplesRequestSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val testEdge = NewSampleEdge(
      sourceHnName = Name("hn1"),
      targetHnName = Name("hn2"),
      edgeType = EdgeType.THEN
    )

    lazy val testConcreteNodeDef = ConcreteNodeDef(testEdge.sourceHnName, Name("ioNode1"), IoIndex(0))
    lazy val testAbstractNodeDef = AbstractNodeDef(testEdge.targetHnName)

    lazy val testNewSampleData: NewSampleData = NewSampleData(
      probabilityCount = 10,
      utility = 0.5,
      name = Some(Name("sample1")),
      description = Some(Description("Sample 1 description")),
      hiddenNodes = List(testConcreteNodeDef, testAbstractNodeDef),
      edges = List(testEdge)
    )

    lazy val hnIdMap: Map[Name, HnId] = Map(
      testEdge.sourceHnName -> HnId(1),
      testEdge.targetHnName -> HnId(2)
    )

    lazy val testRequest: MapAddSamplesRequest = MapAddSamplesRequest(samples = List(testNewSampleData))

  "MapAddSamplesRequest.hnNames" should:
    "list all hidden node names" in newCase[CaseData]: (_, data) =>
      async[IO]:
        data.testRequest.hnNames mustEqual List(data.testConcreteNodeDef.name, data.testAbstractNodeDef.name)

  "MapAddSamplesRequest.listNewNotFoundHn" should:
    "return empty lists when all hidden nodes are found" in newCase[CaseData]: (_, data) =>
      async[IO]:
        val foundHnNames = Set(data.testConcreteNodeDef.name, data.testAbstractNodeDef.name)
        val (concreteList, abstractList) = data.testRequest.listNewNotFoundHn(foundHnNames)

        concreteList.list mustBe empty
        abstractList.list mustBe empty

    "return lists of new hidden nodes when some are not found" in newCase[CaseData]: (_, data) =>
      async[IO]:
        val (concreteList, abstractList) = data.testRequest.listNewNotFoundHn(Set())

        concreteList.list.size mustEqual 1
        concreteList.list.head mustEqual data.testConcreteNodeDef.toNew

        abstractList.list.size mustEqual 1
        abstractList.list.head mustEqual data.testAbstractNodeDef.toNew

  "MapAddSamplesRequest.toSampleNewList(...)" should:
    "convert to new samples" in newCase[CaseData]: (_, data) =>
      data.testRequest.toSampleNewList[IO](data.hnIdMap).asserting: sampleListNew =>
        sampleListNew.list.size mustEqual 1
        val sample = sampleListNew.list.head

        sample.probabilityCount mustEqual data.testNewSampleData.probabilityCount
        sample.utility mustEqual data.testNewSampleData.utility
        sample.name mustEqual data.testNewSampleData.name
        sample.description mustEqual data.testNewSampleData.description

        sample.edges.size mustEqual 1
        val edge = sample.edges.head

        edge.source mustEqual data.hnIdMap(data.testEdge.sourceHnName)
        edge.target mustEqual data.hnIdMap(data.testEdge.targetHnName)
        edge.edgeType mustEqual data.testEdge.edgeType
