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
| created: 2025-03-25 |||||||||||*/

package planning.engine.map.io.node

import cats.MonadThrow
import planning.engine.common.values.io.IoName
import planning.engine.map.io.variable.IoVariable

final case class OutputNode[F[_]: MonadThrow](name: IoName, variable: IoVariable[F, ?]) extends IoNode[F]
