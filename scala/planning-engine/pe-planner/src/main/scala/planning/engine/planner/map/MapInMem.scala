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
import planning.engine.planner.map.dcg.nodes.ConcreteDcgNode
import planning.engine.planner.map.dcg.state.DcgState
import planning.engine.planner.map.logic.MapBaseLogic

class MapInMem[F[_]: {Async, LoggerFactory}](
    stateCell: AtomicCell[F, DcgState[F]]
) extends MapBaseLogic[F](stateCell) with MapLike[F]:
  override def getForIoValues(values: Set[IoValue]): F[(Map[IoValue, Set[ConcreteDcgNode[F]]], Set[IoValue])] =
    for
      state <- stateCell.get
      (foundNodes, notFoundValues) <- state.concreteForIoValues(values)
    yield (foundNodes, notFoundValues)

object MapInMem:
  def apply[F[_]: {Async, LoggerFactory}](): F[MapInMem[F]] =
    for
        state <- AtomicCell[F].of(DcgState.init[F]())
    yield new MapInMem(state)
