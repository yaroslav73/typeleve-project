package example.project.jobsboard.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

final case class PostgresConfig(
  numberOfThreads: Int,
  driverClassName: String,
  url: String,
  user: String,
  password: String
) derives ConfigReader
