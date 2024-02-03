package example.project.jobsboard.http

import cats.effect.Concurrent
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import cats.syntax.all.toSemigroupKOps
import example.project.jobsboard.http.routes.HealthRoutes
import example.project.jobsboard.http.routes.JobRoutes
import org.typelevel.log4cats.Logger

class HttpApi[F[_]: Concurrent: Logger] private:
  private val healthRoutes: HttpRoutes[F] = HealthRoutes[F].routes
  private val jobRoutes: HttpRoutes[F]    = JobRoutes[F].routes

  val routes: HttpRoutes[F] = Router("/api" -> (healthRoutes <+> jobRoutes))

object HttpApi:
  def apply[F[_]: Concurrent: Logger]: HttpApi[F] = new HttpApi[F]
