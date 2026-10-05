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
| created: 2026-10-05 |||||||||||*/

package planning.engine.api.model.map.extensions.gsi

import cats.effect.IO
import cats.effect.cps.*
import io.circe.Json
import org.mockito.scalatest.AsyncIdiomaticMockito
import planning.engine.api.model.map.MapAddSamplesRequest
import planning.engine.api.model.map.extensions.gsi.AbstractNodeDef.toNew
import planning.engine.api.model.map.extensions.gsi.MapAddSamplesRequestEx.*
import planning.engine.api.model.map.payload.*
import planning.engine.common.UnitSpecWithData
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.io.{IoIndex, IoName}
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.common.values.text.{Description, Name}
import planning.engine.map.hidden.node.ConcreteNode
import planning.engine.map.io.node.{InputNode, IoNode}
import planning.engine.map.io.variable.IntIoVariableLike

class MapAddSamplesRequestExSpec extends UnitSpecWithData with AsyncIdiomaticMockito:

  private class CaseData extends Case:
    lazy val testEdge = NewSampleEdge(
      sourceHnName = HnName("hn1"),
      targetHnName = HnName("hn2"),
      edgeType = EdgeType.THEN,
    )

    lazy val testValue = 1234L
    lazy val mockedIntIoVariable: IntIoVariableLike[IO] = mock[IntIoVariableLike[IO]]
    lazy val ioNode = InputNode(name = IoName("ioNode1"), variable = mockedIntIoVariable)
    lazy val mockedGetIoNode: IoName => IO[IoNode[IO]] = mock[IoName => IO[IoNode[IO]]]

    lazy val testConcreteNodeDef = ConcreteNodeDef(
      testEdge.sourceHnName,
      Description.some("testConcreteNodeDef"),
      ioNode.name,
      Json.fromLong(testValue),
    )

    lazy val testAbstractNodeDef = AbstractNodeDef(testEdge.targetHnName, Description.some("testAbstractNodeDef"))

    lazy val testNewSampleData: NewSampleData = NewSampleData(
      probabilityCount = 10,
      utility = 0.5,
      name = Name.some("sample1"),
      description = Description.some("Sample 1 description"),
      edges = List(testEdge),
    )

    lazy val hnIdMap: Map[HnName, HnId] = Map(
      testEdge.sourceHnName -> HnId(1),
      testEdge.targetHnName -> HnId(2),
    )

    lazy val testRequest: MapAddSamplesRequest = MapAddSamplesRequest(
      samples = List(testNewSampleData),
      hiddenNodes = List(testConcreteNodeDef, testAbstractNodeDef),
    )

  "MapAddSamplesRequestEx.listNewNotFoundHn" should:
    "return empty lists when all hidden nodes are found" in newCase[CaseData]: (_, data) =>
      async[IO]:
        val foundHnNames = Set(data.testConcreteNodeDef.name, data.testAbstractNodeDef.name)
        val (concreteList, abstractList) = data.testRequest.listNewNotFoundHn(foundHnNames, data.mockedGetIoNode).await

        data.mockedGetIoNode(data.testConcreteNodeDef.ioNodeName) wasNever called
        concreteList.list mustBe empty
        abstractList.list mustBe empty

    "return lists of new hidden nodes when some are not found" in newCase[CaseData]: (_, data) =>
      val testIoIndex = IoIndex(4321)
      data.mockedGetIoNode(data.testConcreteNodeDef.ioNodeName) returns IO.pure(data.ioNode)
      data.mockedIntIoVariable.indexForValue(data.testValue) returns IO.pure(testIoIndex)

      async[IO]:
        val (concreteList, abstractList) = data.testRequest.listNewNotFoundHn(Set(), data.mockedGetIoNode).await

        data.mockedGetIoNode(data.testConcreteNodeDef.ioNodeName) was called
        data.mockedIntIoVariable.indexForValue(data.testValue) was called
        concreteList.list.size mustEqual 1

        concreteList.list.head mustEqual ConcreteNode.New(
          name = Some(data.testConcreteNodeDef.name),
          description = data.testConcreteNodeDef.description,
          ioNodeName = data.testConcreteNodeDef.ioNodeName,
          valueIndex = testIoIndex,
        )

        abstractList.list.size mustEqual 1
        abstractList.list.head mustEqual data.testAbstractNodeDef.toNew

  "MapAddSamplesRequestEx.toSampleNewList(...)" should:
    "convert to new samples" in newCase[CaseData]: (_, data) =>
      async[IO]:
        val sampleListNew = data.testRequest.toSampleNewList[IO](data.hnIdMap).await

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
