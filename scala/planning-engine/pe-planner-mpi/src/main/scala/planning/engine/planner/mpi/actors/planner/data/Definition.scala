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
| created: 31.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.planner.data

import cats.MonadThrow
import cats.syntax.all.*
import cats.syntax.ext.*
import planning.engine.common.errors.*
import planning.engine.common.values.io.IoName
import planning.engine.planner.mpi.actors.node.Node
import planning.engine.planner.mpi.common.io.Variable

private[planner] final case class Definition(
    inputVariables: Map[IoName, Variable.Input],
    outputVariables: Map[IoName, Variable.Output],
):
  def conNodesByType[F[_]: MonadThrow](nodes: Set[Node.Con]): F[(Set[Node.Con], Set[Node.Con])] = nodes
    .foldM((Set[Node.Con](), Set[Node.Con]())):
      case ((inNs, outNs), n) if inputVariables.contains(n.ioValue.name) =>
        inputVariables(n.ioValue.name).validateNode(n).as((inNs + n, outNs))

      case ((inNs, outNs), n) if outputVariables.contains(n.ioValue.name) =>
        outputVariables(n.ioValue.name).validateNode(n).as((inNs, outNs + n))

      case (_, n) => s"Undefined IO name ${n.ioValue.name}".assertionError

private[planner] object Definition:
  def apply[F[_]: MonadThrow](
      inVars: Map[IoName, Variable.Input],
      outVars: Map[IoName, Variable.Output],
  ): F[Definition] =
    for
      _ <- inVars.keySet.assertContainsNoneOf(outVars.keySet, "Input and output variable names must be unique")
      _ <- inVars.foreachM((n, v) => n.assertEquals(v.name, s"Input variable name mismatch for $n"))
      _ <- outVars.foreachM((n, v) => n.assertEquals(v.name, s"Output variable name mismatch for $n"))
    yield new Definition(inVars, outVars)

  def apply[F[_]: MonadThrow](inVars: Set[Variable.Input], outVars: Set[Variable.Output]): F[Definition] =
    apply(inVars.map(v => v.name -> v).toMap, outVars.map(v => v.name -> v).toMap)
