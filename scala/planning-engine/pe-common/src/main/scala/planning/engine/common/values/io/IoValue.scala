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
| created: 2025-12-04 |||||||||||*/

package planning.engine.common.values.io

final case class IoValue(value: (IoName, IoIndex)) extends AnyVal:
  def name: IoName = value._1
  def index: IoIndex = value._2

object IoValue:
  def apply(name: IoName, index: IoIndex): IoValue = IoValue((name, index))
