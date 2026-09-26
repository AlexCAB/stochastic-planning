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
| created: 2026-09-14 |||||||||||*/

package planning.engine.planner.mpi.map

import cats.effect.Async
import cats.effect.kernel.Async
import cats.syntax.all.*
import cats.effect.std.AtomicCell
import org.apache.pekko.actor.typed.Scheduler
import org.typelevel.log4cats.LoggerFactory
import planning.engine.planner.mpi.{MapMpi, Visualization}
import planning.engine.planner.mpi.actors.guardian.Guardian
import planning.engine.planner.mpi.actors.manager.Manager
import planning.engine.planner.mpi.actors.planner.Planner
import planning.engine.planner.mpi.actors.visualizer.Visualizer
import planning.engine.planner.mpi.model.io.Variable
import planning.engine.planner.mpi.model.data.samples.Sample
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.common.values.node.MnId
import planning.engine.common.values.sample.SampleId
import planning.engine.common.errors.*
import planning.engine.planner.mpi.model.data.map.Metadata

private[mpi] class MapMpiImpl[F[_]: {Async, LoggerFactory}](
    visualization: Option[Visualization],
    guardian: Guardian,
    scheduler: Scheduler,
    mapState: AtomicCell[F, Option[MapMpiImpl.MapState]],
) extends MapMpi[F]:
  import MapMpiImpl.MapState

  private val logger = LoggerFactory[F].getLogger
  private given Scheduler = scheduler

  private def runAt[R](block: MapState => F[R]): F[R] = mapState.get.flatMap:
    case Some(map) => block(map)
    case None      => "Map network not initialized".assertionError

  def init(metadata: Metadata, inVars: Set[Variable.Input], outVars: Set[Variable.Output]): F[Unit] =

    def initMap: F[Option[MapState]] =
      for
        _ <- logger.info(s"Init: md = $metadata, in = $inVars, out = $outVars, viz = $visualization")
        (manager, planner, visualizer) <- guardian.initialize(inVars, outVars, visualization)
        map = MapState(metadata, manager, planner, visualizer)
        _ <- logger.info(s"Initialized: $map")
      yield Some(map)

    mapState.evalUpdate:
      case Some(map) => s"Map network already initialized, $map".assertionError
      case None      => initMap

  def reset(): F[Unit] =
    def cleanup(map: MapState): F[Option[MapState]] =
      for
        _ <- guardian.reset()
        _ <- logger.info(s"Map cleaned: $map")
      yield None

    mapState.evalUpdate:
      case None      => logger.info("Map network not initialized, nothing to do").as(None)
      case Some(map) => cleanup(map)

  def addSamples(samples: Set[Sample.Man], nodes: Map[MnId.Nim, NodeData]): F[Map[SampleId, Sample.Man]] =
    runAt(_.manager.addManSamples(samples, nodes))

private[mpi] object MapMpiImpl:
  final case class MapState(
      metadata: Metadata,
      manager: Manager,
      planner: Planner,
      visualizer: Option[Visualizer],
  )
