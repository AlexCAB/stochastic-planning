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
| created: 2026-10-06 |||||||||||*/

package planning.engine.api.model.map

import planning.engine.api.model.map.payload.NewSampleEdge
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnName, MnId}
import planning.engine.planner.mpi.model.data.samples.Sample
import planning.engine.planner.mpi.model.io.{IoVars, Type, Variable}

trait TestMpiData extends TestApiData:
  lazy val boolVar = Variable
    .Input(testConNodeBoolDef.ioNodeName, Type.Bool(booleanIoNodeDef.acceptableValues))

  lazy val floatVar = Variable
    .Input(testConNodeFloatDef.ioNodeName, Type.R(floatIoNodeDef.min.toDouble, floatIoNodeDef.max.toDouble))

  lazy val intVar = Variable
    .Output(testConNodeIntDef.ioNodeName, Type.N(intIoNodeDef.min.toLong, intIoNodeDef.max.toLong))

  lazy val listStrVar = Variable
    .Output(testConNodeListStrDef.ioNodeName, Type.Opt(listStrIoNodeDef.elements))

  lazy val vars = new IoVars(
    in = Map(boolVar.name -> boolVar, floatVar.name -> floatVar),
    out = Map(intVar.name -> intVar, listStrVar.name -> listStrVar),
  )

  lazy val mnIds: Map[HnName, MnId] = Map(
    testConNodeBoolDef.name -> MnId.Con(101L),
    testAbsNodeDef1.name -> MnId.Abs(102L),
  )

  lazy val testEdge: NewSampleEdge = testNewSampleData.edges.head

  lazy val sampleMan = Sample.Man(
    props = Sample.Props(testNewSampleData.probabilityCount, testNewSampleData.utility),
    info = Sample.Info(testNewSampleData.name.get, testNewSampleData.description),
    edges = Set(MeKey(testEdge.edgeType, mnIds(testEdge.sourceHnName), mnIds(testEdge.targetHnName))),
  )
