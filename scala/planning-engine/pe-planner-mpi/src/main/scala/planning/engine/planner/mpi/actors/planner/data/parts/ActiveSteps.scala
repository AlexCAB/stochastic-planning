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
| created: 2026-10-18 |||||||||||*/

package planning.engine.planner.mpi.actors.planner.data.parts

import cats.syntax.all.*
import cats.syntax.ext.MT
import planning.engine.common.errors.*
import planning.engine.common.values.node.MnId
import planning.engine.common.values.plan.HuangWeight
import planning.engine.planner.mpi.actors.node.data.parts.ActivePath

private[planner] final case class ActiveSteps(
    weightAcc: Map[MnId, HuangWeight],
    paths: Set[ActivePath],
):
  def addPath[F[_]: MT](path: ActivePath): F[ActiveSteps] =
    for
      _ <- weightAcc.assertContain(path.pathRoot, s"Path root of $path is not in the weight accumulator: $weightAcc")
      newWeight <- weightAcc.get(path.pathRoot).map(_.add(path.weight)).getOrElse(path.weight.pure)
      _ <- paths.assertContain(path, s"Path $path already exists in the active paths: $paths")
    yield copy(
      weightAcc = weightAcc + (path.pathRoot -> newWeight),
      paths = paths + path,
    )

  def isBuilt: Boolean = weightAcc.values.forall(_.isFull)

object ActiveSteps:
  val empty: ActiveSteps = ActiveSteps(Map.empty, Set.empty)

  def apply(mnIds: Set[MnId]): ActiveSteps = ActiveSteps(mnIds.map(_ -> HuangWeight.zero).toMap, Set.empty)
