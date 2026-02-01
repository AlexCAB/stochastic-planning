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

import planning.engine.common.values.db.Neo4j

enum EdgeType:
  case LINK, THEN

  def toLabel: String = this match
    case LINK => Neo4j.LINK_LABEL
    case THEN => Neo4j.THEN_LABEL

  def isLink: Boolean = this == LINK
  def isThen: Boolean = this == THEN

  def repr: String = if isLink then "L" else "T"

object EdgeType:
  def fromLabel(l: String): Either[String, EdgeType] =
    if l.equalsIgnoreCase(Neo4j.THEN_LABEL)
    then Right(THEN)
    else if l.equalsIgnoreCase(Neo4j.LINK_LABEL)
    then Right(LINK)
    else Left(s"Unknown EdgeType label: $l")
