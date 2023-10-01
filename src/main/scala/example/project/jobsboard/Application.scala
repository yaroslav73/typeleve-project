package example.project.jobsboard

import cats.effect.{ IO, IOApp }
import example.project.jobsboard.http.routes.HealthRoutes
import org.http4s.ember.server.EmberServerBuilder
import pureconfig.ConfigSource
import example.project.jobsboard.config.EmberConfig
import pureconfig.error.ConfigReaderException

object Application extends IOApp.Simple:

  val configSource = ConfigSource.default.load[EmberConfig]

  def run: IO[Unit] =
    configSource match
      case Left(errors) =>
        IO.raiseError(ConfigReaderException[String](errors))
      case Right(config) =>
        EmberServerBuilder
          .default[IO]
          .withHost(config.host)
          .withPort(config.port)
          .withHttpApp(HealthRoutes[IO].routes.orNotFound)
          .build
          .use(s => IO.println(s"Server started at: ${s.address}") *> IO.never)
