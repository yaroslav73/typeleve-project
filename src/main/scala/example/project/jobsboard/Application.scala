package example.project.jobsboard

import cats.effect.{ IO, IOApp }
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.*
import cats.Monad
import cats.data.Kleisli
import org.http4s.{ HttpRoutes, Request, Response }
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router

object Application extends IOApp.Simple:
  private def healthEndpoint[F[_]: Monad]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl.*
    HttpRoutes.of[F] { case GET -> Root / "health" =>
      Ok("All going great!")
    }
  }

  private def routerWithPathPrefix = Router("/private" -> healthEndpoint[IO]).orNotFound

  def run: IO[Unit] =
    EmberServerBuilder
      .default[IO]
      .withHttpApp(routerWithPathPrefix)
      .build
      .use(s => IO.println(s"Server started at: ${s.address}") *> IO.never)
