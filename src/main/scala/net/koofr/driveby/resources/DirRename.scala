package net.koofr.driveby.resources

case class DirRename(name: String)

object DirRename  {
  import spray.json.DefaultJsonProtocol._
  implicit val format = jsonFormat1(DirRename.apply)
}


