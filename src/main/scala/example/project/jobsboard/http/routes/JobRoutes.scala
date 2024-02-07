package example.project.jobsboard.http.routes

import cats.Monad
import cats.MonadThrow
import cats.syntax.semigroupk.toSemigroupKOps
import cats.syntax.flatMap.toFlatMapOps
import cats.syntax.functor.toFunctorOps
import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.effect.Concurrent
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.FormDataDecoder.formEntityDecoder
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.CirceEntityEncoder.*
import io.circe.generic.auto.*
import java.util.UUID
import example.project.jobsboard.domain.Job
import example.project.jobsboard.http.responses.FailureResponse
import example.project.jobsboard.domain.Job.JobInfo
import scala.collection.mutable
import java.time.Instant
import cats.Applicative
import org.typelevel.log4cats.Logger
import example.project.jobsboard.core.Jobs

// TODO: Why we use Concurrent here?
class JobRoutes[F[_]: Concurrent: Logger] private (jobs: Jobs[F]) extends Http4sDsl[F]:
  // POST /jobs?offset=n&limit=k { filters }
  // TODO: Add query parameters and filters
  private val allJobsRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root =>
      jobs.all().flatMap(Ok(_))
  }

  // GET /jobs/uuid
  private val findJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / UUIDVar(id) =>
      jobs.find(id).flatMap {
        case Some(job) => Ok(job)
        case None      => NotFound(FailureResponse(s"Job with id $id not found"))
      }
  }

  // POST /jobs/new { job info }
  private val createJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "new" =>
      for
        jobInfo  <- req.as[JobInfo]
        id       <- jobs.create("test@test.test", jobInfo) // TODO: Remove hardcoded email
        response <- Created(id)
      yield response
  }

  // PUT /jobs/uuid { job info }
  private val updateJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> Root / UUIDVar(id) =>
      for
        jobInfo <- req.as[JobInfo]
        updated <- jobs.update(id, jobInfo)
        response <- updated match
          case Some(updated) => Ok(updated)
          case None          => NotFound(FailureResponse(s"Job with id $id not found"))
      yield response
  }

  // DELETE /jobs/uuid
  private val deleteJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case DELETE -> Root / UUIDVar(id) =>
      jobs.find(id).flatMap {
        case Some(_) =>
          for
            _        <- jobs.delete(id)
            response <- Ok(s"Job with id $id deleted")
          yield response
        case None => NotFound(FailureResponse(s"Job with id $id not found"))
      }
  }

  val routes: HttpRoutes[F] = Router(
    "/jobs" -> (allJobsRoute <+> findJobRoute <+> createJobRoute <+> updateJobRoute <+> deleteJobRoute)
  )

object JobRoutes:
  def apply[F[_]: Concurrent: Logger](jobs: Jobs[F]): JobRoutes[F] = new JobRoutes[F](jobs)
