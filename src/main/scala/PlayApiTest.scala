import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import java.net.URI
import scala.concurrent.duration._
import java.io._

object PlayApiTest {
  implicit val formats = DefaultFormats

  def main(args: Array[String]):Unit = {
    val SERVER_URL = args(0)
    val indexTestAPI = args(1)
    val logFilePath =  "./logs/logs.txt"
    val logFile = new PrintWriter( logFilePath )

    print("Where do you want to start from ?: ")
    val from = scala.io.StdIn.readLine().toInt //start
    print("Where do you want to end to ?: ")
    val to = scala.io.StdIn.readLine().toInt// end

    val urls: Seq[Future[String]] = List.range( from, to + 1) map {
      id => Future{
          val testUrl = indexTestAPI match {
            case "1" => SERVER_URL + id + "?tableNum=09"
            case "2" | "3" => SERVER_URL + id
          }

          val result = getTargets( testUrl )
          val resultJson = parse( result )
          val bodyJson = ( resultJson \ "result" )

          val msg = indexTestAPI match {
            case "1" =>
              val rCode = ( bodyJson \ "code" ).extract[String]
              val rValue = ( bodyJson \ "value" ).extract[String]
              val resultStatus = messageBuilder( rCode )
              val message = s"$id\t$rValue\t$resultStatus"
              message
            case "2" =>
              val rGtin = ( bodyJson \ "gtin" ).extract[String]
              val rMsg = ( bodyJson \ "msg" ).extract[String]
              val message = s"$id\t$rGtin\t$rMsg"
              message
            case "3" =>
              val rUuid = ( bodyJson \ "uuid" ).extract[String]
              val rMsg = ( bodyJson \ "msg" ).extract[String]
              val message = s"$id\t$rUuid\t$rMsg"
              message
          }
          println(msg)
          write2LogByLine( logFile, msg )
          ""
        }
    }

    val futureSeq = Future.sequence(urls)
    Await.result(futureSeq, Duration.Inf)
    logFile.close()
    println("END")
  }

  def getTargets( url: String ): String = {
    val req = new HttpGet
    val client = HttpClientBuilder.create().build()
    req.setURI( new URI( url ))
    req.setHeader(new BasicHeader("Accept","application/json"))
    val res = client.execute(req)
    val body = EntityUtils.toString(res.getEntity)
    body
  }

  def write2LogByLine( f : PrintWriter, m : String ) = f.write( m + "\n" )

  def messageBuilder( code: String ): String = code match{
    case "1" => "SUCCESS"
    case "2" => "FAIL"
    case "3" => "URL_NOT_EXIST"
    case "4" => "ID_NOT_EXIST"
    case _ => "UNKNOWN"
  }
}

