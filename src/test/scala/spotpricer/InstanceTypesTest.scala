package spotpricer

import awscala.Region0.US_EAST_1
import awscala.ec2.EC2
import codes.reactive.scalatime._
import org.scalatest.FlatSpec

class InstanceTypesTest extends FlatSpec {

  it should "return best instances" in {
    InstanceTypes().bestInstanceTypes()


  }

}
