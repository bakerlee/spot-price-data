import awscala.ec2.EC2
import awscala.{AvailabilityZone, ec2}

import scala.collection.JavaConverters._

package object spotpricer {

  def allAvailabilityZones(implicit ec2: EC2): Seq[AvailabilityZone] = {
    ec2.describeAvailabilityZones()
      .getAvailabilityZones.asScala
      .map(zone => AvailabilityZone(zone.getZoneName))
  }

}
