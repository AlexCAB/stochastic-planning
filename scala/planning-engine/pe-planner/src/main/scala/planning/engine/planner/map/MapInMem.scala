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
import planning.engine.common.values.io.{IoName, IoValue}
import planning.engine.common.values.sample.SampleId
import planning.engine.map.samples.sample.Sample
import planning.engine.planner.map.dcg.nodes.ConcreteDcgNode
import planning.engine.planner.map.dcg.state.{DcgState, IdsCountState}
import planning.engine.planner.map.logic.MapBaseLogic
import planning.engine.common.errors.*
import planning.engine.common.values.node.{HnId, HnName}
import planning.engine.map.data.MapMetadata
import planning.engine.map.hidden.node.{AbstractNode, ConcreteNode}
import planning.engine.map.io.node.{InputNode, IoNode, OutputNode}

trait MapInMemLike[F[_]] extends MapLike[F]:
  def init(metadata: MapMetadata, inNodes: List[InputNode[F]], outNodes: List[OutputNode[F]]): F[Unit]

class MapInMem[F[_]: {Async, LoggerFactory}](
    dcgStateCell: AtomicCell[F, DcgState[F]],
    idsCountCell: AtomicCell[F, IdsCountState]
) extends MapBaseLogic[F](dcgStateCell) with MapInMemLike[F]:
  private val logger = LoggerFactory[F].getLogger

  private[map] override def stateUpdated(state: DcgState[F]): F[Unit] = Async[F].unit
  private[map] def getIdsCount: F[IdsCountState] = idsCountCell.get

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

  override def init(metadata: MapMetadata, inNodes: List[InputNode[F]], outNodes: List[OutputNode[F]]): F[Unit] = ???

  override def getIoNode(name: IoName): F[IoNode[F]] = ???

  override def addNewConcreteNodes(params: ConcreteNode.ListNew): F[Map[HnId, Option[HnName]]] = ???

  override def addNewAbstractNodes(params: AbstractNode.ListNew): F[Map[HnId, Option[HnName]]] = ???

  override def addNewSamples(samples: Sample.ListNew): F[Map[SampleId, Sample]] =
    addNewSamplesToCache(buildSamples(samples))

  override def findHnIdsByNames(names: List[HnName]): F[Map[HnName, List[HnId]]] = ???

  override def getForIoValues(values: Set[IoValue]): F[(Map[IoValue, Set[ConcreteDcgNode[F]]], Set[IoValue])] =
    for
      state <- getMapState
      (foundNodes, notFoundValues) <- state.concreteForIoValues(values)
      _ <- logger.info(s"Got from map in mem: foundNodes = $foundNodes, notFoundValues = $notFoundValues")
    yield (foundNodes, notFoundValues)

  override def reset(): F[Unit] =
    for
      _ <- dcgStateCell.set(DcgState.init[F]())
      _ <- idsCountCell.set(IdsCountState.init)
    yield ()

object MapInMem:
  def apply[F[_]: {Async, LoggerFactory}](): F[MapInMem[F]] =
    for
      dcgState <- AtomicCell[F].of(DcgState.init[F]())
      idsCount <- AtomicCell[F].of(IdsCountState.init)
    yield new MapInMem(dcgState, idsCount)
