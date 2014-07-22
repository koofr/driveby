package net.koofr.driveby

import akka.actor.ActorSystem
import spray.http.Uri
import spray.can.client.HostConnectorSettings
import spray.can.Http

case class Endpoint(uriString: String)(implicit system: ActorSystem) {
  val fullUri = Uri(if (uriString.endsWith("/")) uriString.dropRight(1) else uriString)
  val uri =fullUri.copy(scheme = "", authority = Uri.Authority.Empty)

  val hostConnector = {
    val Uri.Authority(host, port, _) = fullUri.authority
    val effectivePort = if (port == 0) Uri.defaultPorts(fullUri.scheme) else port

    val settings = HostConnectorSettings(system)
    Http.HostConnectorSetup(
      host = host.toString,
      port = effectivePort,
      sslEncryption = fullUri.scheme == "https"
    ).copy(settings = Some(settings))
  }
}
