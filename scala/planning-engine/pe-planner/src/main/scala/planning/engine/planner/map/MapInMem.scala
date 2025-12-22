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
| created: 2025-12-12 |||||||||||*/

package planning.engine.planner.map

import cats.effect.kernel.Async
import cats.effect.std.AtomicCell
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.common.values.io.IoValue
import planning.engine.common.values.sample.SampleId
import planning.engine.map.samples.sample.Sample
import planning.engine.planner.map.dcg.nodes.ConcreteDcgNode
import planning.engine.planner.map.dcg.state.{DcgState, IdsCountState}
import planning.engine.planner.map.logic.MapBaseLogic
import planning.engine.common.errors.*

class MapInMem[F[_]: {Async, LoggerFactory}](
    dcgStateCell: AtomicCell[F, DcgState[F]],
    idsCountCell: AtomicCell[F, IdsCountState]
) extends MapBaseLogic[F](dcgStateCell) with MapLike[F]:
  private val logger = LoggerFactory[F].getLogger

  private[map] def stateUpdated(state: DcgState[F]): F[Unit] = Async[F].unit

  private[map] def buildSamples(newSamples: Sample.ListNew): F[List[Sample]] =
    for
      sampleIds <- idsCountCell.modify(_.getNextSampleIds(newSamples.list.size))
      _ <- sampleIds.assertDistinct("Generated sample IDs are not distinct")
      _ <- (sampleIds, newSamples.list).assertSameSize("Sample IDs count does not match samples count")
      sampleMap = sampleIds.zip(newSamples.list)
      sampleWithHnIndexMap <- sampleMap.traverse: (id, sample) =>
        idsCountCell.modify(_.getNextHnIndexes(sample.hnIds)).map(ixs => (id, sample, ixs))
      samples <- sampleWithHnIndexMap.traverse((id, sample, indexes) => Sample.formNew(id, sample, indexes))
    yield samples

  override def getForIoValues(values: Set[IoValue]): F[(Map[IoValue, Set[ConcreteDcgNode[F]]], Set[IoValue])] =
    for
      state <- getMapState
      (foundNodes, notFoundValues) <- state.concreteForIoValues(values)
      _ <- logger.info(s"Got from map in mem: foundNodes = $foundNodes, notFoundValues = $notFoundValues")
    yield (foundNodes, notFoundValues)

  override def addNewSamples(samples: Sample.ListNew): F[Map[SampleId, Sample]] =
    addNewSamplesToCache(buildSamples(samples))

object MapInMem:
  def apply[F[_]: {Async, LoggerFactory}](): F[MapInMem[F]] =
    for
      dcgState <- AtomicCell[F].of(DcgState.init[F]())
      idsCount <- AtomicCell[F].of(IdsCountState.init)
    yield new MapInMem(dcgState, idsCount)
