package net.koofr.driveby

import akka.actor.ActorSystem
import spray.http.{HttpMethod, Uri}
import spray.client.pipelining._
import java.net.URLEncoder
import scala.concurrent.Future
import akka.util.Timeout
import spray.httpx.SprayJsonSupport._

class Client(authConfig: AuthConfig)(implicit system: ActorSystem) {
  import system.dispatcher

  private val rest = new Rest(authConfig)
  private implicit val timeout = new Timeout(20000)

  private val Copy = new RequestBuilder(HttpMethod.custom("COPY", safe=false, idempotent=false, entityAccepted=true))
  private val Move = new RequestBuilder(HttpMethod.custom("MOVE", safe=false, idempotent=false, entityAccepted=true))


  type Id = String

  def nodeInfo(id: Id): Future[resources.NodeInfo] = {
    rest.requestJSON[resources.NodeInfo](Get(s"/$id"))
  }

  def rootInfo(): Future[resources.NodeInfo] = nodeInfo("me/skydrive")

  def nodeFiles(id: Id): Future[Seq[resources.NodeInfo]] = {
    rest.requestJSON[resources.NodeFiles](Get(s"/$id/files")) map (_.data)
  }

  def resolvePath(path: String): Future[Option[Id]] = {
    val parts = path.split('/').filter(!_.isEmpty)
    parts.foldLeft(rootInfo() map (i => Option(i.id))){
      case (idFO, part) =>
        idFO.flatMap {
          _ map { id =>
            nodeFiles(id) map { files =>
              val name = part.toLowerCase
              files.find(_.name.toLowerCase == name).map(_.id)
            }
          } getOrElse Future.successful(None)
        }
    }
  }

  def createFolder(parentId: Id, name: String, description: String): Future[Boolean] = {
      rest.requestStatusUnit(Post(parentId, resources.DirCreate(name, description)), 201)
  }

  def delete(id: Id): Future[Boolean] = {
      rest.requestStatusUnit(Delete(id), 204)
  }

  def rename(id: Id, name: String): Future[Boolean] = {
    rest.requestStatusUnit(Put(id, resources.DirRename(name)), 200)
  }

  def copy(id: Id, targetParent: Id): Future[Boolean] = {
    rest.requestStatusUnit(Copy(id, resources.NodeRelocation(targetParent)), 201)
  }

  def move(id: Id, targetParent: Id): Future[Boolean] = {
    rest.requestStatusUnit(Move(id, resources.NodeRelocation(targetParent)), 201)
  }

  def quota(): Future[resources.Quota] = {
    rest.requestJSON[resources.Quota](Get("me/skydrive/quota"))
  }

  def me(): Future[resources.User] = {
    rest.requestJSON[resources.User](Get("me"))
  }
}
