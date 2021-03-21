package eskimi.task

import akka.actor.typed.ActorRef
import eskimi.task.BidMsg.In.Campaign.Targeting
import eskimi.task.BidMsg.Out.BidResponse

object BidMsg {
  object In {

    sealed trait Command

    final case class BidCommand(
      request: BidRequest,
      replyTo: ActorRef[Option[BidResponse]]
    ) extends Command

    final case class BidRequest(
      id: String,
      imp: List[Impression],
      site: Site,
      user: Option[User],
      device: Option[Device]
    )

    final case class Impression(
      id: String,
      wmin: Option[Int],
      wmax: Option[Int],
      w: Option[Int],
      hmin: Option[Int],
      hmax: Option[Int],
      h: Option[Int],
      bidFloor: Option[Double]
    )
    final case class Site(id: String, domain: String)
    final case class User(id: String, geo: Option[Geo])
    final case class Device(id: String, geo: Option[Geo])
    final case class Geo(country: Option[String])

    final case class SetCampaigns(campaigns: List[Campaign]) extends Command

    final case class Campaign(
      id: String,
      country: String,
      targeting: Targeting,
      banners: List[Banner],
      bid: Double
    )

    object Campaign {
      final case class Targeting(targetedSiteIds: LazyList[String])
    }
  }

  object Out {
    final case class BidResponse(
      bidRequestId: String,
      price: Double,
      adid: String,
      banner: List[Banner]
    )
  }

  final case class Banner(id: Int, src: String, width: Int, height: Int)
}
