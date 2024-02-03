package example.project.jobsboard.logging

import cats.MonadError
import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.monadError.catsSyntaxMonadError
import org.typelevel.log4cats.Logger

extension [F[_], E, A](fa: F[A])(using ME: MonadError[F, E], logger: Logger[F])
  def log(success: A => String, error: E => String): F[A] =
    fa.attemptTap {
      case Right(a) => logger.info(success(a))
      case Left(e)  => logger.error(error(e))
    }

  def logError(error: E => String): F[A] =
    fa.attemptTap {
      case Left(e)  => logger.error(error(e))
      case Right(_) => ().pure[F]
    }
