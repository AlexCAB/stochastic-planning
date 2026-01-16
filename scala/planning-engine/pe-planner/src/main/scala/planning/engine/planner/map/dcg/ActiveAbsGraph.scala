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
| created: 2026-01-11 |||||||||||*/

package planning.engine.planner.map.dcg

import cats.MonadThrow
import planning.engine.common.values.node.HnId
import planning.engine.planner.map.dcg.DcgGraph

// Is DAG where leafs are concrete hidden nodes and rest of the tree is abstract hidden nodes.
// Edges with type LINK pointed form higher abstract root nodes to concrete leaf nodes.
// Also include THEN edges to previous nodes.
final case class ActiveAbsGraph[F[_]: MonadThrow](
    prevConNodes: Set[HnId],
    prevAbsNodes: Set[HnId],
    graph: DcgGraph[F]
)
