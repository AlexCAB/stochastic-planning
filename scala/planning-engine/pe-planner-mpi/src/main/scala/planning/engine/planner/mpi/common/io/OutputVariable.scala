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
| created: 31.08.26 |||||||||||||*/

package planning.engine.planner.mpi.common.io

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.values.io.IoName
import planning.engine.planner.mpi.actors.node.Node

final case class OutputVariable(name: IoName):

  // TODO: Add validation logic for output variables if needed
  def validateNode[F[_]: MonadThrow](node: Node.Con): F[Node.Con] = node.pure
