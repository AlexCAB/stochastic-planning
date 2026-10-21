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

import planning.engine.common.values.io.IoName
import planning.engine.common.values.text.{Description, Name}
import planning.engine.planner.mpi.model.data.map.Metadata
import planning.engine.planner.mpi.model.io.{Type, Variable}

trait MapData:
  lazy val testMetadata: Metadata = Metadata(Name("smoke-map"), Description.some("MPI map structure build smoke test"))

  // Input variables
  lazy val inPosition: Variable.Input = Variable.Input(IoName("in-position"), Type.N(min = 0L, max = 10L))
  lazy val inObstacle: Variable.Input = Variable.Input(IoName("in-obstacle"), Type.Bool(Set(true, false)))

  // Output variables
  lazy val outSpeed: Variable.Output = Variable.Output(IoName("out-speed"), Type.R(min = 0.0, max = 1.0))
  lazy val outTurn: Variable.Output = Variable.Output(IoName("out-turn"), Type.Opt(List("left", "right", "straight")))
  lazy val outStep: Variable.Output = Variable.Output(IoName("out-step"), Type.N(min = 1L, max = 5L))

  lazy val inVars: Set[Variable.Input] = Set(inPosition, inObstacle)
  lazy val outVars: Set[Variable.Output] = Set(outSpeed, outTurn, outStep)
