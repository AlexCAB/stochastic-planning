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
| created: 2025-06-28 |||||||||||*/

package planning.engine.common.enums

import cats.ApplicativeThrow
import planning.engine.common.values.db.Neo4j
import planning.engine.common.errors.assertionError
import cats.syntax.all.*

enum EdgeType:
  case THEN, LINK

  def toLabel: String = this match
    case THEN => Neo4j.THEN_LABEL
    case LINK => Neo4j.LINK_LABEL

object EdgeType:
  def fromLabel[F[_]: ApplicativeThrow](l: String): F[EdgeType] =
    if l.equalsIgnoreCase(Neo4j.THEN_LABEL)
    then THEN.pure
    else if l.equalsIgnoreCase(Neo4j.LINK_LABEL)
    then LINK.pure
    else s"Unknown EdgeType label: $l".assertionError
