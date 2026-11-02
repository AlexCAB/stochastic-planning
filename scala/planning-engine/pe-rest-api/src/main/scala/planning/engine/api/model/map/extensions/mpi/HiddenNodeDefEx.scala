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
import io.circe.{Decoder, Json}
import planning.engine.api.model.map.payload.{AbstractNodeDef, ConcreteNodeDef, HiddenNodeDef}
import planning.engine.common.errors.assertionError
import planning.engine.common.values.io.IoIndex
import planning.engine.planner.mpi.model.data.node.NodeData
import planning.engine.planner.mpi.model.io.{IoVars, Type}

object HiddenNodeDefEx:
  extension (hnDef: HiddenNodeDef)
    def toNodeData(vars: IoVars)[F[_]: MT]: F[NodeData] =
      def decodeT[T](v: Json, t: Type[?])(using decoder: Decoder[T]): F[T] = v.as[T].fold(
        err => s"Failed to decode value: $v, as type: $t, err: ${err.getMessage}".assertionError,
        _.pure,
      )

      def decodeBool(v: Json): F[Boolean] =
        def parse(str: String): F[Boolean] = str.toLowerCase match
          case "t" | "true"  => true.pure
          case "f" | "false" => false.pure
          case _             => s"Failed to decode value: $v, as type: Bool".assertionError

        v.as[String].fold(
          err => s"Failed to decode value: $v: ${err.getMessage}".assertionError,
          str => parse(str),
        )

      def parseValue(tp: Type[?], v: Json): F[IoIndex] = tp match
        case t: Type.N    => decodeT[Long](v, t).flatMap(t.indexForValue)
        case t: Type.R    => decodeT[Double](v, t).flatMap(t.indexForValue)
        case t: Type.Bool => decodeBool(v).flatMap(t.indexForValue)
        case t: Type.Opt  => decodeT[String](v, t).flatMap(t.indexForValue)

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
