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
| created: 04.07.2026 |||||||||||*/



package planning.engine.planner.mpi.data.edge

import cats.effect.IO
import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit
import planning.engine.planner.mpi.test.data.MapEdgeTestData

class EdgeDataSpec extends UnitSpecWithIOAndTestKit:
  private class CaseData extends Case with MapEdgeTestData

  "EdgeData.join(...)" should:
    "join two EdgeData instances" in newCase[CaseData]: (_, data) =>
      import data.*
      edgeData1.join[IO](edgeData2).map(_.indexies mustBe (edgeData1.indexies ++ edgeData2.indexies))

    "fail when joining EdgeData instances with duplicate sample IDs" in newCase[CaseData]: (_, data) =>
      import data.*
      edgeData1.join[IO](edgeData1).assertThrows[AssertionError]

