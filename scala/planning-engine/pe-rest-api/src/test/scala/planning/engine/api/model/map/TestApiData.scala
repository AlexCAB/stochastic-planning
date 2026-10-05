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

import cats.effect.unsafe.IORuntime
import io.circe.Json
import planning.engine.api.model.map.payload.*
import planning.engine.common.enums.EdgeType
import planning.engine.common.values.db.DbName
import planning.engine.common.values.io.IoName
import planning.engine.common.values.node.HnName
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.{Description, Name}
import planning.engine.map.config.MapConfig

trait TestApiData:
  private implicit lazy val ioRuntime: IORuntime = IORuntime.global

  lazy val testConfig: MapConfig = MapConfig(
    initNextHnId = 100L,
    initNextSampleId = 200L,
    initSampleCount = 300L,
    initNextHnIndex = 400L,
  )

  lazy val testDbName = DbName("testMapDb")

  lazy val testMapResetResponse = MapResetResponse(
    prevDbName = Some(testDbName),
    prevMapName = Name.some("testMapName"),
  )

  lazy val booleanIoNodeDef = BooleanIoNodeDef(IoName("boolDef"), Set(true, false))
  lazy val floatIoNodeDef = FloatIoNodeDef(IoName("floatDef"), min = -1, max = 1)
  lazy val intIoNodeDef = IntIoNodeDef(IoName("intDef"), min = 0, max = 10)
  lazy val listStrIoNodeDef = ListStrIoNodeDef(IoName("listStrDef"), elements = List("a", "b", "c"))

  lazy val testMapInitRequest = MapInitRequest(
    dbName = testDbName,
    name = Name.some("testMapName"),
    description = Description.some("testMapDescription"),
    inputNodes = List(booleanIoNodeDef, floatIoNodeDef),
    outputNodes = List(intIoNodeDef, listStrIoNodeDef),
  )

  lazy val testMapLoadRequest = MapLoadRequest(dbName = testDbName)

  lazy val testMapInfoResponse = MapInfoResponse(
    testDbName,
    testMapInitRequest.name,
    testMapInitRequest.inputNodes.size,
    testMapInitRequest.outputNodes.size,
    numHiddenNodes = 3L,
  )

  lazy val testConNodeBoolVal = true
  lazy val testConNodeFloatVal = 0.5
  lazy val testConNodeIntVal = 5L
  lazy val testConNodeListStrVal = "a"

  lazy val testConNodeBoolDef = ConcreteNodeDef(
    HnName("conHnBool"),
    Description.some("testConNodeBoolDef"),
    booleanIoNodeDef.name,
    Json.fromBoolean(testConNodeBoolVal),
  )

  lazy val testConNodeFloatDef = ConcreteNodeDef(
    HnName("conHnFloat"),
    Description.some("testConNodeFloatDef"),
    floatIoNodeDef.name,
    Json.fromDoubleOrNull(testConNodeFloatVal),
  )

  lazy val testConNodeIntDef = ConcreteNodeDef(
    HnName("conHnInt"),
    Description.some("testConNodeIntDef"),
    intIoNodeDef.name,
    Json.fromLong(testConNodeIntVal),
  )

  lazy val testConNodeListStrDef = ConcreteNodeDef(
    HnName("conHnListStr"),
    Description.some("testConNodeListStrDef"),
    listStrIoNodeDef.name,
    Json.fromString(testConNodeListStrVal),
  )

  lazy val testAbsNodeDef1 = AbstractNodeDef(HnName("absHn3"), Description.some("testAbsNodeDef1"))
  lazy val testAbsNodeDef2 = AbstractNodeDef(HnName("absHn4"), Description.some("testAbsNodeDef2"))

  lazy val testNewSampleData: NewSampleData = NewSampleData(
    probabilityCount = 10,
    utility = 0.5,
    name = Name.some("sample1"),
    description = Description.some("Sample 1 description"),
    edges = List(NewSampleEdge(testConNodeBoolDef.name, testAbsNodeDef1.name, EdgeType.THEN)),
  )

  lazy val testMapAddSamplesRequest = MapAddSamplesRequest(
    samples = List(
      testNewSampleData,
      NewSampleData(
        probabilityCount = 20,
        utility = 0.8,
        name = Name.some("sample2"),
        description = Description.some("Sample 2 description"),
        edges = List(NewSampleEdge(testConNodeListStrDef.name, testAbsNodeDef2.name, EdgeType.LINK)),
      ),
    ),
    hiddenNodes = List(testConNodeBoolDef, testAbsNodeDef1, testConNodeListStrDef, testAbsNodeDef2),
  )

  lazy val testMapAddSamplesResponse = MapAddSamplesResponse(
    addedSamples = testMapAddSamplesRequest.samples.zipWithIndex
      .map((data, i) => ShortSampleData(SampleId(i), data.name)),
  )

  lazy val testResponse = MapAddSamplesResponse(
    testMapAddSamplesRequest.samples.zipWithIndex.map((data, i) => ShortSampleData(SampleId(i + 1), data.name)),
  )
