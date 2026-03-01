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
| created: 2026-02-27 |||||||||||*/

package planning.engine.common.graph.paths

import cats.MonadThrow
import cats.syntax.all.*
import cats.data.NonEmptyChain
import planning.engine.common.graph.edges.EdgeKey.End
import planning.engine.common.values.node.MnId
import planning.engine.common.errors.*

sealed trait Path:
  def walk: NonEmptyChain[(MnId, End)]

  lazy val begin: MnId = walk.head._1
  lazy val end: MnId = walk.last._2.id

  lazy val repr: String = this match
    case _: Path.Direct => "Direct"
    case _: Path.Loop   => "Loop"
    case _: Path.Noose  => "Noose"

  override lazy val toString: String = s"$repr(${walk.head._1.reprNode}${walk.map(_._2.repr).toList.mkString("")})"

object Path:
  private[paths] def makePath[F[_]: MonadThrow, P <: Path](
      walk: Vector[(MnId, End)],
      make: NonEmptyChain[(MnId, End)] => P
  ): F[P] =
    for
      _ <- walk.zip(walk.drop(1)).map((c, n) => (c._2.id, n._1)).assertAllEqual("Path must be continuous")
      opList <- NonEmptyChain.fromSeq(walk).pure
      list <- opList.map(_.pure).getOrElse(s"Path must contain at least one edge".assertionError)
    yield make.apply(list)

  final case class Direct(walk: NonEmptyChain[(MnId, End)]) extends Path

  object Direct:
    def apply[F[_]: MonadThrow](walk: Vector[(MnId, End)]): F[Direct] = makePath(walk, Direct.apply)

  final case class Loop(walk: NonEmptyChain[(MnId, End)]) extends Path

  object Loop:
    def apply[F[_]: MonadThrow](walk: Vector[(MnId, End)]): F[Loop] = makePath(walk, Loop.apply)

  final case class Noose(walk: NonEmptyChain[(MnId, End)]) extends Path

  object Noose:
    def apply[F[_]: MonadThrow](walk: Vector[(MnId, End)]): F[Noose] = makePath(walk, Noose.apply)
