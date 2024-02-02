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

class JobRoutes[F[_]: Concurrent] private extends Http4sDsl[F]:
  // Simulate DB:
  private val jobs = mutable.Map.empty[UUID, Job]

  // POST /jobs?offset=n&limit=k { filters }
  // TODO: Add query parameters and filters
  private val allJobsRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root =>
      Ok(jobs.values.toList)
  }

  // GET /jobs/uuid
  private val findJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / UUIDVar(id) =>
      jobs.get(id) match
        case Some(job) => Ok(job)
        case None      => NotFound(FailureResponse(s"Job with id $id not found"))
  }

  // POST /jobs/new { job info }
  private def createJob(jobInfo: JobInfo): F[Job] =
    Job(
      id         = UUID.randomUUID(),
      timestamp  = Instant.now(),
      ownerEamil = "test@test.test",
      jobInfo    = jobInfo,
      active     = false,
    ).pure[F]

  private val createJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "new" =>
      for
        jobInfo  <- req.as[JobInfo]
        job      <- createJob(jobInfo)
        _         = jobs += (job.id -> job)
        response <- Created(job)
      yield response
  }

  // PUT /jobs/uuid { job info }
  private val updateJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ PUT -> Root / UUIDVar(id) =>
      jobs
        .get(id)
        .fold {
          NotFound(FailureResponse(s"Job with id $id not found"))
        } { job =>
          for
            jobInfo  <- req.as[JobInfo]
            updated   = job.copy(jobInfo = jobInfo)
            _        <- jobs.update(id, updated).pure[F]
            response <- Ok(updated)
          yield response
        }
  }

  // DELETE /jobs/uuid
  private val deleteJobRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case DELETE -> Root / UUIDVar(id) =>
      jobs
        .get(id)
        .fold(NotFound(FailureResponse(s"Job with id $id not found"))) { _ =>
          for
            _        <- jobs.remove(id).pure[F]
            response <- Ok(s"Job with id $id deleted")
          yield response
        }
  }

  val routes: HttpRoutes[F] = Router(
    "/jobs" -> (allJobsRoute <+> findJobRoute <+> createJobRoute <+> updateJobRoute <+> deleteJobRoute)
  )

object JobRoutes:
  def apply[F[_]: Concurrent]: JobRoutes[F] = new JobRoutes[F]
