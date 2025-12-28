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

package planning.engine.api.service.maintenance

import cats.effect.{IO, Resource}
import planning.engine.common.UnitSpecWithResource
import planning.engine.api.model.maintenance.HealthResponse

class MaintenanceServiceSpec extends UnitSpecWithResource[MaintenanceService[IO]]:
  override val resource: Resource[IO, MaintenanceService[IO]] = MaintenanceService[IO]()

  "MaintenanceService.getHealth" should:
    "return OK status and version when environment variable is set" in: service =>
      service.getHealth
        .logValue("getHealth")
        .asserting(_ mustEqual HealthResponse(HealthResponse.Status.OK, "test_app_version"))

  "MaintenanceService.awaitShutdown and MaintenanceService.exit" should:
    "log the shutdown message after latch is released" in: service =>
      val shutdown =
        for
          _ <- service.exit
          _ <- service.awaitShutdown
        yield ()

      shutdown.logValue("exit").asserting(_ mustEqual ())
