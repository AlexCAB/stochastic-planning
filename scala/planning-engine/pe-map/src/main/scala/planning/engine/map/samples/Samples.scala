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
| created: 2025-05-01 |||||||||||*/

package planning.engine.map.samples

import cats.MonadThrow
import cats.effect.kernel.Concurrent
import cats.effect.std.AtomicCell
import planning.engine.map.database.Neo4jDatabaseLike
import cats.syntax.all.*

class Samples[F[_]: MonadThrow](state: AtomicCell[F, SamplesState], database: Neo4jDatabaseLike[F]):
  def getState: F[SamplesState] = state.get

object Samples:
  private[map] def apply[F[_]: Concurrent](initState: SamplesState, database: Neo4jDatabaseLike[F]): F[Samples[F]] =
    for
      state <- AtomicCell[F].of[SamplesState](initState)
      samples = new Samples[F](state, database)
    yield samples
