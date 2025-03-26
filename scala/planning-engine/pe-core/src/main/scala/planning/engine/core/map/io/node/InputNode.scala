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


package planning.engine.core.map.io.node

import cats.MonadThrow
import planning.engine.core.map.io.variable.IoVariable


class InputNode[F[_] : MonadThrow, T](val name: String, val variable: IoVariable[F, T]) extends IoNode[F, T]
