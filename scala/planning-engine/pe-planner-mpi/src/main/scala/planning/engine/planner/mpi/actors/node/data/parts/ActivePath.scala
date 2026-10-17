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
| created: 2026-10-15 |||||||||||*/

package planning.engine.planner.mpi.actors.node.data.parts

import cats.syntax.all.*
import cats.syntax.ext.MT
import planning.engine.common.errors.*
import planning.engine.common.graph.inference.PU
import planning.engine.common.values.node.MnId
import planning.engine.common.values.plan.HuangWeight
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.model.data.node.StepKey

// Used as accumulator for active path during the active tree traversal.
final case class ActivePath(
    // Path from leaf (which is concrete node) node to the root node of active tree.
    // Contain only LINK edges.
    linkPath: List[MnId],

    // Collected active nodes along the path, with their corresponding step keys.
    activeNodes: Map[Node, Set[StepKey]],

    // Evaluated P(N) * U(N), probability and utility
    pu: PU,

    // Evaluated Huang weight.
    weight: HuangWeight,
):
  def append[F[_]: MT](node: Node, steps: Set[StepKey], pu: PU): F[ActivePath] =
    for
        _ <- linkPath.assertNotContain(node.mnId, s"Node ${node.mnId} already exists in the link path: $linkPath")
    yield copy(
      linkPath = node.mnId :: linkPath,
      activeNodes = activeNodes + (node -> steps),
      pu = pu,
    )

  def withNextWeight(weight: HuangWeight): ActivePath = copy(weight = weight)

object ActivePath:
  def init(leaf: Node, steps: Set[StepKey], pu: PU): ActivePath = ActivePath(
    linkPath = List(leaf.mnId),
    activeNodes = Map(leaf -> steps),
    pu = pu,
    weight = HuangWeight.init,
  )
