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
| created: 08.06.2026 |||||||||||*/

package planning.engine.planner.mpi.model

import cats.MonadThrow
import planning.engine.common.values.io.IoIndex
import planning.engine.common.values.node.HnName
import planning.engine.common.values.text.Description
import planning.engine.map.io.node.IoNode

object MapNode:

  sealed trait Data:
    def name: Option[HnName]
    def description: Option[Description]

  final case class Concrete[F[_]: MonadThrow](
      name: Option[HnName],
      description: Option[Description],
      ioNode: IoNode[F],
      valueIndex: IoIndex
  ) extends Data

  final case class Abstract(
      name: Option[HnName],
      description: Option[Description]
  ) extends Data
