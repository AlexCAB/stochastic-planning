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
| created: 2025-03-15 |||||||||||*/

package planning.engine.core.map.knowledge.graph

import cats.effect.Async
import planning.engine.core.map.io.node.{InputNode, OutputNode}

class KnowledgeGraph[F[_]: Async](
    val metadata: Metadata,
    val inputNodes: Vector[InputNode[F]],
    val outputNodes: Vector[OutputNode[F]]
)
