package example.project.jobsboard.core

import cats.implicits.catsSyntaxApplicativeId
import cats.effect.kernel.Resource
import doobie.util.transactor.Transactor
import cats.effect.IO
import org.testcontainers.containers.PostgreSQLContainer
import doobie.util.ExecutionContexts
import doobie.hikari.HikariTransactor
import org.testcontainers.containers.wait.strategy.Wait
import java.time.Duration

trait Database {
  def initStript: String

  val postgres: Resource[IO, PostgreSQLContainer[_]] =
    val acquire: IO[PostgreSQLContainer[_]] = IO {
      val container: PostgreSQLContainer[_] = new PostgreSQLContainer("postgres:15.3-alpine").withInitScript(initStript)
      container.start()
      container
    }
    val release: PostgreSQLContainer[_] => IO[Unit] = container => IO(container.stop())
    Resource.make(acquire)(release)

  val transactor: Resource[IO, Transactor[IO]] =
    for
      db <- postgres
      ec <- ExecutionContexts.fixedThreadPool[IO](1)
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver",
        db.getJdbcUrl,
        db.getUsername,
        db.getPassword,
        ec
      )
    yield xa
}
