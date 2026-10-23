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
| created: 2026-10-23 |||||||||||*/

package planning.engine.tools.mpi.vizedit.gui.controls

import cats.effect.Async
import cats.effect.std.Dispatcher
import planning.engine.tools.mpi.vizedit.gui.AsyncFx.*
import scalafx.geometry.Insets
import scalafx.scene.layout.HBox

// Control area of the Samples window. Must be constructed on the FX thread (see `SamplesControl.apply`).
class SamplesControl extends HBox:
  padding = Insets(10)
  spacing = 10

object SamplesControl:
  def apply[F[_]: {Async, Dispatcher}](): F[SamplesControl] = fx(new SamplesControl)
