package eskimi.task

import eskimi.task.BidMsg.Banner
import eskimi.task.BidMsg.In._
import eskimi.task.BidMsg.Out.BidResponse
import spray.json.DefaultJsonProtocol._

object JsonFormats {
  implicit val geoFormat        = jsonFormat1(Geo)
  implicit val userFormat       = jsonFormat2(User)
  implicit val siteFormat       = jsonFormat2(Site)
  implicit val deviceFormat     = jsonFormat2(Device)
  implicit val impressionFormat = jsonFormat8(Impression)
  implicit val bidRequestFormat = jsonFormat5(BidRequest)

  implicit val bannerFormat      = jsonFormat4(Banner)
  implicit val bidResponseFormat = jsonFormat4(BidResponse)
}
