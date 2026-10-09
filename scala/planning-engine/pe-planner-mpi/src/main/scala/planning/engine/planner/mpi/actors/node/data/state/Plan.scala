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
| created: 30.08.26 |||||||||||||*/

package planning.engine.planner.mpi.actors.node.data.state

import planning.engine.planner.mpi.model.data.node.StepKey
import planning.engine.planner.mpi.repr.Representable

private[node] final case class Plan(
    steps: Map[StepKey, Plan.Step],
    nextPathId: Long,
) extends Representable

private[node] object Plan:

  // Structure (edges) of the plan tree (plan tree is the composition of abstraction tree and sequence tree):
  // - `up` and `bottom`: upper (more abstract) and lower (more concrete) steps in abstraction tree.
  // - `prev` and `next`: previous and next steps in the sequence tree.
  final case class Struct(up: StepKey, bottom: Set[StepKey], prev: Set[StepKey], next: Set[StepKey])

  // Step is the vertex of the plan tree.
  // Plan tree can be decomposed into set of plan paths, where each path is a sequence of steps.
  // Each path is a basically a timeline, which can be split into three parts:
  // - Done steps, is the actions or observations (events) that already happened in the past.
  // - Next step, is the events that expected to happen next (or in the present).
  // - Planned steps, is the events that are possibly (or planned to) happen in the future (i.e. after the next step).
  sealed trait Step:
    def struct: Struct

  final case class Done(struct: Struct) extends Step
  final case class Next(struct: Struct) extends Step
  final case class Planned(struct: Struct) extends Step

  val init = Plan(steps = Map.empty, nextPathId = 1L)
