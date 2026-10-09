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
| created: 2026-10-10 |||||||||||*/

package planning.engine.planner.mpi.model.data.node

import planning.engine.common.values.node.MnId
import planning.engine.common.values.plan.{Depth, PathId}

// StepKey is used to identify the step in Plan Tree.
final case class StepKey(
    // ID of the node where the step is located (belong to) in plan state
    mnId: MnId,

    // Unique ID generated base on path count (local for the node), in the node where it started.
    // Path can start from empty or by split exist one.
    pathId: PathId,

    // Depth of the step in the sequence tree, where 0 is the root.
    depth: Depth,
)
