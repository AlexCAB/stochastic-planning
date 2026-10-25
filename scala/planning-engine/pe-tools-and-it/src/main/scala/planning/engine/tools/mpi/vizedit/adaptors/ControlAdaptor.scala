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
| created: 2026-10-23 |||||||||||*/

package planning.engine.tools.mpi.vizedit.adaptors

import cats.effect.{Async, Resource}
import cats.syntax.all.*
import cats.effect.std.Dispatcher
import org.typelevel.log4cats.LoggerFactory
import planning.engine.api.model.map.{MapAddSamplesRequest, MapInitRequest}
import planning.engine.planner.mpi.MapMpi
import planning.engine.tools.mpi.vizedit.arguments.Arguments
import planning.engine.yed.Yed
import planning.engine.api.model.map.extensions.mpi.{MapAddSamplesRequestEx, MapInitRequestEx}
import planning.engine.common.values.db.DbName

import java.nio.file.Path

// Adopt commands send from GUI to map network `MapMpi`
class ControlAdaptor[F[_]: {Async, Dispatcher, LoggerFactory}](args: Arguments, map: MapMpi[F]):
  import MapAddSamplesRequestEx.*, MapInitRequestEx.*
  private val logger = LoggerFactory[F].getLogger

  val dbName: DbName = DbName("in-mem-map")

  def init: F[Unit] =
    for
      _ <- logger.info(s"Initializing ControlAdaptor, args: $args")
      _ <- args.yedPath.map(p => loadMap(p)).getOrElse(().pure)
    yield ()

  def reset(): F[Unit] =
    for
      _ <- map.reset()
      _ <- logger.info("MapMpi reset")
    yield ()

  def isMapLoaded: F[Boolean] =
    for
      isLoaded <- map.isInitialized
      _ <- logger.info(s"Map is loaded: $isLoaded")
    yield isLoaded

  def loadMap(yedPath: Path): F[Unit] =
    def initMap(definition: MapInitRequest): F[Unit] =
      for
        metadata <- definition.metadata
        inVars <- definition.inVars
        outVars <- definition.outVars
        _ <- map.init(metadata, inVars, outVars)
      yield ()

    def loadSamples(request: MapAddSamplesRequest): F[Unit] =
      for
        vars <- map.getIoVars
        (mnIds, nodes) <- request.toNodes(vars)
        _ <- logger.info(s"Built mnIds map = $mnIds")
        samples <- request.toSamples(mnIds)
        _ <- map.addSamples(samples, nodes)
      yield ()

    for
      _ <- logger.info(s"Loading map from yEd graph: $yedPath")
      yed <- Yed.load(yedPath)
      _ <- logger.info(s"Loaded raw map from yEd graph: $yed")
      defReq <- yed.buildMapDefinition(dbName)
      _ <- logger.info(s"Built map definition from yEd graph: $defReq")
      _ <- initMap(defReq)
      samplesReq <- yed.buildSamples
      _ <- loadSamples(samplesReq)
    yield ()

object ControlAdaptor:
  def apply[F[_]: {Async, Dispatcher, LoggerFactory}](args: Arguments, map: MapMpi[F]): Resource[F, ControlAdaptor[F]] =
    Resource.eval(new ControlAdaptor(args, map).pure)
