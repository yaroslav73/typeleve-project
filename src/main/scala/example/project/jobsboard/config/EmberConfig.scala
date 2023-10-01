package example.project.jobsboard.config

import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import pureconfig.ConfigCursor
import pureconfig.ConfigReader.Result
import pureconfig.error.FailureReason
import pureconfig.error.CannotConvert

final case class EmberConfig(host: Host, port: Port) derives ConfigReader

object EmberConfig:
  given hostReader: ConfigReader[Host] =
    ConfigReader[String].emap { host =>
      Host
        .fromString(host)
        .toRight(CannotConvert(host, Host.getClass.toString, s"Invalid host format: $host"))
    }

  given portReader: ConfigReader[Port] =
    ConfigReader[Int].emap { port =>
      Port
        .fromInt(port)
        .toRight(CannotConvert(port.toString, Port.getClass.toString, s"Invalid port number: $port"))
    }
