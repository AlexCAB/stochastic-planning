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
import planning.engine.common.graph.inference.PU
import planning.engine.common.values.node.MnId
import planning.engine.common.values.plan.HuangWeight
import planning.engine.planner.mpi.actors.node.data.parts.ActivePath
import planning.engine.planner.mpi.model.data.visualization.ActiveForest

private[actors] final case class ActiveSteps(
    weightAcc: Map[MnId, HuangWeight],
    paths: Set[ActivePath],
):
  def addPath[F[_]: MT](path: ActivePath): F[ActiveSteps] =
    for
      _ <- weightAcc.keySet.assertContain(path.root, s"Path root of $path is not in the weight accumulator: $weightAcc")
      _ <- paths.assertNotContain(path, s"Path $path already exists in the active paths: $paths")
      newWeight <- weightAcc.get(path.root).map(_.add(path.weight)).getOrElse(path.weight.pure)
    yield copy(
      weightAcc = weightAcc + (path.root -> newWeight),
      paths = paths + path,
    )

  def isBuilt: Boolean = weightAcc.values.forall(_.isFull)

  lazy val nodes: Map[MnId, PU] = paths.flatMap(_.nodes).toMap
  lazy val roots: Set[MnId] = paths.map(_.root)
  lazy val edges: Set[(MnId, MnId)] = paths.flatMap(_.edges)

  lazy val activeForest: ActiveForest = ActiveForest(nodes, roots, edges)

private[actors] object ActiveSteps:
  val empty: ActiveSteps = ActiveSteps(Map.empty, Set.empty)

  def apply(mnIds: Set[MnId]): ActiveSteps = ActiveSteps(mnIds.map(_ -> HuangWeight.zero).toMap, Set.empty)
