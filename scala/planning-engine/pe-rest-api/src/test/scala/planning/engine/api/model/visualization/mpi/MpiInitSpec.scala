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
| created: 2026-10-08 |||||||||||*/

package planning.engine.api.model.visualization.mpi

import cats.effect.IO
import planning.engine.api.model.map.TestMpiData
import planning.engine.common.UnitSpecWithData
import planning.engine.common.values.text.{Description, Name}
import planning.engine.planner.mpi.model.data.map.Metadata

class MpiInitSpec extends UnitSpecWithData:

  private class CaseData extends Case with TestMpiData:
    lazy val metadata: Metadata = Metadata(Name("test-map"), Description.some("Test map description"))

  "MpiInit.apply(...)" should:
    "build init message from map metadata and IO variables" in newCase[CaseData]: (tn, data) =>
      import data.*
      MpiInit[IO](metadata, vars).logValue(tn).asserting(_ mustEqual MpiInit(
        name = metadata.name,
        description = metadata.description,
        inVars = Set(boolVar.name, floatVar.name),
        outVars = Set(intVar.name, listStrVar.name),
      ))
