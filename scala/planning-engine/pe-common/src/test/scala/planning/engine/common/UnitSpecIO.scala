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
| created: 2025-03-22 |||||||||||*/


package planning.engine.common

import cats.effect.Sync
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.{Assertion, Assertions, compatible}
import org.scalatest.wordspec.AsyncWordSpecLike


abstract class UnitSpecIO extends AsyncWordSpecLike with AsyncIOSpec:
  
  extension[F[_], A](f: F[A])
    def expect(p: A => Boolean)(implicit F: Sync[F]): F[Assertion] = f.asserting(a => assert(p(a)))