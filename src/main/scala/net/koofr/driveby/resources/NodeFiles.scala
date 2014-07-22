package net.koofr.driveby.resources

case class NodeFiles(data: Vector[NodeInfo])

object NodeFiles  {
  import spray.json.DefaultJsonProtocol._
  implicit val format = jsonFormat1(NodeFiles.apply)
}
