package example.project.jobsboard.domain

import java.time.Instant
import java.util.UUID
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
    salary: Option[Int],
    // TODO: Reaplace with SalaryRange
    // salary: Option[SalaryRange],
    currency: Option[String],
    country: Option[String],
    tags: Option[List[String]],
    image: Option[String],
    seniority: Option[String],
    other: Option[String],
  )

  object JobInfo:
    def Empty: JobInfo = JobInfo("", "", "", "", "", false, None, None, None, None, None, None, None)

    val Test: JobInfo =
      JobInfo(
        company     = "Test Company",
        title       = "Software Engineer",
        description = "An awesome job!",
        externalUrl = "https://test-company.com/jobs/1",
        location    = "Kyiv, Ukraine",
        remote      = true,
        salary      = None,
        currency    = None,
        country     = None,
        tags        = None,
        image       = None,
        seniority   = None,
        other       = None,
      )

  final case class SalaryRange(min: BigDecimal, main: BigDecimal)

  final case class JobFilter(
    companies: List[String]   = List.empty,
    locations: List[String]   = List.empty,
    countries: List[String]   = List.empty,
    tags: List[String]        = List.empty,
    seniority: Option[String] = None,
    salary: Option[Int]       = None,
    remote: Boolean           = false,
  )
