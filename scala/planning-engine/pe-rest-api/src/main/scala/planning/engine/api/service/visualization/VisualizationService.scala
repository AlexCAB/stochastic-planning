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

import cats.Applicative
import cats.effect.{Async, Resource}
import cats.syntax.all.*
import fs2.{Pipe, Stream}
import org.typelevel.log4cats.LoggerFactory
import planning.engine.api.model.visualization.MapVisualizationMsg

import scala.concurrent.duration.DurationInt

trait VisualizationServiceLike[F[_]]:
  def mapSendWs: Stream[F, MapVisualizationMsg]
  def mapReceiveWs: Pipe[F, String, Unit]

class VisualizationService[F[_]: {Async, LoggerFactory}]() extends VisualizationServiceLike[F]:
  private val logger = LoggerFactory[F].getLogger

  override val mapSendWs: Stream[F, MapVisualizationMsg] =
    
    Stream.awakeEvery[F](1.second).evalMap(_ => Applicative[F].pure(MapVisualizationMsg("ok")))
    

  override val mapReceiveWs: Pipe[F, String, Unit] =
    in => in.evalMap(frameIn => logger.info("Ping received: " + frameIn))

object VisualizationService:
  def apply[F[_]: {Async, LoggerFactory}](): Resource[F, VisualizationService[F]] =
    Resource.eval(new VisualizationService().pure)
