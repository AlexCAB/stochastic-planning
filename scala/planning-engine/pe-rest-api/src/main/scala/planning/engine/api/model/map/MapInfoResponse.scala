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
import cats.syntax.all.*
import planning.engine.common.values.text.Name
import planning.engine.map.MapGraphLake
import io.circe.{Decoder, Encoder}
import planning.engine.common.values.db.DbName
import planning.engine.map.io.node.{InputNode, OutputNode}

final case class MapInfoResponse(
    dbName: DbName,
    mapName: Option[Name],
    numInputNodes: Long,
    numOutputNodes: Long,
    numHiddenNodes: Long
)

object MapInfoResponse:
  import io.circe.generic.semiauto.*
  import planning.engine.api.model.values.*

  implicit val decoder: Decoder[MapInfoResponse] = deriveDecoder[MapInfoResponse]
  implicit val encoder: Encoder[MapInfoResponse] = deriveEncoder[MapInfoResponse]

  def fromMapGraph[F[_]: Async](dbName: DbName, knowledgeGraph: MapGraphLake[F]): F[MapInfoResponse] =
    for
      numHiddenNodes <- knowledgeGraph.countHiddenNodes
      mapName = knowledgeGraph.metadata.name
      numInputNodes = knowledgeGraph.ioNodes.values.count(_.isInstanceOf[InputNode[?]])
      numOutputNodes = knowledgeGraph.ioNodes.values.count(_.isInstanceOf[OutputNode[?]])
    yield MapInfoResponse(dbName, mapName, numInputNodes, numOutputNodes, numHiddenNodes)

  def emptyInMem[F[_]: Async](): F[MapInfoResponse] =
    MapInfoResponse(DbName("in-mem"), Name.some("Not implemented for in memory map"), 0L, 0L, 0L).pure[F]
