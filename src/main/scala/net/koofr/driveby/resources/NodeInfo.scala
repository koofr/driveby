package net.koofr.driveby.resources

case class NodeInfo(id: String,
                    name: String,
                    description: String,
                    size: Long,
                    `type`: String,
                    updated_time: String)

object NodeInfo  {
  import spray.json.DefaultJsonProtocol._
  implicit val format = jsonFormat6(NodeInfo.apply)
}
