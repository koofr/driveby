package net.koofr.driveby.resources

case class NodeRelocation(destination: String)

object NodeRelocation  {
  import spray.json.DefaultJsonProtocol._
  implicit val format = jsonFormat1(NodeRelocation.apply)
}

