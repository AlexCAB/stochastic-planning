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
| created: 2026-01-04 |||||||||||*/

package planning.engine.planner.plan.dag.state

import cats.MonadThrow
import planning.engine.common.values.node.SnId
import planning.engine.planner.map.dcg.nodes.{AbstractDcgNode, ConcreteDcgNode}
import planning.engine.planner.plan.dag.nodes.DagNode

final case class DagState[F[_]: MonadThrow](
    contextConNodes: Map[SnId, DagNode[F, ConcreteDcgNode[F]]],
    contextAbsNodes: Map[SnId, DagNode[F, AbstractDcgNode[F]]],
    planConNodes: Map[SnId, DagNode[F, ConcreteDcgNode[F]]],
    planAbsNodes: Map[SnId, DagNode[F, AbstractDcgNode[F]]],
    forwardLinks: Map[SnId, Set[SnId]],
    backwardLinks: Map[SnId, Set[SnId]],
    forwardThen: Map[SnId, Set[SnId]],
    backwardThen: Map[SnId, Set[SnId]]
)

object DagState:
  def empty[F[_]: MonadThrow]: DagState[F] = DagState(
    contextConNodes = Map.empty,
    contextAbsNodes = Map.empty,
    planConNodes = Map.empty,
    planAbsNodes = Map.empty,
    forwardLinks = Map.empty,
    backwardLinks = Map.empty,
    forwardThen = Map.empty,
    backwardThen = Map.empty
  )
