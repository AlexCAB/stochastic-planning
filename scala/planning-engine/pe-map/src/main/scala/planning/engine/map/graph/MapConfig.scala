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
| created: 2025-05-18 |||||||||||*/

package planning.engine.map.graph

import cats.MonadThrow
import com.typesafe.config.Config
import cats.effect.Sync
import neotypes.query.QueryArg.Param
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.semiauto.*
import pureconfig.module.catseffect.syntax.*
import planning.engine.common.properties.*

// Config do not stored in the database (unlike MapMetadata), but used to initialize the map graph
final case class MapConfig(
    initNextHnId: Long,
    initNextSampleId: Long,
    initSampleCount: Long,
    initNextHnIndex: Long,
    samplesName: String
):
  def toRootParams[F[_]: MonadThrow]: F[Map[String, Param]] = paramsOf(
    PROP.NEXT_HN_ID -> initNextHnId.toDbParam
  )

  def toSamplesParams[F[_]: MonadThrow]: F[Map[String, Param]] = paramsOf(
    PROP.NEXT_SAMPLES_ID -> initNextSampleId.toDbParam,
    PROP.SAMPLES_COUNT -> initSampleCount.toDbParam,
    PROP.NAME -> samplesName.toDbParam
  )

object MapConfig:
  def formConfig[F[_]: Sync](conf: Config): F[MapConfig] =
    given configReader: ConfigReader[MapConfig] = deriveReader[MapConfig]
    ConfigSource.fromConfig(conf).loadF[F, MapConfig]()
