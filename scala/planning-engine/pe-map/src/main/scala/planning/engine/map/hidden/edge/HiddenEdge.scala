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
| created: 2025-04-07 |||||||||||*/

package planning.engine.map.hidden.edge

import cats.MonadThrow
import planning.engine.map.hidden.node.HiddenNode
import planning.engine.map.samples.sample.stored.SampleEdge

trait HiddenEdge[F[_]: MonadThrow]:
  def source: HiddenNode[F]
  def target: HiddenNode[F]
  def samples: List[SampleEdge]

  override def toString: String = s"${this.getClass.getSimpleName}(source=${source.name}:target=${target.name}:" +
    s"${target.id}, samples=[${samples.mkString(", ")}])"
