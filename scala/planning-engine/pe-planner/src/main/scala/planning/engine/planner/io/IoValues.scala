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
| created: 2025-08-13 |||||||||||*/

package planning.engine.planner.io

import planning.engine.common.values.io.{IoTime, IoValue}

trait IoValues:
  def values: Set[IoValue] // Set of variable names to their indices

final case class Observation(
    time: IoTime, // Monotonically increased value used to track the time of the observation
    values: Set[IoValue], // Set of observed IO variable names to their value indices.
    utility: Option[Double] // Optional utility value associated with the observation
) extends IoValues

final case class Action(values: Set[IoValue]) extends IoValues

object Action:
  val empty: Action = Action(Set.empty)
