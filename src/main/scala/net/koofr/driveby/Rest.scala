package net.koofr.driveby

import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.io.IO
import akka.util.Timeout
import spray.can.Http
import spray.client.pipelining._
import spray.httpx.encoding.{Gzip, Deflate}
import spray.http._
import java.util.Date
import spray.httpx.unmarshalling.FromResponseUnmarshaller

case class AuthConfig(accessToken: String,
                      refreshToken: String,
                      expires_at: Long,
                      webAuth: WebAuth,
                      verifySsl: Boolean)

class Rest(authConfig: AuthConfig)(implicit system: ActorSystem) {
  import system.dispatcher

  implicit private val sslContext = SslUtils.sslContext(authConfig.verifySsl)

  private val endpoint = Endpoint("https://apis.live.net/v5.0")
  private val baseUrl = endpoint.uri.path.toString()

  private val io = IO(Http)

  private[this] var access = (authConfig.accessToken, authConfig.expires_at)

  def validAuthToken(): Future[String]= {
    val (token, expires) = access
    if (expires < new Date().getTime) {
      authConfig.webAuth.refresh(authConfig.refreshToken) map { newAuth =>
        synchronized {
          val expiresAt = newAuth.expires_in*1000 + new Date().getTime
          access = (newAuth.access_token, expiresAt)
        }
        newAuth.access_token
      }
    } else {
      Future successful token
    }

  }

  private def prepare(req: HttpRequest)(implicit t: Timeout) = {
    val reqPath = req.uri.path.toString()
    val realReqPath = if (reqPath.startsWith("/")) reqPath else "/" + reqPath
    val fullPath = Uri.Path(baseUrl + realReqPath)
    for {
      token <- validAuthToken()
      fullReq = req.copy(
        uri = req.uri.copy(
          path = fullPath,
          query = ("access_token", token) +: req.uri.query
        )
      )
      Http.HostConnectorInfo(connector, _) <- io ? endpoint.hostConnector
      pipeline = sendReceive(connector) ~> decode(Deflate) ~> decode(Gzip)
    } yield (pipeline, fullReq)
  }

  def request(req: HttpRequest)(implicit t: Timeout): Future[HttpResponse] = {
    prepare(req) flatMap {
      case (pipeline, req) =>
        pipeline(req)
    }
  }

  def requestJSON[T](req: HttpRequest)(implicit t: Timeout, um: FromResponseUnmarshaller[T]): Future[T] = {
    prepare(req) flatMap {
      case (pipeline, req) =>
        (pipeline ~> unmarshal[T]) apply req
    }
  }

  def requestStatusUnit(req: HttpRequest, statusCode: StatusCode)(implicit t: Timeout): Future[Boolean] = {
    request(req) map (_.status == statusCode)
  }

  def requestStatus(req: HttpRequest, statusCode: StatusCode)(implicit t: Timeout): Future[Either[HttpResponse, HttpResponse]] = {
    request(req) map {
      case res @ HttpResponse(status, entity, _, _) if status == statusCode => Right(res)
      case res => Left(res)
    }
  }

}
