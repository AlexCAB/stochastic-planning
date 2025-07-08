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
import planning.engine.common.values.text.Name
import planning.engine.map.graph.MapGraphLake

final case class MapInfoResponse(
    mapName: Option[Name],
    numInputNodes: Long,
    numOutputNodes: Long,
    numHiddenNodes: Long
)

object MapInfoResponse:
  def fromKnowledgeGraph[F[_]: {Async, LoggerFactory}](knowledgeGraph: MapGraphLake[F]): F[MapInfoResponse] =
    for
      numHiddenNodes <- knowledgeGraph.countHiddenNodes
      mapName = knowledgeGraph.metadata.name
      numInputNodes = knowledgeGraph.inputNodes.size
      numOutputNodes = knowledgeGraph.outputNodes.size
    yield MapInfoResponse(mapName, numInputNodes, numOutputNodes, numHiddenNodes)
