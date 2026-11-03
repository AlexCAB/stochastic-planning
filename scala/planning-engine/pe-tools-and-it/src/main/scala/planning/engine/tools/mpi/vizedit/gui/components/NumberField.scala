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
| created: 2026-11-03 |||||||||||*/

package planning.engine.tools.mpi.vizedit.gui.components

import scalafx.scene.control.TextField

import scala.util.matching.Regex

// Text field which rejects edits not matching the pattern. Must be constructed on the FX thread.
// Note: the rejected text is seen by the other `text` listeners too, they must read the current value of `text`.
class NumberField private (pattern: Regex) extends TextField:
  text.onChange((_, oldText, newText) => if !pattern.matches(newText) then text = oldText)

object NumberField:
  // Accept also partial input of a number (e.g. "", "-" or "1."), since it is validated on each key stroke.
  private val intPattern: Regex = """-?\d*""".r
  private val floatPattern: Regex = """-?\d*\.?\d*""".r

  def int(): NumberField = new NumberField(intPattern)
  def float(): NumberField = new NumberField(floatPattern)
