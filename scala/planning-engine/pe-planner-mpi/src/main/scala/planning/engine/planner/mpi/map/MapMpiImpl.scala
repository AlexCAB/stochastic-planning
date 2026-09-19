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

private[mpi] class MapMpiImpl[F[_]: {Async, LoggerFactory}](
    visualization: Option[Visualization],
    guardian: Guardian,
    scheduler: Scheduler,
    actors: AtomicCell[F, Option[MapMpiImpl.Actors]],
) extends MapMpi[F]:
  import MapMpiImpl.Actors

  private val logger = LoggerFactory[F].getLogger
  private given Scheduler = scheduler

  private def runAtActors[R](block: Actors => F[R]): F[R] = actors.get.flatMap:
    case Some(actors) => block(actors)
    case None         => "Map network not initialized".assertionError

  def init(vars: Set[Variable]): F[Unit] =
    def splitVars: (Set[Variable.Input], Set[Variable.Output]) =
      (vars.collect { case i: Variable.Input => i }, vars.collect { case o: Variable.Output => o })

    def initActors: F[Option[Actors]] =
      for
        (inVars, outVars) <- splitVars.pure
        _ <- logger.info(s"Initializing: inVars = $inVars, outVars = $outVars, visualization = $visualization")
        (manager, planner, visualizer) <- guardian.initialize(inVars, outVars, visualization)
        actors = Actors(manager, planner, visualizer)
        _ <- logger.info(s"Initialized: $actors")
      yield Some(actors)

    actors.evalUpdate:
      case Some(actors) => s"Map network already initialized, $actors".assertionError
      case None         => initActors

  def reset(): F[Unit] =
    def cleanup(actors: Actors): F[Option[Actors]] =
      for
        _ <- guardian.reset()
        _ <- logger.info(s"Actor cleaned: $actors")
      yield None

    actors.evalUpdate:
      case None         => logger.info("Map network not initialized, nothing to do").as(None)
      case Some(actors) => cleanup(actors)

  def addSamples(samples: Set[Sample.Man], nodes: Map[MnId.Nim, NodeData]): F[Map[SampleId, Sample.Man]] =
    runAtActors(_.manager.addManSamples(samples, nodes))

private[mpi] object MapMpiImpl:
  final case class Actors(
      manager: Manager,
      planner: Planner,
      visualizer: Option[Visualizer],
  )
