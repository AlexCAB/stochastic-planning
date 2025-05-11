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
| created: 2025-04-22 |||||||||||*/

package planning.engine.api.route.maintenance

import cats.effect.{IO, Resource}
import org.http4s.{Request, Status}
import planning.engine.common.UnitSpecWithResource
import planning.engine.api.model.maintenance.HealthResponse
import planning.engine.api.model.enums.Status as HealthStatus
import org.scalamock.scalatest.AsyncMockFactory
import planning.engine.api.service.maintenance.MaintenanceServiceLike

import cats.effect.cps.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*

type ServiceWithRoute = (MaintenanceServiceLike[IO], MaintenanceRoute[IO])

class MaintenanceRouteSpec extends UnitSpecWithResource[ServiceWithRoute] with AsyncMockFactory:

  val testHealthResponse: HealthResponse = HealthResponse(HealthStatus.OK, "1.0.0")

  override val resource: Resource[IO, ServiceWithRoute] =
    for
      service <- Resource.pure(mock[MaintenanceServiceLike[IO]])
      route <- MaintenanceRoute[IO](service)
    yield (service, route)

  "GET /maintenance/__health" should:
    "return OK status and health response with version" in: (mockedService, route) =>
      import io.circe.generic.auto.*
      import org.http4s.circe.CirceEntityCodec.*

      (() => mockedService.getHealth).expects().returns(IO.pure(testHealthResponse)).once()
      val request = Request[IO](method = GET, uri = uri"/maintenance/__health")

      async[IO]:
        route.endpoints.run(request).value.await match
          case Some(response) =>
            response.status mustEqual Status.Ok
            response.as[HealthResponse].await mustEqual testHealthResponse

          case None => fail("Expected a response")

  "POST /maintenance/__exit" should:
    "return OK status and termination message" in: (mockedService, route) =>
      (() => mockedService.exit).expects().returns(IO.unit).once()
      val request = Request[IO](method = POST, uri = uri"/maintenance/__exit")

      async[IO]:
        route.endpoints.run(request).value.await match
          case Some(response) =>
            response.status mustEqual Status.Ok
            response.as[String].await mustEqual "Application terminated."

          case None => fail("Expected a response")
