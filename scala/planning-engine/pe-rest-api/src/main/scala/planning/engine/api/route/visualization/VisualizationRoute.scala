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

package planning.engine.api.route.visualization

import cats.effect.kernel.Sync
import cats.effect.{Concurrent, Resource, Temporal}
import fs2.{Pipe, Stream}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.typelevel.log4cats.LoggerFactory
import planning.engine.api.config.VisualizationRouteConf
import planning.engine.api.model.visualization.MapVisualizationMsg
import planning.engine.api.route.RouteBase
import planning.engine.api.service.visualization.VisualizationServiceLike
import scodec.bits.ByteVector

class VisualizationRoute[F[_]: {Concurrent, Temporal, LoggerFactory}](
    config: VisualizationRouteConf,
    service: VisualizationServiceLike[F]
) extends RouteBase[F] with Http4sDsl[F]:

  import io.circe.syntax.*

  private val pingStream: Stream[F, WebSocketFrame] =
    Stream.awakeEvery[F](config.pingTimeout).map(_ => WebSocketFrame.Ping(ByteVector("ping".getBytes)))

  private val receiveConvert: Pipe[F, WebSocketFrame, String] = _.collect:
    case WebSocketFrame.Text(t, _) => t

  private def sendConvert(s: Stream[F, MapVisualizationMsg]): Stream[F, WebSocketFrame] =
    s.map(d => WebSocketFrame.Text(d.asJson.noSpaces)).merge(pingStream)

  def endpoints(ws: WebSocketBuilder[F]): HttpRoutes[F] = HttpRoutes.of[F]:
    case GET -> Root / "visualization" / "map" =>
      ws.build(sendConvert(service.mapSendWs), service.mapReceiveWs.compose(receiveConvert))

object VisualizationRoute:
  def apply[F[_]: {Sync, Temporal, LoggerFactory}](
      config: VisualizationRouteConf,
      service: VisualizationServiceLike[F]
  ): Resource[F, VisualizationRoute[F]] = Resource.eval(Sync[F].delay(new VisualizationRoute[F](config, service)))
