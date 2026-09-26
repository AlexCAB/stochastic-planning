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
| created: 2026-09-26 |||||||||||*/

package planning.engine.api.model.map.extensions.gsi

import cats.effect.Async
import cats.syntax.all.*
import planning.engine.api.model.map.MapInfoResponse
import planning.engine.common.values.db.DbName
import planning.engine.map.MapGraphLake
import planning.engine.map.io.node.{InputNode, OutputNode}

object MapGraphLakeEx:
  extension [F[_]: Async](graph: MapGraphLake[F])
    def toResponse(dbName: DbName): F[MapInfoResponse] =
      for
        numHiddenNodes <- graph.countHiddenNodes
        mapName = graph.metadata.name
        numInputNodes = graph.ioNodes.values.count(_.isInstanceOf[InputNode[?]])
        numOutputNodes = graph.ioNodes.values.count(_.isInstanceOf[OutputNode[?]])
      yield MapInfoResponse(dbName, mapName, numInputNodes, numOutputNodes, numHiddenNodes)
