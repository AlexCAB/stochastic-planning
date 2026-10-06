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
| created: 2025-12-30 |||||||||||*/

package planning.engine.api.route.visualization

import cats.effect.cps.*
import cats.effect.{IO, Resource}
import fs2.{Pipe, Stream}
import io.circe.parser.*
import org.http4s.Uri
import org.http4s.client.testkit.WSTestClient
import org.http4s.client.websocket.*
import org.http4s.implicits.*
import planning.engine.api.config.parts.VisualizationRouteConf
import planning.engine.api.model.map.TestGsiData
import planning.engine.api.model.visualization.MapVisualizationMsg
import planning.engine.api.service.visualization.VisualizationService
import planning.engine.common.{MockitoWithResource, UnitSpecWithResource}

import scala.concurrent.duration.DurationInt

class VisualizationRouteSpec extends UnitSpecWithResource[(VisualizationService[IO], VisualizationRoute[IO])]
    with MockitoWithResource with TestGsiData:

  override val resource: Resource[IO, (VisualizationService[IO], VisualizationRoute[IO])] =
    for
      stubService <- Resource.pure(mock[VisualizationService[IO]])
      config <- Resource.pure(VisualizationRouteConf(pingTimeout = 5.seconds))
      route <- VisualizationRoute(config, stubService)
    yield (stubService, route)

  "GET /visualization/map" should:
    val testSendStream = Stream.emit[IO, MapVisualizationMsg](testMapVisualizationMsg)

    val testReceiveStream: Pipe[IO, String, Unit] = _.evalMap: msg =>
      for
          _ <- logInfo("Received WebSocket message:", msg)
      yield ()

    def setStubService(service: VisualizationService[IO]): Unit =
      service.mapReceiveWs returns testReceiveStream
      service.mapSendWs returns testSendStream

    def getConnection(route: VisualizationRoute[IO]): Resource[IO, WSConnection[IO]] =
      for
        client <- Resource.eval(WSTestClient.fromHttpWebSocketApp[IO](ws => route.endpoints(ws).orNotFound))
        connection <- client.connect(WSRequest(uri"/visualization/map"))
      yield connection

    "connect to WS and send messages" in: (stubService, route) =>
      async[IO]:
        setStubService(stubService)
        getConnection(route).use(_.send(WSFrame.Text("ping"))).await
        succeed

    "connect to WS and receive messages" in: (stubService, route) =>
      async[IO]:
        setStubService(stubService)

        getConnection(route)
          .use(_.receive.logValue("received value").map:
            case Some(frame: WSFrame.Text) => parse(frame.data).flatMap(_.as[MapVisualizationMsg]) match
                case Right(msg) => msg mustEqual testMapVisualizationMsg
                case Left(err)  => fail(s"Failed to parse MapVisualizationMsg: $err")
            case msg => fail(s"Expected a WebSocketFrame.Text, but got $msg")).await

        succeed
