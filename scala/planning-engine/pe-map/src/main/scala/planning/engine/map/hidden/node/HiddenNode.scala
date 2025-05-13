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
| created: 2025-04-05 |||||||||||*/

package planning.engine.map.hidden.node

import cats.MonadThrow
import planning.engine.common.values.text.Name
import planning.engine.common.values.node.HnId

trait HiddenNode[F[_]: MonadThrow]:
  def id: HnId
  def name: Option[Name]
