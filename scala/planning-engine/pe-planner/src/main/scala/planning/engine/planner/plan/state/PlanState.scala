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



package planning.engine.planner.plan.state

import cats.MonadThrow
import planning.engine.planner.plan.dag.DaGraph

final case class PlanState[F[_]: MonadThrow](
    graph: DaGraph[F]
    
    // ??? Also here ia separation of graph to context and plan
                                            )





