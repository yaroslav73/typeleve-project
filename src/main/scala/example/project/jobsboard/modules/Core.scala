package example.project.jobsboard.modules

import cats.effect.kernel.{Async, Resource}
import cats.syntax.all.*

import doobie.util.transactor.Transactor
import example.project.jobsboard.core.Jobs
import example.project.jobsboard.core.Jobs.LiveJobs

final case class Core[F[_]] private (val jobs: Jobs[F])

object Core:
  def apply[F[_]: Async](xa: Transactor[F]): Resource[F, Core[F]] =
    Resource
      .eval(LiveJobs(xa))
      .map(jobs => new Core(jobs))
