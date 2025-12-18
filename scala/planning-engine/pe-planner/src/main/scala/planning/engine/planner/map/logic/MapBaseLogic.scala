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

package planning.engine.planner.map.logic

import cats.effect.kernel.Async
import cats.effect.std.AtomicCell
import cats.syntax.all.*
import org.typelevel.log4cats.LoggerFactory
import planning.engine.common.values.sample.SampleId
import planning.engine.map.samples.sample.Sample
import planning.engine.planner.map.dcg.state.DcgState
import planning.engine.common.errors.*
import planning.engine.common.validation.Validation
import planning.engine.planner.map.dcg.edges.DcgEdge

abstract class MapBaseLogic[F[_]: {Async, LoggerFactory}](stateCell: AtomicCell[F, DcgState[F]]):
  private[map] def getState: F[DcgState[F]] = stateCell.get
  private[map] def setState(state: DcgState[F]): F[Unit] = stateCell.set(state)

  private[map] def addNewSamplesToCache(newSamples: => F[List[Sample]]): F[Map[SampleId, Sample]] = 
    stateCell.evalModify: state =>
      for
        samples <- newSamples
        _ <- Validation.validateList(samples)
        _ <- (state.allHnIds, samples.flatMap(_.allHnIds)).assertContainsAll("New samples contain unknown HnIds")
        dcgEdges <- samples.traverse(_.edges.toList.traverse(DcgEdge.apply)).map(_.flatten)
        stateWithEdges <- state.mergeEdges(dcgEdges)
        stateWithSamples <- stateWithEdges.addSamples(samples.map(_.data))
      yield (stateWithSamples, samples.map(s => s.data.id -> s).toMap)
