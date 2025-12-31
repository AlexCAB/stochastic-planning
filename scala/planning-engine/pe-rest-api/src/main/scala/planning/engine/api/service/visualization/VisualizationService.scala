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
| created: 2025-12-28 |||||||||||*/

package planning.engine.api.service.visualization

import cats.effect.{Async, Resource}
import cats.syntax.all.*
import fs2.{Pipe, Stream}
import fs2.concurrent.Topic
import org.typelevel.log4cats.LoggerFactory
import planning.engine.api.config.VisualizationServiceConf
import planning.engine.api.model.visualization.MapVisualizationMsg
import planning.engine.planner.map.dcg.state.{DcgState, MapInfoState}
import planning.engine.planner.map.visualization.MapVisualizationLike

trait VisualizationServiceLike[F[_]]:
  def mapSendWs: Stream[F, MapVisualizationMsg]
  def mapReceiveWs: Pipe[F, String, Unit]

class VisualizationService[F[_]: {Async, LoggerFactory}](
    config: VisualizationServiceConf,
    topic: Topic[F, (MapInfoState[F], DcgState[F])]
) extends VisualizationServiceLike[F] with MapVisualizationLike[F]:

  private val topicMaxQueued = 1000
  private val logger = LoggerFactory[F].getLogger

  override val mapSendWs: Stream[F, MapVisualizationMsg] = topic
    .subscribe(topicMaxQueued)
    .map((info, state) => MapVisualizationMsg.fromState(info, state))

  override val mapReceiveWs: Pipe[F, String, Unit] =
    in => in.evalMap(frameIn => logger.info("Ping received: " + frameIn))

  override def stateUpdated(info: MapInfoState[F], state: DcgState[F]): F[Unit] =
    if config.mapEnabled then
      for
        res <- topic.publish1((info, state))
        _ <- logger.info(s"Published to visualization topic, res = $res, info = $info, state = $state")
      yield ()
    else Async[F].unit

object VisualizationService:
  private[api] def init[F[_]: {Async, LoggerFactory}](config: VisualizationServiceConf): F[VisualizationService[F]] =
    for
        topic <- Topic[F, (MapInfoState[F], DcgState[F])]
    yield new VisualizationService(config, topic)

  def apply[F[_]: {Async, LoggerFactory}](config: VisualizationServiceConf): Resource[F, VisualizationService[F]] =
    Resource.eval(init(config))
