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
| created: 2025-04-23 |||||||||||*/

package planning.engine.api.model.map

import cats.effect.Async
import org.typelevel.log4cats.LoggerFactory
import cats.syntax.all.*
import planning.engine.map.knowledge.graph.KnowledgeGraphLake

final case class MapInfoResponse(
    mapName: Option[String],
    numInputNodes: Long,
    numOutputNodes: Long,
    numHiddenNodes: Long
)

object MapInfoResponse:
  def fromKnowledgeGraph[F[_]: {Async, LoggerFactory}](knowledgeGraph: KnowledgeGraphLake[F]): F[MapInfoResponse] =
    for
      numHiddenNodes <- knowledgeGraph.countHiddenNodes
      mapName = knowledgeGraph.metadata.name.value
      numInputNodes = knowledgeGraph.inputNodes.size
      numOutputNodes = knowledgeGraph.outputNodes.size
    yield MapInfoResponse(mapName, numInputNodes, numOutputNodes, numHiddenNodes)
