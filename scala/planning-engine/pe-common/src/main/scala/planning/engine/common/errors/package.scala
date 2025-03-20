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
| created: 2025-03-20 |||||||||||*/


package planning.engine.common.errors

import cats.ApplicativeThrow

package object errors


extension (msg: String)
  inline def assertionError[M[_] : ApplicativeThrow, V]: M[V] =
    ApplicativeThrow[M].raiseError(AssertionError(msg))

