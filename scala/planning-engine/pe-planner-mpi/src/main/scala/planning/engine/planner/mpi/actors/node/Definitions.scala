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
| created: 19.06.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.node

import cats.MonadThrow
import planning.engine.common.values.node.MnId
import planning.engine.planner.mpi.data.node.{AbsData, ConData, NodeData, StaticActors}

private[node] trait Definitions:
  sealed trait Definition:
    def id: MnId
    def data: NodeData
    def actors: StaticActors

  final case class ConDef[F[_]: MonadThrow](
      id: MnId.Con,
      data: ConData[F],
      actors: StaticActors,
  ) extends Definition:
    override lazy val toString: String = s"[${id.reprValue}, ${data.name.repr}]"

  final case class AbsDef(
      id: MnId.Abs,
      data: AbsData,
      actors: StaticActors,
  ) extends Definition:
    override lazy val toString: String = s"(${id.reprValue}, ${data.name.repr})"
