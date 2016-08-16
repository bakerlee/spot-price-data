package spotpricer

import java.time._
import java.util
import java.util.Date

import awscala.Region0.US_EAST_1
import awscala.ec2.EC2
import codes.reactive.scalatime._
import com.amazonaws.services.ec2.model.{DescribeSpotPriceHistoryRequest, SpotPrice}
import org.apache.commons.math3.stat.descriptive.rank.Percentile

import scala.collection.JavaConverters._
import scala.language.implicitConversions

case class InstanceTypes(duration: Duration = 1L.hour,
                         lookback: Period = 90.days,
                         confidence: Double = .99
                        )
                        (implicit ec2: EC2 = EC2().at(US_EAST_1)) {
  val Product = "Linux/UNIX (Amazon VPC)"
  private val _availabilityZones = allAvailabilityZones

  private val startTime = ZonedDateTime.now(ZoneId.of("UTC")).minus(lookback)
  private val request = new DescribeSpotPriceHistoryRequest()
    .withProductDescriptions(Product)
    .withStartTime(startTime)
    .withAvailabilityZone(_availabilityZones.head.name)
    .withInstanceTypes("c3.8xlarge")

  def bestInstanceTypes() = {
    val points: Seq[SpotPrice] =
      spotHistory(request)
        .sliding(duration.toMinutes.toInt)
        .map(_.maxBy(_.getSpotPrice.toDouble))
        .toSeq
        .sortBy(_.getSpotPrice.toDouble)

    println(points.take(3))
    println(points.takeRight(3))

    val map = points.map(point => point.getSpotPrice.toDouble -> point).toMap

    val pct = new Percentile().evaluate(map.keys.toArray, confidence)
    println(s"min=${map.minBy(_._1)}")
    println(s"target=$pct")
    println(s"max=${map.maxBy(_._1)}")
    pct
  }

  def spotHistory(request: DescribeSpotPriceHistoryRequest): Iterator[SpotPrice] = Iterator
    .iterate(ec2.describeSpotPriceHistory(request))(
      prev => ec2.describeSpotPriceHistory(new DescribeSpotPriceHistoryRequest().withNextToken(prev.getNextToken)))
    .takeWhile(_.getNextToken.nonEmpty)
    .flatMap(_.getSpotPriceHistory.asScala)

  implicit def OffsetDateTimeToUtilDate(dateTime: ZonedDateTime): util.Date = Date.from(dateTime.toInstant)

}
