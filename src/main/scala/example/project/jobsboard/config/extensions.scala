package example.project.jobsboard.config

import pureconfig.ConfigSource
import pureconfig.ConfigReader
import cats.MonadThrow
import cats.syntax.all.toFlatMapOps
import cats.syntax.all.catsSyntaxApplicativeId
import cats.syntax.all.catsSyntaxApplicativeErrorId
import pureconfig.error.ConfigReaderException
import scala.reflect.ClassTag

extension (source: ConfigSource)
  def loadF[F[_], A](using reader: ConfigReader[A], F: MonadThrow[F], classTag: ClassTag[A]): F[A] =
    F.pure(source.load[A]).flatMap {
      case Left(errors) => F.raiseError[A](ConfigReaderException(errors))
      case Right(value) => F.pure(value)
    }
