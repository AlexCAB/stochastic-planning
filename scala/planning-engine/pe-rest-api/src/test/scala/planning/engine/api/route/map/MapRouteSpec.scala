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
import org.http4s.{Method, Request, Status, Uri}
import org.scalamock.scalatest.AsyncMockFactory
import org.typelevel.log4cats.Logger
import planning.engine.api.service.map.MapServiceLike
import planning.engine.common.UnitSpecWithResource
import planning.engine.api.model.map.*
import cats.effect.cps.*
import org.http4s.implicits.*
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import io.circe.syntax.*
import planning.engine.api.model.map.payload.*
import planning.engine.common.values.text.{Name, Description}
import planning.engine.api.model.values.*

class MapRouteSpec extends UnitSpecWithResource[(MapServiceLike[IO], MapRoute[IO])] with AsyncMockFactory:
  private val testMapInitRequest = MapInitRequest(
    name = Some(Name("testMapName")),
    description = Some(Description("testMapDescription")),
    inputNodes = List(
      BooleanIoNode(Name("boolDef"), Set(true, false)),
      FloatIoNode(Name("floatDef"), min = -1, max = 1)
    ),
    outputNodes = List(
      IntIoNode(Name("intDef"), min = 0, max = 10),
      ListStrIoNode(Name("listStrDef"), elements = List("a", "b", "c"))
    )
  )

  private val expectedResponse = MapInfoResponse(
    testMapInitRequest.name,
    testMapInitRequest.inputNodes.size,
    testMapInitRequest.outputNodes.size,
    numHiddenNodes = 3L
  )

  override val resource: Resource[IO, (MapServiceLike[IO], MapRoute[IO])] =
    for
      mockService <- Resource.pure(mock[MapServiceLike[IO]])
      route <- MapRoute(mockService)
    yield (mockService, route)

  "POST /map/init" should:
    "return OK and valid response when initialization succeeds" in: (mockService, route) =>
      async[IO]:
        Logger[IO].info(s"Test request JSON: ${testMapInitRequest.asJson}").await
        Logger[IO].info(s"Expected response JSON: ${expectedResponse.asJson}").await

        mockService.init.expects(testMapInitRequest).returns(IO.pure(expectedResponse)).once()

        val request = Request[IO](Method.POST, uri"/map/init").withEntity(testMapInitRequest)
        val response = route.endpoints.run(request).value
          .logValue("init")
          .await.getOrElse(fail("Expected a response"))

        response.status mustEqual Status.Ok
        response.as[MapInfoResponse].await mustEqual expectedResponse

  "POST /map/load" should:
    "return OK and valid response when loading succeeds" in: (mockService, route) =>
      async[IO]:
        (() => mockService.load).expects().returns(IO.pure(expectedResponse)).once()

        val request = Request[IO](Method.POST, uri"/map/load")
        val response = route.endpoints.run(request).value
          .logValue("load")
          .await.getOrElse(fail("Expected a response"))

        response.status mustEqual Status.Ok
        response.as[MapInfoResponse].await mustEqual expectedResponse
