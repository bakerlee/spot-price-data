import awscala.s3.S3
import com.amazonaws.services.s3.model.ListObjectsRequest

object Pipeline {

  def main(args: Array[String]) {


    val s3 = S3.at(awscala.Region.US_EAST_1)
    val objs = s3.listObjects(new ListObjectsRequest("big-regression-take5", "", "", "/", 1000))
    objs
  }
}
