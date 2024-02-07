package example.project.jobsboard.modules

import example.project.jobsboard.core.Jobs
import cats.effect.kernel.Resource
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import example.project.jobsboard.core.Jobs.LiveJobs
import cats.effect.kernel.Async

final case class Core[F[_]] private (val jobs: Jobs[F])

object Core:
  def postrgresResource[F[_]: Async]: Resource[F, HikariTransactor[F]] =
    for
      ec <- ExecutionContexts.fixedThreadPool[F](32)
      xa <- HikariTransactor.newHikariTransactor[F](
        "org.postgresql.Driver",
        "jdbc:postgresql:board",
        "docker",
        "docker",
        ec,
      )
    yield xa

  def apply[F[_]: Async]: Resource[F, Core[F]] =
    postrgresResource[F]
      .evalMap(postgres => LiveJobs(postgres))
      .map(jobs => new Core(jobs))
