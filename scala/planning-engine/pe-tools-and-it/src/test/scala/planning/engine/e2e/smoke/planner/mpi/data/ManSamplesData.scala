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
| created: 2026-10-21 |||||||||||*/

package planning.engine.e2e.smoke.planner.mpi.data

import planning.engine.common.graph.edges.MeKey
import planning.engine.common.values.node.MnId
import planning.engine.common.values.text.{Description, Name}
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.planner.mpi.model.data.samples.Sample

trait ManSamplesData extends NodesData:
  import MeKey.{Link, Then}
  import MnId.Nim

  final case class ManSample(samples: Set[Sample.Man], nodes: Map[MnId.Nim, NodeData])

  // Reuses `conPosition3` (expected to be already in the map), all other nodes are new.
  lazy val avoidObstacleSample: ManSample = ManSample(
    samples = Set(
      Sample.Man(
        props = Sample.Props(probabilityCount = 10L, utility = 0.8),
        info = Sample.Info(Name("avoid-obstacle-left"), Description.some("Turn left on obstacle")),
        edges = Set(Link(Nim(1), Nim(3)), Link(Nim(2), Nim(3)), Then(Nim(1), Nim(2)), Then(Nim(3), Nim(4))),
      ),
      Sample.Man(
        props = Sample.Props(probabilityCount = 3L, utility = -0.2),
        info = Sample.Info(Name("hit-obstacle"), None),
        edges = Set(Link(Nim(2), Nim(3)), Then(Nim(2), Nim(1))),
      ),
    ),
    nodes = Map(
      Nim(1) -> conPosition3,
      Nim(2) -> conObstacleTrue,
      Nim(3) -> absAvoid,
      Nim(4) -> conTurnLeft,
    ),
  )

  // Reuses `conObstacleTrue` and `absAvoid` (expected to be added by `avoidObstacleSample`), all other nodes are new.
  lazy val moveForwardSample: ManSample = ManSample(
    samples = Set(
      Sample.Man(
        props = Sample.Props(probabilityCount = 7L, utility = 0.5),
        info = Sample.Info(Name("move-forward"), Description.some("Move forward when no obstacle")),
        edges = Set(Link(Nim(3), Nim(4)), Link(Nim(5), Nim(4)), Then(Nim(4), Nim(3))),
      ),
      Sample.Man(
        props = Sample.Props(probabilityCount = 2L, utility = 0.1),
        info = Sample.Info(Name("avoid-then-move"), None),
        edges = Set(Link(Nim(1), Nim(2)), Then(Nim(2), Nim(4)), Then(Nim(4), Nim(5))),
      ),
    ),
    nodes = Map(
      Nim(1) -> conObstacleTrue,
      Nim(2) -> absAvoid,
      Nim(3) -> conSpeedHalf,
      Nim(4) -> absMove,
      Nim(5) -> conStep2,
    ),
  )
