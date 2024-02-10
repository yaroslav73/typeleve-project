package example.project.jobsboard.fixtures

import java.time.Instant
import java.util.UUID
import cats.syntax.all.*

import example.project.jobsboard.domain.Job
import example.project.jobsboard.domain.Job.JobInfo

trait JobFixture:
  val NotFoundJobId = UUID.fromString("6ea79557-3112-4c84-a8f5-1d1e2c300948")

  val TestJobId = UUID.fromString("843df718-ec6e-4d49-9289-f799c0f40064")

  val NewTestJobId = UUID.fromString("efcd2a64-4463-453a-ada8-b1bae1db4377")

  val TestInstant = Instant.parse("2024-01-01T00:00:00Z")

  val TestJob = Job(
    id         = TestJobId,
    timestamp  = TestInstant,
    ownerEamil = "test@test.com",
    JobInfo(
      company     = "Awesome Company",
      title       = "Tech Lead",
      description = "An awesome job in Berlin",
      externalUrl = "https://rockthejvm.com/awesomejob",
      remote      = false,
      location    = "Berlin",
      salary      = Some(5000),
      currency    = Some("EUR"),
      country     = Some("Germany"),
      tags        = Some(List("scala", "scala-3", "cats")),
      image       = None,
      seniority   = Some("Senior"),
      other       = None,
    )
  )

  val UpdatedTestJob = TestJob.copy(
    jobInfo = JobInfo(
      company     = "Awesome Company (Spain Branch)",
      title       = "Engineering Manager",
      description = "An awesome job in Barcelona",
      externalUrl = "http://www.awesome.com",
      remote      = false,
      location    = "Barcelona",
      salary      = Some(5000),
      currency    = Some("USD"),
      country     = Some("Spain"),
      tags        = Some(List("scala", "scala-3", "zio")),
      image       = Some("http://www.awesome.com/logo.png"),
      seniority   = Some("Highest"),
      other       = Some("Some additional info")
    )
  )

  val InvalidJob = Job(
    null,
    TestInstant,
    "nothing@gmail.com",
    JobInfo.Empty
  )

  val NewTestJobInfo = JobInfo(
    "RockTheJvm",
    "Technical Author",
    "For the glory of the RockTheJvm!",
    "https://rockthejvm.com/",
    "From remote",
    true,
    3500.some,
    "EUR".some,
    "Romania".some,
    Some(List("scala", "scala-3", "cats", "akka", "spark", "flink", "zio")),
    None,
    Some("High"),
    None
  )

  val TestJobWithNotFoundId = TestJob.copy(id = NotFoundJobId)

  val AnotherTestJobId = UUID.fromString("19a941d0-aa19-477b-9ab0-a7033ae65c2b")
  val AnotherTestJob   = TestJob.copy(id = AnotherTestJobId)

  val RockTheJvmAwesomeJob =
    TestJob.copy(jobInfo = TestJob.jobInfo.copy(company = "RockTheJvm"))

  val UpdateTestJobInfo = JobInfo(
    "Awesome Company",
    "Tech Lead",
    "An awesome job in Berlin",
    "example.com",
    "Berlin",
    false,
    2000.some,
    "EUR".some,
    "Germany".some,
    Some(List("scala", "scala-3", "cats")),
    None,
    "High".some,
    None
  )
