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

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import io.circe.Json
import planning.engine.api.model.map.payload.*
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.db.DbName
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.{Description, Name}
import planning.engine.map.graph.MapConfig
import planning.engine.map.hidden.node.ConcreteNode
import planning.engine.map.io.node.{InputNode, OutputNode}
import planning.engine.map.io.variable.*

trait TestApiData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  lazy val testConfig: MapConfig = MapConfig(
    initNextHnId = 100L,
    initNextSampleId = 200L,
    initSampleCount = 300L,
    initNextHnIndex = 400L,
    samplesName = "samples"
  )

  lazy val testDbName = DbName("testMapDb")

  lazy val testMapResetResponse = MapResetResponse(
    prevDbName = Some(testDbName),
    prevMapName = Some(Name("testMapName"))
  )

  lazy val booleanIoNodeDef = BooleanIoNodeDef(Name("boolDef"), Set(true, false))
  lazy val floatIoNodeDef = FloatIoNodeDef(Name("floatDef"), min = -1, max = 1)
  lazy val intIoNodeDef = IntIoNodeDef(Name("intDef"), min = 0, max = 10)
  lazy val listStrIoNodeDef = ListStrIoNodeDef(Name("listStrDef"), elements = List("a", "b", "c"))

  lazy val testMapInitRequest = MapInitRequest(
    dbName = testDbName,
    name = Some(Name("testMapName")),
    description = Some(Description("testMapDescription")),
    inputNodes = List(booleanIoNodeDef, floatIoNodeDef),
    outputNodes = List(intIoNodeDef, listStrIoNodeDef)
  )

  lazy val testMapLoadRequest = MapLoadRequest(dbName = testDbName)

  lazy val testMapInfoResponse = MapInfoResponse(
    testDbName,
    testMapInitRequest.name,
    testMapInitRequest.inputNodes.size,
    testMapInitRequest.outputNodes.size,
    numHiddenNodes = 3L
  )

  lazy val testConNodeVal1 = true
  lazy val testConNodeVal2 = "a"

  lazy val booleanIoVar = BooleanIoVariable[IO](booleanIoNodeDef.acceptableValues)
  lazy val floatIoVar = FloatIoVariable[IO](floatIoNodeDef.min, floatIoNodeDef.max)
  lazy val intIoVar = IntIoVariable[IO](intIoNodeDef.min, intIoNodeDef.max)
  lazy val listStrIoVar = ListStrIoVariable[IO](listStrIoNodeDef.elements)

  lazy val booleanIoNode = InputNode(Name("ioNode1"), booleanIoVar)
  lazy val floatIoNode = InputNode(Name("ioNode2"), floatIoVar)
  lazy val intIoNode = OutputNode(Name("ioNode3"), intIoVar)
  lazy val listStrIoNode = OutputNode(Name("ioNode4"), listStrIoVar)

  lazy val ioNodes = Map(
    booleanIoNode.name -> booleanIoNode,
    floatIoNode.name -> floatIoNode,
    intIoNode.name -> intIoNode,
    listStrIoNode.name -> listStrIoNode
  )

  lazy val testConNodeDef1 = ConcreteNodeDef(Name("conHn1"), Some(Description("testConNodeDef1")), booleanIoNode.name, Json.fromBoolean(testConNodeVal1))
  lazy val testConNodeDef2 = ConcreteNodeDef(Name("conHn2"), Some(Description("testConNodeDef2")), listStrIoNode.name, Json.fromString(testConNodeVal2))
  lazy val testAbsNodeDef1 = AbstractNodeDef(Name("absHn3"), Some(Description("testAbsNodeDef1")))
  lazy val testAbsNodeDef2 = AbstractNodeDef(Name("absHn4"), Some(Description("testAbsNodeDef2")))

  lazy val testConNodeNew1 = ConcreteNode.New(
    Some(testConNodeDef1.name),
    testConNodeDef1.description,
    testConNodeDef1.ioNodeName,
    booleanIoVar.indexForValue(testConNodeVal1).unsafeRunSync()
  )

  lazy val testConNodeNew2 = ConcreteNode.New(
    Some(testConNodeDef2.name),
    testConNodeDef2.description,
    testConNodeDef2.ioNodeName,
    listStrIoVar.indexForValue(testConNodeVal2).unsafeRunSync()
  )

  lazy val testMapAddSamplesRequest = MapAddSamplesRequest(
    samples = List(
      NewSampleData(
        probabilityCount = 10,
        utility = 0.5,
        name = Some(Name("sample1")),
        description = Some(Description("Sample 1 description")),
        edges = List(NewSampleEdge(testConNodeDef1.name, testAbsNodeDef1.name, EdgeType.THEN))
      ),
      NewSampleData(
        probabilityCount = 20,
        utility = 0.8,
        name = Some(Name("sample2")),
        description = Some(Description("Sample 2 description")),
        edges = List(NewSampleEdge(testConNodeDef2.name, testAbsNodeDef2.name, EdgeType.LINK))
      )
    ),
    hiddenNodes = List(testConNodeDef1, testAbsNodeDef1, testConNodeDef2, testAbsNodeDef2)
  )

  lazy val testMapAddSamplesResponse = MapAddSamplesResponse(
    addedSamples = testMapAddSamplesRequest.samples.zipWithIndex
      .map((data, i) => ShortSampleData(SampleId(i), data.name))
  )
