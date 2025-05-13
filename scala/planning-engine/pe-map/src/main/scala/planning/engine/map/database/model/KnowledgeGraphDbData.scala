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
| created: 2025-05-11 |||||||||||*/

package planning.engine.map.database.model

import planning.engine.map.io.node.{InputNode, OutputNode}
import planning.engine.map.knowledge.graph.{KnowledgeGraphState, Metadata}
import planning.engine.map.samples.SamplesState

final case class KnowledgeGraphDbData[F[_]](
    metadata: Metadata,
    inNodes: Vector[InputNode[F]],
    outNodes: Vector[OutputNode[F]],
    samplesState: SamplesState,
    graphState: KnowledgeGraphState
)
