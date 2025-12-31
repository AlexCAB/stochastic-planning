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

import cats.effect.{IO, Resource}
import fs2.{Pipe, Stream}
import org.http4s.Uri
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.api.model.map.TestApiData
import planning.engine.api.service.visualization.VisualizationServiceLike
import planning.engine.common.UnitSpecWithResource
import cats.effect.cps.*
import org.http4s.client.testkit.WSTestClient
import org.http4s.client.websocket.WSRequest
import org.http4s.implicits.*
import planning.engine.api.model.visualization.MapVisualizationMsg
import org.http4s.client.websocket.*
import io.circe.parser.*
import planning.engine.api.config.VisualizationRouteConf

import scala.concurrent.duration.DurationInt

class VisualizationRouteSpec extends UnitSpecWithResource[(VisualizationServiceLike[IO], VisualizationRoute[IO])]
    with AsyncMockFactory with TestApiData:

  override val resource: Resource[IO, (VisualizationServiceLike[IO], VisualizationRoute[IO])] =
    for
      stubService <- Resource.pure(stub[VisualizationServiceLike[IO]])
      config <- Resource.pure(VisualizationRouteConf(pingTimeout = 5.seconds))
      route <- VisualizationRoute(config, stubService)
    yield (stubService, route)

  "GET /visualization/map" should:
    val testSendStream = Stream.emit[IO, MapVisualizationMsg](testMapVisualizationMsg)

    val testReceiveStream: Pipe[IO, String, Unit] = _.evalMap: msg =>
      for
          _ <- logInfo("Received WebSocket message:", msg)
      yield ()

    def setStubService(service: VisualizationServiceLike[IO]): Unit =
      (() => service.mapReceiveWs).when().returns(testReceiveStream).once()
      (() => service.mapSendWs).when().returns(testSendStream).once()

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
