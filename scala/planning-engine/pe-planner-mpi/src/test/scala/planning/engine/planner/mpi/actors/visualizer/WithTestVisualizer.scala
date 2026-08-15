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
| created: 14.08.2026 |||||||||||*/

package planning.engine.planner.mpi.actors.visualizer

import planning.engine.planner.mpi.actors.UnitSpecWithIOAndTestKit

trait WithTestVisualizer:
  self: UnitSpecWithIOAndTestKit =>
  
  trait WithVisualizer:
    lazy val visualizer: TestVisualizer = TestVisualizer("test-visualizer")
