package net.koofr.driveby.resources

case class Quota(quota: Long, available: Long) {
  def used = quota - available
}

object Quota  {
  import spray.json.DefaultJsonProtocol._
  implicit val format = jsonFormat2(Quota.apply)
}