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
import planning.engine.common.UnitSpecWithData
import planning.engine.common.enums.EdgeType
import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.{HnId, MnId}

class MpiNewEdgesSpec extends UnitSpecWithData:

  private class CaseData extends Case:
    lazy val conId: MnId.Con = MnId.Con(1L)
    lazy val absId: MnId.Abs = MnId.Abs(2L)

  "MpiNewEdges.formMeKeys(...)" should:
    "convert edge keys to (source, target, edge type) tuples" in newCase[CaseData]: (tn, data) =>
      import data.*
      MpiNewEdges.formMeKeys[IO](Set(MeKey.Link(conId, absId), MeKey.Then(absId, conId))).logValue(tn)
        .asserting(_ mustEqual MpiNewEdges(Set(
          (HnId(1L), HnId(2L), EdgeType.LINK),
          (HnId(2L), HnId(1L), EdgeType.THEN),
        )))
