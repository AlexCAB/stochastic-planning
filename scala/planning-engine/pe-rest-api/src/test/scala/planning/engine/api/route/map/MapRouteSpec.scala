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
| created: 2025-04-28 |||||||||||*/

package planning.engine.api.route.map

import cats.effect.{IO, Resource}
import org.http4s.{Method, Request, Response, Status, Uri}
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.api.service.map.MapServiceLike
import planning.engine.common.UnitSpecWithResource
import planning.engine.api.model.map.*
import cats.effect.cps.*
import org.http4s.implicits.*
import org.http4s.circe.CirceEntityCodec.*
import io.circe.syntax.*

class MapRouteSpec extends UnitSpecWithResource[(MapServiceLike[IO], MapRoute[IO])]
    with AsyncMockFactory with TestApiData:

  override val resource: Resource[IO, (MapServiceLike[IO], MapRoute[IO])] =
    for
      mockService <- Resource.pure(mock[MapServiceLike[IO]])
      route <- MapRoute(mockService)
    yield (mockService, route)

  "POST /map/init" should:
    "return OK and valid response when initialization succeeds" in: (mockService, route) =>
      async[IO]:
        logInfo("POST /map/init", s"Request JSON: ${testMapInitRequest.asJson}").await
        logInfo("POST /map/init", s"Response JSON: ${testMapInfoResponse.asJson}").await

        mockService.init.expects(testMapInitRequest).returns(IO.pure(testMapInfoResponse)).once()

        val request = Request[IO](Method.POST, uri"/map/init").withEntity(testMapInitRequest)
        val response: Response[IO] = route.endpoints.run(request).value
          .logValue("init")
          .await.getOrElse(fail("Expected a response"))

        response.status mustEqual Status.Ok
        response.as[MapInfoResponse].await mustEqual testMapInfoResponse

  "POST /map/load" should:
    "return OK and valid response when loading succeeds" in: (mockService, route) =>
      async[IO]:
        logInfo("POST /map/load", s"Response JSON: ${testMapInfoResponse.asJson}").await

        (() => mockService.load).expects().returns(IO.pure(testMapInfoResponse)).once()

        val request = Request[IO](Method.POST, uri"/map/load")
        val response: Response[IO] = route.endpoints.run(request).value
          .logValue("load")
          .await.getOrElse(fail("Expected a response"))

        response.status mustEqual Status.Ok
        response.as[MapInfoResponse].await mustEqual testMapInfoResponse

  "POST /map/samples" should:
    "return OK and valid response when creating samples is succeeds" in: (mockService, route) =>
      async[IO]:
        logInfo("POST /map/samples", s"Request JSON: ${testMapAddSamplesRequest.asJson}").await
        logInfo("POST /map/samples", s"Response JSON: ${testMapAddSamplesResponse.asJson}").await

        mockService.addSamples.expects(testMapAddSamplesRequest).returns(IO.pure(testMapAddSamplesResponse)).once()

        val request = Request[IO](Method.POST, uri"/map/samples").withEntity(testMapAddSamplesRequest)
        val response: Response[IO] = route.endpoints.run(request).value
          .logValue("samples", "response")
          .await.getOrElse(fail("Expected a response"))

        response.status mustEqual Status.Ok
        response.as[MapAddSamplesResponse].await mustEqual testMapAddSamplesResponse
