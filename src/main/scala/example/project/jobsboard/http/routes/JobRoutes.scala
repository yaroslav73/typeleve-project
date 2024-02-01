package example.project.jobsboard.http.routes

import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import cats.syntax.all.toSemigroupKOps

class JobRoutes[F[_]: Monad] private extends Http4sDsl[F]:
  // POST /jobs?offset=n&limit=k { filters }
  // TODO: Add query parameters and filters
  private val allJobsRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root =>
      Ok("TODO: Find all jobs with param and filters")
  }

  // GET /jobs/uuid
  private val findJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / UUIDVar(id) =>
      Ok(s"TODO: Get job by $id")
  }

  // POST /jobs/new { job info }
  private val createJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root / "new" =>
      Ok("TODO: Create new job")
  }

  // PUT /jobs/uuid { job info }
  private val updateJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case PUT -> Root / UUIDVar(id) =>
      Ok(s"TODO: Update job by $id")
  }

  // DELETE /jobs/uuid
  private val deleteJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case DELETE -> Root / UUIDVar(id) =>
      Ok(s"TODO: Delete job by $id")
  }

  val routes: HttpRoutes[F] = Router(
    "/jobs" -> (allJobsRoute <+> findJobRoute <+> createJobRoute <+> updateJobRoute <+> deleteJobRoute)
  )

object JobRoutes:
  def apply[F[_]: Monad]: JobRoutes[F] = new JobRoutes[F]
