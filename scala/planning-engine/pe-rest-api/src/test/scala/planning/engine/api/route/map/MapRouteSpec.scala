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

import cats.effect.cps.*
import cats.effect.{IO, Resource}
import io.circe.syntax.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.implicits.*
import org.http4s.{Method, Request, Response, Status, Uri}
import planning.engine.api.model.map.*
import planning.engine.api.service.map.MapServiceLike
import planning.engine.common.{MockitoWithResource, UnitSpecWithResource}

class MapRouteSpec extends UnitSpecWithResource[(MapServiceLike[IO], MapRoute[IO])]
    with MockitoWithResource with TestApiData:

  override val resource: Resource[IO, (MapServiceLike[IO], MapRoute[IO])] =
    for
      mockService <- Resource.pure(mock[MapServiceLike[IO]])
      route <- MapRoute(mockService)
    yield (mockService, route)

  "POST /map/reset" should:
    "return OK and valid response when reset succeeds" in: (mockService, route) =>
      mockService.reset() returns IO.pure(testMapResetResponse)

      async[IO]:
        logInfo("POST /map/reset", s"Response JSON: ${testMapResetResponse.asJson}").await

        val request = Request[IO](Method.POST, uri"/map/reset")
        val response: Response[IO] = route.endpoints.run(request).value
          .logValue("reset")
          .await.getOrElse(fail("Expected a response"))

        mockService.reset() was called
        response.status mustEqual Status.Ok
        response.as[MapResetResponse].await mustEqual testMapResetResponse

  "POST /map/init" should:
    "return OK and valid response when initialization succeeds" in: (mockService, route) =>
      mockService.init(testMapInitRequest) returns IO.pure(testMapInfoResponse)

      async[IO]:
        logInfo("POST /map/init", s"Request JSON: ${testMapInitRequest.asJson}").await
        logInfo("POST /map/init", s"Response JSON: ${testMapInfoResponse.asJson}").await

        val request = Request[IO](Method.POST, uri"/map/init").withEntity(testMapInitRequest)
        val response: Response[IO] = route.endpoints.run(request).value
          .logValue("init")
          .await.getOrElse(fail("Expected a response"))

        mockService.init(testMapInitRequest) was called
        response.status mustEqual Status.Ok
        response.as[MapInfoResponse].await mustEqual testMapInfoResponse

  "POST /map/load" should:
    "return OK and valid response when loading succeeds" in: (mockService, route) =>
      mockService.load(testMapLoadRequest) returns IO.pure(testMapInfoResponse)

      async[IO]:
        logInfo("POST /map/load", s"Request JSON: ${testMapLoadRequest.asJson}").await
        logInfo("POST /map/load", s"Response JSON: ${testMapInfoResponse.asJson}").await

        val request = Request[IO](Method.POST, uri"/map/load").withEntity(testMapLoadRequest)
        val response: Response[IO] = route.endpoints.run(request).value
          .logValue("load")
          .await.getOrElse(fail("Expected a response"))

        mockService.load(testMapLoadRequest) was called
        response.status mustEqual Status.Ok
        response.as[MapInfoResponse].await mustEqual testMapInfoResponse

  "POST /map/samples" should:
    "return OK and valid response when creating samples is succeeds" in: (mockService, route) =>
      mockService.addSamples(testMapAddSamplesRequest) returns IO.pure(testMapAddSamplesResponse)

      async[IO]:
        logInfo("POST /map/samples", s"Request JSON: ${testMapAddSamplesRequest.asJson}").await
        logInfo("POST /map/samples", s"Response JSON: ${testMapAddSamplesResponse.asJson}").await

        val request = Request[IO](Method.POST, uri"/map/samples").withEntity(testMapAddSamplesRequest)
        val response: Response[IO] = route.endpoints.run(request).value
          .logValue("samples", "response")
          .await.getOrElse(fail("Expected a response"))

        mockService.addSamples(testMapAddSamplesRequest) was called
        response.status mustEqual Status.Ok
        response.as[MapAddSamplesResponse].await mustEqual testMapAddSamplesResponse
