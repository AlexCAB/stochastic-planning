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

package planning.engine.planner.model

import planning.engine.common.values.node.IoIndex
import planning.engine.common.values.text.Name

trait IoData:
  def values: Map[Name, IoIndex] // Map of variable names to their indices

final case class Observation(values: Map[Name, IoIndex]) extends IoData

final case class Action(values: Map[Name, IoIndex]) extends IoData
