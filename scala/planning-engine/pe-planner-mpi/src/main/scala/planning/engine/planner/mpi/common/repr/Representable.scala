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
| created: 03.08.2026 |||||||||||*/

package planning.engine.planner.mpi.common.repr

import cats.MonadThrow
import fansi.Str

trait Representable:
  def longAutoRepr[F[_]: MonadThrow]: F[List[Str]] = MonadThrow[F]
    .catchNonFatal(pprint.apply(this).toString.split("\n").toList.map(Str(_)))
