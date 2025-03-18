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
| created: 2025-03-18 |||||||||||*/


package planning.engine.common.values

final class Index(val value: Long) extends AnyVal


object Index:
  def apply(value: Long): Index = 
    assert(value >= 0, s"Index must be non-negative: $value")
    new Index(value)
    
  def unapply(index: Index): Option[Long] = Some(index.value)
