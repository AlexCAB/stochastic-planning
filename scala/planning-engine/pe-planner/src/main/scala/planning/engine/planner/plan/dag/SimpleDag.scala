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
| created: 2025-08-20 |||||||||||*/

package planning.engine.planner.plan.dag

import cats.effect.Async
import cats.syntax.all.*
import cats.effect.std.AtomicCell
import cats.implicits.toFlatMapOps
import org.typelevel.log4cats.LoggerFactory
import planning.engine.planner.map.state.MapIdsCountState
import planning.engine.planner.plan.dag.state.DagState

trait SimpleDagLike[F[_]: Async]

final class SimpleDag[F[_]: {Async, LoggerFactory}](
                                                     idsCountCell: AtomicCell[F, MapIdsCountState],
                                                     dagStateCell: AtomicCell[F, DagState[F]]
) extends SimpleDagLike[F]:

  private val logger = LoggerFactory[F].getLogger

object SimpleDag:
  def apply[F[_] : {Async, LoggerFactory}](): F[SimpleDag[F]] =
    for
      idsCount <- AtomicCell[F].of(MapIdsCountState.init)
      dagState <- AtomicCell[F].of(DagState.empty)
    yield new SimpleDag(idsCount, dagState)
