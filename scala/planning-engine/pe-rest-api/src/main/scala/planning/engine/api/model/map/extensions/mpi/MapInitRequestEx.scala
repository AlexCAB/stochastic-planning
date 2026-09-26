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
| created: 2026-09-26 |||||||||||*/

package planning.engine.api.model.map.extensions.mpi

import cats.MonadThrow
import cats.syntax.all.*
import planning.engine.api.model.map.MapInitRequest
import planning.engine.api.model.map.payload.*
import planning.engine.common.errors.assertionError
import planning.engine.common.values.io.IoName
import planning.engine.planner.mpi.model.data.map.Metadata
import planning.engine.planner.mpi.model.io.{Type, Variable}

object MapInitRequestEx:
  extension (req: MapInitRequest)
    private def toType[F[_]: MonadThrow](definition: IoNodeApiDef): F[Type[?]] = definition match
      case v: BooleanIoNodeDef if v.acceptableValues.nonEmpty => Type.Bool(v.acceptableValues).pure
      case v: FloatIoNodeDef if v.min <= v.max                => Type.R(v.min.toDouble, v.max.toDouble).pure
      case v: IntIoNodeDef if v.min <= v.max                  => Type.N(v.min.toLong, v.max.toLong).pure
      case v: ListStrIoNodeDef if v.elements.nonEmpty         => Type.Opt(v.elements).pure
      case _ => s"Can't convert in/out node definition $definition to variable".assertionError

    private def toVars[F[_]: MonadThrow, V <: Variable](
        definitions: List[IoNodeApiDef],
        makeVar: (IoName, Type[?]) => V,
    ): F[Set[V]] = definitions.traverse(d => toType[F](d).map(t => makeVar(d.name, t))).map(_.toSet)

    def metadata[F[_]: MonadThrow]: F[Metadata] = req.name match
      case Some(name) => Metadata(name, req.description).pure
      case None       => "MapInitRequest.name must be defined to initialize the MPI planner".assertionError

    def inVars[F[_]: MonadThrow]: F[Set[Variable.Input]] = toVars(req.inputNodes, Variable.Input(_, _))
    def outVars[F[_]: MonadThrow]: F[Set[Variable.Output]] = toVars(req.outputNodes, Variable.Output(_, _))
