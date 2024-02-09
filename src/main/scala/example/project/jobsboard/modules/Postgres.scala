package example.project.jobsboard.modules

import example.project.jobsboard.config.PostgresConfig
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object Postgres:
  def make[F[_]: Async](config: PostgresConfig): Resource[F, HikariTransactor[F]] =
    for
      ec <- ExecutionContexts.fixedThreadPool[F](config.numberOfThreads)
      xa <- HikariTransactor.newHikariTransactor[F](
        config.driverClassName,
        config.url,
        config.user,
        config.password,
        ec,
      )
    yield xa
