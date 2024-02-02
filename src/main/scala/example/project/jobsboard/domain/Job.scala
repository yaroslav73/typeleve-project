package example.project.jobsboard.domain

import java.util.UUID
import java.time.Instant
import cats.instances.boolean
import example.project.jobsboard.domain.Job.JobInfo

// TODO: Using opaque type
final case class Job(
  id: UUID,
  timestamp: Instant,
  ownerEamil: String,
  jobInfo: JobInfo,
  active: Boolean = false,
)

object Job:
  final case class JobInfo(
    company: String,
    title: String,
    description: String,
    externalUrl: String,
    location: String,
    remote: Boolean,
    salary: Option[SalaryRange],
    currency: Option[String],
    country: Option[String],
    tags: Option[List[String]],
    image: Option[String],
    seniority: Option[String],
    other: Option[String],
  )

  object JobInfo:
    def empty: JobInfo = JobInfo("", "", "", "", "", false, None, None, None, None, None, None, None)

  final case class SalaryRange(min: BigDecimal, main: BigDecimal)
