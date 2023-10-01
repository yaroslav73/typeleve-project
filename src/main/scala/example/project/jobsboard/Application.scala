package example.project.jobsboard

import cats.effect.{ IO, IOApp }
import example.project.jobsboard.http.routes.HealthRoutes
import org.http4s.ember.server.EmberServerBuilder

object Application extends IOApp.Simple:
  def run: IO[Unit] =
    EmberServerBuilder
      .default[IO]
      .withHttpApp(HealthRoutes[IO].routes.orNotFound)
      .build
      .use(s => IO.println(s"Server started at: ${s.address}") *> IO.never)
