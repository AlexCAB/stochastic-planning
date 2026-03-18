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
| created: 2026-03-19 |||||||||||*/

package planning.engine.common.graph.edges

import planning.engine.common.values.node.PnId

sealed trait PeKey:
  def src: PnId
  def trg: PnId

  lazy val reprArrow: String = this match
    case _: PeKey.Link => "==>"
    case _: PeKey.Then => "-->"

  override lazy val toString: String = s"${src.repr} ${reprArrow} ${trg.repr}"

object PeKey:
  final case class Link(src: PnId, trg: PnId) extends PeKey
  final case class Then(src: PnId, trg: PnId) extends PeKey
