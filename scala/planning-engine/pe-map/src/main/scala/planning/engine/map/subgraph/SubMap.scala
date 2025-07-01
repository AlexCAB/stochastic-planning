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
| created: 2025-06-17 |||||||||||*/

package planning.engine.map.subgraph

import cats.MonadThrow
import planning.engine.common.values.node.HnId
import planning.engine.common.values.sample.SampleId
import planning.engine.common.values.text.Name
import planning.engine.map.hidden.edge.*
import planning.engine.map.hidden.node.*
import planning.engine.map.io.node.*
import planning.engine.map.samples.sample.SampleData

final case class SubMap[F[_]: MonadThrow](
    inputNodes: Map[Name, InputNode[F]],
    outputNodes: Map[Name, OutputNode[F]],
    concreteNodes: Map[HnId, ConcreteNode[F]],
    abstractNodes: Map[HnId, AbstractNode[F]],
    linkEdges: Map[HnId, List[LinkEdge[F]]],
    thenEdges: Map[HnId, List[ThenEdge[F]]],
    samples: Map[SampleId, SampleData]
)
