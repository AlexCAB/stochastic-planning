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
| created: 2026-02-12 |||||||||||*/

package planning.engine.common.values.edge

import cats.MonadThrow
import planning.engine.common.errors.assertionError
import planning.engine.common.values.node.{HnIndex, MnId}
import planning.engine.common.values.edge.Indexies

final case class IndexMap(indexies: Map[MnId, HnIndex]):
  def get[F[_]: MonadThrow](srcId: MnId, trgId: MnId): F[Indexies] = (indexies.get(srcId), indexies.get(trgId)) match
    case (Some(srcIndex), Some(trgIndex)) => MonadThrow[F].pure(Indexies(srcIndex, trgIndex))
    case (srcIndex, trgIndex)             => s"Source ($srcId) or target ($trgId) not found in IndexMap".assertionError

