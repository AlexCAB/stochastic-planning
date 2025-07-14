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
| created: 2025-07-11 |||||||||||*/

package planning.engine.api.model.map

import planning.engine.api.model.map.payload.*
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.node.IoIndex
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.{Description, Name}
import planning.engine.map.graph.MapConfig

trait TestApiData:
  lazy val testConfig: MapConfig = MapConfig(
    initNextHnId = 100L,
    initNextSampleId = 200L,
    initSampleCount = 300L,
    initNextHnIndex = 400L,
    samplesName = "samples"
  )

  lazy val testMapInitRequest = MapInitRequest(
    name = Some(Name("testMapName")),
    description = Some(Description("testMapDescription")),
    inputNodes = List(
      BooleanIoNode(Name("boolDef"), Set(true, false)),
      FloatIoNode(Name("floatDef"), min = -1, max = 1)
    ),
    outputNodes = List(
      IntIoNode(Name("intDef"), min = 0, max = 10),
      ListStrIoNode(Name("listStrDef"), elements = List("a", "b", "c"))
    )
  )

  lazy val testMapInfoResponse = MapInfoResponse(
    testMapInitRequest.name,
    testMapInitRequest.inputNodes.size,
    testMapInitRequest.outputNodes.size,
    numHiddenNodes = 3L
  )

  lazy val testConNodeDef1 = ConcreteNodeDef(Name("conHn1"), Name("ioNode1"), IoIndex(0))
  lazy val testConNodeDef2 = ConcreteNodeDef(Name("conHn2"), Name("ioNode2"), IoIndex(1))
  lazy val testAbsNodeDef1 = AbstractNodeDef(Name("absHn3"))
  lazy val testAbsNodeDef2 = AbstractNodeDef(Name("absHn4"))

  lazy val testMapAddSamplesRequest = MapAddSamplesRequest(
    samples = List(
      NewSampleData(
        probabilityCount = 10,
        utility = 0.5,
        name = Some(Name("sample1")),
        description = Some(Description("Sample 1 description")),
        hiddenNodes = List(testConNodeDef1, testAbsNodeDef1),
        edges = List(NewSampleEdge(testConNodeDef1.name, testAbsNodeDef1.name, EdgeType.THEN))
      ),
      NewSampleData(
        probabilityCount = 20,
        utility = 0.8,
        name = Some(Name("sample2")),
        description = Some(Description("Sample 2 description")),
        hiddenNodes = List(testConNodeDef2, testAbsNodeDef2),
        edges = List(NewSampleEdge(testConNodeDef2.name, testAbsNodeDef2.name, EdgeType.LINK))
      )
    )
  )

  lazy val testMapAddSamplesResponse = MapAddSamplesResponse(
    addedSamples = testMapAddSamplesRequest.samples.zipWithIndex
      .map((data, i) => ShortSampleData(SampleId(i), data.name))
  )
