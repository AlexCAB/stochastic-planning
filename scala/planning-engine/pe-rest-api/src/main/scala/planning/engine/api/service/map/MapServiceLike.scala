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
| created: 2025-12-23 |||||||||||*/

package planning.engine.api.service.map

import planning.engine.api.model.map.*
import planning.engine.common.values.db.DbName
import planning.engine.map.MapGraphLake

trait MapServiceLike[F[_]]:
  def getState: F[Option[(MapGraphLake[F], DbName)]]
  def reset(): F[MapResetResponse]
  def init(request: MapInitRequest): F[MapInfoResponse]
  def load(request: MapLoadRequest): F[MapInfoResponse]
  def addSamples(definition: MapAddSamplesRequest): F[MapAddSamplesResponse]
