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
| created: 2025-12-06 |||||||||||*/

package planning.engine.map.subgraph

import cats.MonadThrow
import planning.engine.common.values.io.IoValue
import planning.engine.map.hidden.edge.HiddenEdge
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.samples.sample.SampleData
import planning.engine.common.values.sample.SampleId

final case class MapSubGraph[F[_]: MonadThrow](
    concreteNodes: List[ConcreteNode[F]],
    abstractNodes: List[AbstractNode[F]],
    edges: List[HiddenEdge],
    loadedSamples: List[SampleData],
    skippedSamples: List[SampleId]
):
  lazy val allIoValues: Set[IoValue] = concreteNodes.map(_.ioValue).toSet
  lazy val allSampleId: Set[SampleId] = loadedSamples.map(_.id).toSet ++ skippedSamples.toSet
