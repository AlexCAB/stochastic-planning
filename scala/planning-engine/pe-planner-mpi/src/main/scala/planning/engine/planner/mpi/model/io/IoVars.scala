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
| created: 2026-10-05 |||||||||||*/

package planning.engine.planner.mpi.model.io

import cats.syntax.all.*
import cats.syntax.ext.*
import planning.engine.common.values.io.IoName
import planning.engine.common.errors.*
import planning.engine.planner.mpi.actors.node.Node

final case class IoVars(
    in: Map[IoName, Variable.Input],
    out: Map[IoName, Variable.Output],
):
  // Do split nodes into input and output sets based on their related IO var names
  def conNodesByIo[F[_]: MT](nodes: Set[Node.Con]): F[(Set[Node.Con], Set[Node.Con])] = nodes
    .foldM((Set[Node.Con](), Set[Node.Con]())):
      case ((inNs, outNs), n) if in.contains(n.ioValue.name) => in(n.ioValue.name).validateNode(n).as((inNs + n, outNs))

      case ((inNs, outNs), n) if out.contains(n.ioValue.name) =>
        out(n.ioValue.name).validateNode(n).as((inNs, outNs + n))

      case (_, n) => s"Undefined IO name ${n.ioValue.name}".assertionError

  def get[F[_]: MT](name: IoName): F[Variable] = in.get(name).orElse(out.get(name)) match
    case Some(v) => v.pure
    case None    => s"Undefined IO name $name".assertionError

  override lazy val toString: String = s"IoVars(in = ${in.values.mkString(", ")}, out = ${out.values.mkString(", ")})"

object IoVars:
  def apply[F[_]: MT](in: Map[IoName, Variable.Input], out: Map[IoName, Variable.Output]): F[IoVars] =
    for
      _ <- in.keySet.assertContainsNoneOf(out.keySet, "Input and output variable names should not overlap")
      _ <- (in ++ out).foreachM((n, v) => n.assertEquals(v.name, "Map key should match variable name"))
    yield new IoVars(in, out)

  def apply[F[_]: MT](in: Set[Variable.Input], out: Set[Variable.Output]): F[IoVars] =
    IoVars(in.map(n => n.name -> n).toMap, out.map(n => n.name -> n).toMap)
