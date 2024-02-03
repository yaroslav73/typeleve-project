package example.project.jobsboard

import example.project.jobsboard.config.EmberConfig
import example.project.jobsboard.http.HttpApi
import cats.effect.{ IO, IOApp }
import org.http4s.ember.server.EmberServerBuilder
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderException
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Application extends IOApp.Simple:

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  private val configSource = ConfigSource.default.load[EmberConfig]

  def run: IO[Unit] =
    configSource match
      case Left(errors) =>
        IO.raiseError(ConfigReaderException[String](errors))
      case Right(config) =>
        EmberServerBuilder
          .default[IO]
          .withHost(config.host)
          .withPort(config.port)
          .withHttpApp(HttpApi[IO].routes.orNotFound)
          .build
          .use(s => IO.println(s"Server started at: ${s.address}") *> IO.never)
