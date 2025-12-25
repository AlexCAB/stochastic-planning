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
| created: 2025-08-06 |||||||||||*/

package planning.engine.api.route

import cats.effect.Concurrent
import io.circe.{Encoder, Json}
import org.http4s.Response
import org.http4s.dsl.Http4sDsl
import io.circe.syntax.EncoderOps
import cats.syntax.all.*
import org.http4s.circe.*
import org.typelevel.log4cats.LoggerFactory

trait RouteBase[F[_]: {Concurrent, LoggerFactory}]:
  self: Http4sDsl[F] =>

  private val logger = LoggerFactory[F].getLogger

  private def throwableToJson(err: Throwable): Json = Json.obj(
    "error" -> Json.fromString(err.getMessage),
    "stackTrace" -> Json.fromValues(err.getStackTrace.map(elem => Json.fromString(elem.toString)))
  )

  extension [T](result: F[T])
    def response(implicit encoder: Encoder[T]): F[Response[F]] = result
      .flatMap(info => Ok(info.asJson))
      .recoverWith: err =>
        for
          _ <- logger.error(err)("Error processing request: ")
          response <- InternalServerError(throwableToJson(err))
        yield response
