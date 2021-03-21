package eskimi.task

import akka.actor.typed.scaladsl.Behaviors
import eskimi.task.BidMsg.Banner
import eskimi.task.BidMsg.In._
import eskimi.task.BidMsg.Out.BidResponse

import scala.util.Random

object BiddingAgent {
  private val random = Random

  def apply: Behaviors.Receive[Command] = apply(Nil)

  def apply(campaigns: List[Campaign]): Behaviors.Receive[Command] = Behaviors.receive {
    case (ctx, SetCampaigns(newCampaigns)) =>
      ctx.log.info(s"Set up new campaigns: $newCampaigns")
      apply(campaigns ++ newCampaigns)

    case (_, BidCommand(bidRequest, replyTo)) =>
      replyTo ! matchResponse(bidRequest, campaigns)
      Behaviors.same
  }

  private def matchResponse(bidRequest: BidRequest, campaigns: List[Campaign]): Option[BidResponse] = {
    val filteredCampaigns = filterCampaigns(campaigns, bidRequest)

    getRandomValue(filteredCampaigns).map { campaign =>
      val banners = bidRequest
        .imp
        .map(imp => campaign.banners.filter(filterBySize(_, imp, campaign.bid)))
        .flatMap(getRandomValue)

      BidResponse(
        bidRequest.id,
        campaign.bid,
        campaign.id,
        banners
      )
    }
  }

  private def getRandomValue[T](list: List[T]): Option[T] = {
    if (list.nonEmpty) list.lift(random.nextInt(list.size)) else None
  }

  private def filterCampaigns(allCampaigns: List[Campaign], bidRequest: BidRequest): List[Campaign] = {
    val filterByCountry = (campaigns: List[Campaign]) => {
      countryCode(bidRequest).map(country => campaigns.filter(_.country == country)).getOrElse(campaigns)
    }

    val filterBySiteId = (campaigns: List[Campaign]) => {
      campaigns.filter(_.targeting.targetedSiteIds.exists(_ == bidRequest.site.id))
    }

    val filterByBanners = (campaigns: List[Campaign]) => {
      val campaignsSet = campaigns.toSet
      val impSize      = bidRequest.imp.size

      bidRequest
        .imp
        .flatMap(imp =>
          campaignsSet.filter(campaign => campaign.banners.exists(filterBySize(_, imp, campaign.bid)))
        )
        .groupBy(identity)
        .filter { case (_, values) => values.size == impSize}
        .keys
        .toList
    }

    println("hello")
    println(allCampaigns)

    (filterByCountry andThen filterBySiteId andThen filterByBanners) (allCampaigns)
  }

  private def countryCode(bidRequest: BidRequest): Option[String] = {
    import bidRequest._

    device.flatMap(_.geo).orElse(user.flatMap(_.geo)).flatMap(_.country)
  }

  private def filterBySize(banner: Banner, impression: Impression, bid: Double): Boolean = {
    import impression._

    validateValue(w, wmin, wmax, banner.width) &&
      validateValue(h, hmin, hmax, banner.height) &&
      bidFloor.forall(_ < bid)
  }

  private def validateValue(
    neededValueOption: Option[Int],
    minOption: Option[Int],
    maxOption: Option[Int],
    value: Int
  ): Boolean = {
    lazy val minMaxValidation = for {
      min <- minOption
      max <- maxOption
    } yield min >= value && max <= value

    neededValueOption.map(_ == value).orElse(minMaxValidation).getOrElse(false)
  }
}
