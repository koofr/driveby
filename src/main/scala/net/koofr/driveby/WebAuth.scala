package net.koofr.driveby

import scala.concurrent.{ExecutionContext, Future}
import spray.http.{FormData, Uri}
import akka.io.IO
import akka.pattern.ask
import spray.can.Http
import spray.http._
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport._
import spray.client.pipelining._
import akka.actor.ActorSystem

case class UserAuth(user_id: String, access_token: String, refresh_token: String, expires_in: Int)

object UserAuthProtocol extends DefaultJsonProtocol  {
  implicit val userAuthFormat = DefaultJsonProtocol.jsonFormat4(UserAuth)
}

class WebAuth(clientId: String, clientSecret: String, redirectUrl: String)(implicit system: ActorSystem) {
  private val pipeline: HttpRequest => Future[UserAuth] = {
    import system.dispatcher
    import UserAuthProtocol.userAuthFormat
    sendReceive ~> unmarshal[UserAuth]
  }

  def start(state: Option[String]): String = {
    s"https://login.live.com/oauth20_authorize.srf?client_id=$clientId&scope=wl.skydrive,wl.skydrive_update,wl.offline_access,wl.signin&response_type=code&redirect_uri=$redirectUrl&state=${state.getOrElse("")}"
  }

  def finish(redirectedUrl: String): Future[UserAuth] = {
    val rUri = Uri(redirectedUrl)

    rUri.query.get("code").map { code =>
      val body = FormData(
        Map(
          "client_id" -> clientId,
          "client_secret" -> clientSecret,
          "code" -> code,
          "grant_type" -> "authorization_code",
          "redirect_uri" -> redirectUrl,
          "state" -> rUri.query.get("state").getOrElse("")
        )
      )
      val req = Post("https://login.live.com/oauth20_token.srf", body)
      pipeline(req)
    } getOrElse Future.failed(new NoSuchElementException("cannot obtain `code` from redirectedUrl"))
  }

  def refresh(refreshToken: String): Future[UserAuth] = {
    val body = FormData(
      Map(
        "client_id" -> clientId,
        "client_secret" -> clientSecret,
        "redirect_uri" -> redirectUrl,
        "grant_type" -> "refresh_token",
        "refresh_token" -> refreshToken
      )
    )
    val req = Post("https://login.live.com/oauth20_token.srf", body)
    pipeline(req)
  }

}


