package example.project.jobsboard.config

import scala.reflect.ClassTag
import cats.MonadThrow
import cats.syntax.all.{ catsSyntaxApplicativeErrorId, catsSyntaxApplicativeId, toFlatMapOps }

import pureconfig.error.ConfigReaderException
import pureconfig.{ ConfigReader, ConfigSource }

extension (source: ConfigSource)
  def loadF[F[_], A](using reader: ConfigReader[A], F: MonadThrow[F], classTag: ClassTag[A]): F[A] =
    F.pure(source.load[A]).flatMap {
      case Left(errors) => F.raiseError[A](ConfigReaderException(errors))
      case Right(value) => F.pure(value)
    }
