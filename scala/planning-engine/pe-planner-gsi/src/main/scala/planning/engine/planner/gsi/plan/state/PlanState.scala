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
| created: 2026-03-12 |||||||||||*/

package planning.engine.planner.gsi.plan.state

import cats.syntax.ext.MT
import planning.engine.planner.gsi.plan.dag.DaGraph

final case class PlanState[F[_]: MT](
    graph: DaGraph[F],

    // ??? Also here ia separation of graph to context and plan
)
