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
import cats.effect.std.AtomicCell
import planning.engine.common.values.{OpName, Neo4jId}
import planning.engine.map.hidden.state.node.NodeState

trait HiddenNode[F[_]: MonadThrow]:
  protected val state: AtomicCell[F, NodeState]
  val neo4jId: Neo4jId
  val name: OpName
