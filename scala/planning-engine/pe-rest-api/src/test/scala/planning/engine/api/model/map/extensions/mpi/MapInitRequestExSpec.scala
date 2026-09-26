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
| created: 2026-09-26 |||||||||||*/

package planning.engine.api.model.map.extensions.mpi

import cats.effect.IO
import planning.engine.api.model.map.TestApiData
import planning.engine.api.model.map.extensions.mpi.MapInitRequestEx.*
import planning.engine.api.model.map.payload.*
import planning.engine.common.UnitSpecWithData
import planning.engine.planner.mpi.model.data.map.Metadata
import planning.engine.planner.mpi.model.io.{Type, Variable}

class MapInitRequestExSpec extends UnitSpecWithData with TestApiData:

  private class CaseData extends Case:
    lazy val expectedMetadata = Metadata(
      name = testMapInitRequest.name.getOrElse(fail("Test request must have a name defined")),
      description = testMapInitRequest.description,
    )

    lazy val expectedInVars: Set[Variable.Input] = testMapInitRequest.inputNodes.map:
      case BooleanIoNodeDef(name, acceptableValues) => Variable.Input(name, Type.Bool(acceptableValues))
      case FloatIoNodeDef(name, min, max)           => Variable.Input(name, Type.R(min.toDouble, max.toDouble))
      case n                                        => fail(s"Unexpected node definition: $n")
    .toSet

    lazy val expectedOutVars: Set[Variable.Output] = testMapInitRequest.outputNodes.map:
      case IntIoNodeDef(name, min, max)     => Variable.Output(name, Type.N(min.toLong, max.toLong))
      case ListStrIoNodeDef(name, elements) => Variable.Output(name, Type.Opt(elements))
      case n                                => fail(s"Unexpected node definition: $n")
    .toSet

  "MapInitRequest.metadata" should:
    "convert valid request to metadata" in newCase[CaseData]: (tn, data) =>
      testMapInitRequest.metadata[IO]
        .logValue(tn)
        .asserting(_ mustEqual data.expectedMetadata)

    "fail if request name is not defined" in newCase[CaseData]: (tn, data) =>
      testMapInitRequest.copy(name = None).metadata[IO]
        .logValue(tn)
        .assertThrowsError[AssertionError](_.getMessage must include(
          "MapInitRequest.name must be defined to initialize the MPI planner",
        ))

  "MapInitRequest.inVars" should:
    "convert valid input nodes to Variable.Input instances" in newCase[CaseData]: (tn, data) =>
      testMapInitRequest.inVars[IO]
        .logValue(tn)
        .asserting(_ mustEqual data.expectedInVars)

  "MapInitRequest.outVars" should:
    "convert valid output nodes to Variable.Output instances" in newCase[CaseData]: (tn, data) =>
      testMapInitRequest.outVars[IO]
        .logValue(tn)
        .asserting(_ mustEqual data.expectedOutVars)
