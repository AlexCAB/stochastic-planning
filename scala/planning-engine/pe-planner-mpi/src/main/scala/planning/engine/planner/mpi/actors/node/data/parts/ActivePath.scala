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
import planning.engine.planner.mpi.actors.node.data.parts.ActivePath.ActiveNode
import planning.engine.planner.mpi.model.data.node.StepKey

// Used as accumulator for active path during the active tree traversal.
private[actors] final case class ActivePath(
    // Path from leaf (which is concrete node) node to the root node of active tree.
    // Contain only LINK edges.
    root: MnId.Con,
    path: List[MnId.Abs],

    // Collected active nodes along the path, with their corresponding step keys.
    activeNodes: Map[MnId, ActiveNode],

    // Evaluated Huang weight.
    weight: HuangWeight,
):
  def append[F[_]: MT](node: Node.Abs, steps: Set[StepKey], pu: PU): F[ActivePath] =
    for
      // Activation forest can't have cycles, so we must ensure that the node is not already in the link path.
      _ <- path.assertNotContain(node.mnId, s"Node ${node.mnId} already exists in the link path: $path")
      _ <- activeNodes.keySet.assertNotContain(node.mnId, s"Node ${node.mnId} already exists in the active nodes")
    yield copy(
      path = node.mnId :: path,
      activeNodes = activeNodes + (node.mnId -> ActiveNode(node, pu, steps)),
    )

  def withNextWeight(weight: HuangWeight): ActivePath = copy(weight = weight)

  lazy val nodes: Map[MnId, PU] = activeNodes.view.mapValues(v => v.pu).toMap

  // Splits the path into edges, where each edge is a pair of MnId (from, to).
  lazy val edges: Set[(MnId, MnId)] =
    val chain: List[MnId] = root :: path.reverse
    chain.zip(chain.tail).toSet

private[actors] object ActivePath:
  final case class ActiveNode(node: Node, pu: PU, steps: Set[StepKey])

  def init(leaf: Node.Con, steps: Set[StepKey], pu: PU): ActivePath = ActivePath(
    root = leaf.mnId,
    path = List(),
    activeNodes = Map(leaf.mnId -> ActiveNode(leaf, pu, steps)),
    weight = HuangWeight.init,
  )
