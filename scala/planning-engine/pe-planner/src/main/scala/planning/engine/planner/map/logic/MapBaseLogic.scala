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
import org.typelevel.log4cats.LoggerFactory
import planning.engine.planner.map.dcg.state.DcgState

abstract class MapBaseLogic[F[_]: {Async, LoggerFactory}](stateCell: AtomicCell[F, DcgState[F]])
