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

package planning.engine.planner.mpi.actors.manager.data.parts

import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.model.data.samples.Sample

final case class SampleData(
    props: Sample.Props,
    info: Option[Sample.Info],

    // Nodes that are connected to this sample via edges.
    // This is used for getting full sample data when needed (mostly for visualization).
    nodes: Set[Node],
)
