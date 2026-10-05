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
| created: 2026-10-05 |||||||||||*/

package planning.engine.api.model.map.extensions.mpi

import cats.syntax.all.*
import cats.syntax.ext.MT
import io.circe.{Json, Decoder}
import planning.engine.api.model.map.payload.{AbstractNodeDef, ConcreteNodeDef, HiddenNodeDef}
import planning.engine.common.values.io.IoIndex
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.planner.mpi.model.io.{IoVars, Type}
import planning.engine.common.errors.assertionError

object HiddenNodeDefEx:
  extension (hnDef: HiddenNodeDef)
    def toNodeData(vars: IoVars)[F[_]: MT]: F[NodeData] =
      def decode[T](v: Json, t: Type[?])(using decoder: Decoder[T]): F[T] = v.as[T].fold(
        err => s"Failed to decode value: $v, as type: $t, err: ${err.getMessage}".assertionError,
        _.pure,
      )

      def parseValue(tp: Type[?], v: Json): F[IoIndex] = tp match
        case t: Type.N    => decode[Long](v, t).flatMap(t.indexForValue)
        case t: Type.R    => decode[Double](v, t).flatMap(t.indexForValue)
        case t: Type.Bool => decode[Boolean](v, t).flatMap(t.indexForValue)
        case t: Type.Opt  => decode[String](v, t).flatMap(t.indexForValue)

      def convertCon(con: ConcreteNodeDef): F[NodeData] =
        for
          ioVar <- vars.get(con.ioNodeName)
          ioIndex <- parseValue(ioVar.varType, con.value)
        yield NodeData.Con(
          name = Some(con.name),
          description = con.description,
          ioName = con.ioNodeName,
          valueIndex = ioIndex,
        )

      hnDef match
        case con: ConcreteNodeDef => convertCon(con)
        case abs: AbstractNodeDef => NodeData.Abs(Some(abs.name), abs.description).pure
