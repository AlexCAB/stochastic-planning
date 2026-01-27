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
| created: 2025-05-11 |||||||||||*/

package planning.engine.common.values.node

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.values.LongVal
import planning.engine.common.values.db.Neo4j.{CONCRETE_LABEL, ABSTRACT_LABEL}
import planning.engine.common.errors.*

// Hidden Node ID is used to identify the hidden node in the graph.
sealed trait HnId extends Any with LongVal:
  private[node] def inc[T <: HnId](make: Long => T): T = make(value + 1L)

object HnId:
  val initCon: ConId = ConId(1L)
  val initAbs: AbsId = AbsId(1L)

  def apply[F[_]: MonadThrow](rawId: Long, labels: Set[String]): F[HnId] = labels match
    case ls if ls.contains(CONCRETE_LABEL) => ConId(rawId).pure
    case ls if ls.contains(ABSTRACT_LABEL) => AbsId(rawId).pure
    case _                                 => assertionError(s"Not found abstract or concrete label in labels: $labels")

final case class ConId(value: Long) extends AnyVal with HnId:
  def increase: ConId = ConId(value + 1L)

final case class AbsId(value: Long) extends AnyVal with HnId:
  def increase: AbsId = AbsId(value + 1L)
