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
| created: 2025-08-20 |||||||||||*/

package planning.engine.planner.plan

import cats.effect.Async
import planning.engine.planner.dag.PlanningDagLike

trait PlanningSimpleLike[F[_]: Async]

final class PlanSimple[F[_]: Async](planningDag: PlanningDagLike[F]) extends PlanningSimpleLike[F]
