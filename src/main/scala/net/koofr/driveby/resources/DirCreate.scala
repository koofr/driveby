package net.koofr.driveby.resources

case class DirCreate(name: String, description: String)

object DirCreate  {
  import spray.json.DefaultJsonProtocol._
  implicit val format = jsonFormat2(DirCreate.apply)
}


