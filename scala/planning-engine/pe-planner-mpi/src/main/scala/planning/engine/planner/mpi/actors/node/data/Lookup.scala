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
| created: 2026-10-11 |||||||||||*/

package planning.engine.planner.mpi.actors.node.data

import planning.engine.common.values.sample.SampleId
import planning.engine.planner.mpi.actors.node.Node

final case class Lookup(
    inLink: Map[SampleId, Set[Node]],
    outLink: Map[SampleId, Set[Node]],
    inThen: Map[SampleId, Set[Node]],
    outThen: Map[SampleId, Set[Node]],
)
