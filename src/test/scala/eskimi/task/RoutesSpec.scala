package eskimi.task

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{MessageEntity, _}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import eskimi.task.JsonFormats._
import eskimi.task.BidMsg.Banner
import eskimi.task.BidMsg.In.Campaign.Targeting
import eskimi.task.BidMsg.In._
import eskimi.task.BidMsg.Out.BidResponse
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import monocle.macros.syntax.lens._



class CampaignMatcherSpec extends AnyFlatSpec with Matchers with ScalaFutures with ScalatestRouteTest with TestScope {


  it should "send 204 code if bid is not matched" in {
    setupCampaigns()

    val bidRequestEntity = Marshal(bidRequest).to[MessageEntity].futureValue
    val request = Post("/bids").withEntity(bidRequestEntity)

    request ~> bidRoutes ~> check {
      assert(status == StatusCodes.NoContent)
    }
  }

  it should "send bidResponse if bid is matched" in {
    setupCampaigns()

    val newBidRequest = bidRequest.lens(_.site.id).set("siteId5")

    val bidRequestEntity = Marshal(newBidRequest).to[MessageEntity].futureValue
    val request = Post("/bids").withEntity(bidRequestEntity)

    request ~> bidRoutes ~> check {
      assert(status == StatusCodes.OK)

      assert(entityAs[BidResponse].banner.nonEmpty)
    }
  }
}

sealed trait TestScope {
  val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system

  val biddingAgent = testKit.spawn(BiddingAgent.apply, "biddingAgent")
  lazy val bidRoutes = new Routes(biddingAgent).biddingRoutes

  val geoTest = Geo(
    country = Some("PL")
  )

  val bidRequest = BidRequest(
    id = "testRequestId1",
    imp = List(
      Impression(
        id = "imp1",
        wmin = None,
        wmax = None,
        hmin = None,
        hmax = None,
        h = Some(100),
        w = Some(50),
        bidFloor = Some(1)
      )
    ),
    site = Site(
      id = "siteId10",
      domain = "domain1"
    ),
    user = Some(User(
      id = "userId",
      geo = Some(geoTest))
    ),
    device = Some(Device(
      id = "testId",
      geo = Some(geoTest))
    )
  )

  def setupCampaigns(): Unit = {
    val campaigns = List(
      Campaign(
        id = "campaignId1",
        country = "LT",
        targeting = Targeting(
          targetedSiteIds = LazyList("siteId1, siteId2, siteId3")
        ),
        banners = List(
          Banner(
            id = 1,
            src = "https://test1.com",
            width = 300,
            height = 250
          ),
          Banner(
            id = 2,
            src = "https://test2.com",
            width = 100,
            height = 50
          )
        ),
        bid = 5d
      ),
      Campaign(
        id = "campaignId2",
        country = "PL",
        targeting = Targeting(
          targetedSiteIds = LazyList("siteId5", "siteId6", "siteId7")
        ),
        banners = List(
          Banner(
            id = 3,
            src = "https://test3.com",
            width = 300,
            height = 250
          ),
          Banner(
            id = 4,
            src = "https://test4.com",
            width = 50,
            height = 100
          )
        ),
        bid = 10d
      ),
    )

    biddingAgent ! SetCampaigns(campaigns)
  }
}
