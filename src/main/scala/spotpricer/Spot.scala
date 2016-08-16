package spotpricer

import java.io.Serializable
import java.time.{Duration, OffsetDateTime}
import java.util.Date

import com.amazonaws.services.ec2.AmazonEC2Client
import com.amazonaws.services.ec2.model.{DescribeSpotPriceHistoryRequest, SpotPrice}
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.math.BigDecimal.RoundingMode

object Spot {
  val ZonesOfInterest = Seq("us-east-1a","us-east-1b","us-east-1c","us-east-1e")
  val Product = "Linux/UNIX (Amazon VPC)"

  def main(args: Array[String]) {
    val results: ListBuffer[Seq[Any]] = ListBuffer(Seq("Instance Type", "Zone", "95%", "99%", "99.9%"))
    val ec2 = new AmazonEC2Client()

    ZonesOfInterest.foreach { zone =>
      val request = new DescribeSpotPriceHistoryRequest()
        .withProductDescriptions(Product)
        .withStartTime(Date.from(OffsetDateTime.now().minusDays(10).toInstant))
        .withAvailabilityZone(zone)

      print(s"Getting all pages for $zone")
      val points = Stream.iterate(ec2.describeSpotPriceHistory(request)) { prev =>
        print(".")
        ec2.describeSpotPriceHistory(request.withNextToken(prev.getNextToken))
      }
        .takeWhile(_.getNextToken.nonEmpty)
        .flatMap(_.getSpotPriceHistory.asScala)
        .groupBy(_.getInstanceType)

      points.foreach { case (instanceType, prices) =>
        val times = sortedWeighted(prices)
        val line: Seq[Serializable] = Seq(instanceType, zone, percentile(times, .95), percentile(times, .99), percentile(times, .999))
        results += line
      }
      println()
    }
    println(Tabulator.format(results.sortBy(_ (0).toString)))
  }

  def percentile(observations: Seq[(BigDecimal, Long)], d: Double): BigDecimal = {
    val total = observations.map(_._2).sum
    val t = total * d
    val cumulative = observations
      .scanLeft((observations.head._1, observations.head._2, 0l))((prev, next) => (next
        ._1, next._2, prev._3 + next._2))
    val list = cumulative.takeWhile(_._3 < t)
    val sum = list.map(tuple => tuple._1 * tuple._2).sum
    val time = list.map(_._2).sum
    val weighted = (sum / time).setScale(3, RoundingMode.HALF_UP)
    weighted
  }

  def sortedWeighted(points: Seq[SpotPrice]): Seq[(BigDecimal, Long)] = {
    points.zip(points.drop(1)).map { tuple =>
      (BigDecimal(tuple._2.getSpotPrice).setScale(3, RoundingMode.HALF_UP),
        Duration.between(tuple._2.getTimestamp.toInstant, tuple._1.getTimestamp.toInstant).getSeconds)
    }.sortBy(_._1)
  }

  implicit class Currency(num: BigDecimal) {
    def formatCurrency(): String = num.formatted(f"$$$num%.3f")
  }

}
