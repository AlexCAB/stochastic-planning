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

package planning.engine.planner.mpi.actors.node.data.state.plan

import planning.engine.planner.mpi.model.data.node.StepKey

// Structure (edges) of the plan tree (plan tree is the composition of abstraction tree and sequence tree):
// - `up` and `bottom`: upper (more abstract) and lower (more concrete) steps in abstraction tree.
// - `prev` and `next`: previous and next steps in the sequence tree.
private[node] final case class Struct(
    up: Option[StepKey],
    bottom: Set[StepKey],
    prev: Option[StepKey],
    next: Set[StepKey],
)

private[node] object Struct:
  val init: Struct = Struct(up = None, bottom = Set.empty, prev = None, next = Set.empty)
