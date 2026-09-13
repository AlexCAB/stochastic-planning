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
| created: 04.08.26 |||||||||||*/

package planning.engine.planner.mpi.model.io

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.common.errors.*
import planning.engine.common.values.io.IoName
import planning.engine.planner.mpi.actors.node.Node

sealed trait Variable:
  def name: IoName
  def varType: Type[?]

  def validateNode[F[_]: MonadThrow](node: Node.Con): F[Node.Con] =
    for
      _ <- node.ioValue.name.assertEquals(name, s"Node ${node.mnId} has invalid name for variable $name")
      _ <- varType.isDefinedAt(node.ioValue.index).assertTrue(s"Node ${node.mnId} has invalid index for variable $name")
    yield node

object Variable:
  final case class Input(name: IoName, varType: Type[?]) extends Variable
  final case class Output(name: IoName, varType: Type[?]) extends Variable
