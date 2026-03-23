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
| created: 2026-03-13 |||||||||||*/

package planning.engine.planner.plan.repr

import cats.MonadThrow
import planning.engine.common.repr.StructureReprBase
import planning.engine.planner.plan.dag.DaGraph

trait DaGraphRepr[F[_]: MonadThrow] extends StructureReprBase[F]:
  self: DaGraph[F] =>

  lazy val repr: F[String] = ???
