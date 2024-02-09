package example.project.jobsboard

import example.project.jobsboard.config.AppConfig
import example.project.jobsboard.modules.{ Core, HttpApi }
import cats.effect.{ IO, IOApp }
import org.http4s.ember.server.EmberServerBuilder
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderException
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import example.project.jobsboard.config.loadF
import example.project.jobsboard.modules.Postgres

object Application extends IOApp.Simple:

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  private val configSource: IO[AppConfig] = ConfigSource.default.loadF[IO, AppConfig]

  def run: IO[Unit] =
    configSource.flatMap {
      case AppConfig(postgresConfig, emberConfig) =>
        val app =
          for
            xa      <- Postgres.make[IO](postgresConfig)
            core    <- Core[IO](xa)
            httpApi <- HttpApi[IO](core)
            server <- EmberServerBuilder
              .default[IO]
              .withHost(emberConfig.host)
              .withPort(emberConfig.port)
              .withHttpApp(httpApi.routes.orNotFound)
              .build
          yield server

        app.use(s => IO.println(s"Server started at: ${s.address}") *> IO.never)
    }
