package net.koofr.driveby.resources

case class User(id: String,
                name: Option[String],
                first_name: Option[String],
                last_name: Option[String],
                gender: Option[String],
                locale: String)

object User  {
  import spray.json.DefaultJsonProtocol._
  implicit val format = jsonFormat6(User.apply)
}
