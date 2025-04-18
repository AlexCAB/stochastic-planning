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

package planning.engine.core.map.hidden.state.edge

import cats.MonadThrow
import planning.engine.core.map.hidden.node.HiddenNode
import planning.engine.core.map.hidden.state.SampleState

trait EdgeState[F[_]: MonadThrow]:
  val target: HiddenNode[F]
  val samples: Vector[SampleState]

  override def toString: String =
    s"${this.getClass.getSimpleName}(target=${target.name}:${target.neo4jId}, samples=[${samples.mkString(", ")}])"
