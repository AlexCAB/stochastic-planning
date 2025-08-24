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

package planning.engine.map.config

import cats.MonadThrow
import cats.effect.Sync
import com.typesafe.config.Config
import neotypes.query.QueryArg.Param
import planning.engine.common.properties.*
import planning.engine.common.values.db.Neo4j.SAMPLES_LABEL
import pureconfig.{ConfigReader, ConfigSource}

// Config do not stored in the database (unlike MapMetadata), but used to initialize the map graph
final case class MapConfig(
    initNextHnId: Long,
    initNextSampleId: Long,
    initSampleCount: Long,
    initNextHnIndex: Long
):
  def toRootParams[F[_]: MonadThrow]: F[Map[String, Param]] = paramsOf(
    PROP.NEXT_HN_ID -> initNextHnId.toDbParam
  )

  def toSamplesParams[F[_]: MonadThrow]: F[Map[String, Param]] = paramsOf(
    PROP.NEXT_SAMPLES_ID -> initNextSampleId.toDbParam,
    PROP.SAMPLES_COUNT -> initSampleCount.toDbParam,
    PROP.NAME -> SAMPLES_LABEL.toDbParam
  )

object MapConfig:
  import pureconfig.generic.semiauto.*
  import pureconfig.module.catseffect.syntax.*
  
  def formConfig[F[_]: Sync](conf: Config): F[MapConfig] =
    given configReader: ConfigReader[MapConfig] = deriveReader[MapConfig]
    ConfigSource.fromConfig(conf).loadF[F, MapConfig]()
