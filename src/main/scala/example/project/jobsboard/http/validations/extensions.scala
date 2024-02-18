package example.project.jobsboard.http.validations

import cats.*
import cats.implicits.*
import cats.effect.Concurrent
import org.http4s.*
import org.http4s.dsl.impl.*
import org.http4s.implicits.*
import io.circe.generic.auto.*
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.FormDataDecoder.formEntityDecoder
import org.typelevel.log4cats.Logger
import example.project.jobsboard.logging.logError
import example.project.jobsboard.http.validations.Validator.ValidationResult
import example.project.jobsboard.http.responses.FailureResponse

extension [F[_]: Concurrent: Logger](req: Request[F])
  def validate[A: Validator](
    valid: A => F[Response[F]]
  )(using decoder: EntityDecoder[F, A], encoder: EntityEncoder[F, A]): F[Response[F]] =
    val dsl = Http4sDsl[F]
    import dsl.*

    req
      .as[A]
      .logError(e => s"Failed to decode request: $e")
      .flatMap { value =>
        validatedEntity(value)
          .fold(
            errors  => BadRequest(FailureResponse(errors.toList.map(_.error).mkString(", "))),
            success => valid(success)
          )
      }

def validatedEntity[A](entity: A)(using validator: Validator[A]): ValidationResult[A] =
  validator.validate(entity)
