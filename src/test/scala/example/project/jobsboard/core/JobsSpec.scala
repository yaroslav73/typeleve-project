package example.project.jobsboard.core

import org.scalatest.freespec.AsyncFreeSpec
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import example.project.jobsboard.fixtures.JobFixture
import org.scalatest.Args
import org.scalatest.Status
import example.project.jobsboard.core.Jobs.LiveJobs
import cats.effect.IO
import org.scalatest.BeforeAndAfterAll
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class JobsSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with Database with JobFixture {
  given Logger[IO] = Slf4jLogger.getLogger[IO]

  val initStript: String = "sql/jobs.sql"

  "Jobs 'algebra'" - {
    "should not return a job if the given UUID does not exist" in {
      transactor.use { xa =>
        val result = for
          jobs   <- LiveJobs[IO](xa)
          jobOpt <- jobs.find(NotFoundJobId)
        yield jobOpt

        result.asserting(_ shouldBe None)
      }
    }

    "should return a job by given UUID" in {
      transactor.use { xa =>
        val result = for
          jobs   <- LiveJobs[IO](xa)
          jobOpt <- jobs.find(TestJobId)
        yield jobOpt

        result.asserting(_ shouldBe Some(TestJob))
      }
    }

    "should return all jobs" in {
      transactor.use { xa =>
        val result = for
          jobs <- LiveJobs[IO](xa)
          jobs <- jobs.all()
        yield jobs

        result.asserting(_ shouldBe List(TestJob))
      }
    }

    "should update job by given UUID" in {
      transactor.use { xa =>
        val result = for
          jobs   <- LiveJobs[IO](xa)
          jobOpt <- jobs.update(TestJobId, UpdatedTestJob.jobInfo)
        yield jobOpt

        result.asserting(_ shouldBe Some(UpdatedTestJob))
      }
    }

    "should not update job by given not existen UUID" in {
      transactor.use { xa =>
        val result = for
          jobs   <- LiveJobs[IO](xa)
          jobOpt <- jobs.update(NotFoundJobId, UpdatedTestJob.jobInfo)
        yield jobOpt

        result.asserting(_ shouldBe None)
      }
    }

    "should create a new job" in {
      transactor.use { xa =>
        val result = for
          jobs   <- LiveJobs[IO](xa)
          id     <- jobs.create("new@test.job", NewTestJobInfo)
          jobOpt <- jobs.find(id)
        yield jobOpt

        result.asserting(_ should not be None)
      }
    }

    "should delete job by given UUID" in {
      transactor.use { xa =>
        val result = for
          jobs   <- LiveJobs[IO](xa)
          n      <- jobs.delete(TestJobId)
          jobOpt <- jobs.find(TestJobId)
        yield (n, jobOpt)

        result.asserting(_ shouldBe (1, None))
      }
    }
  }
}
