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
    val logFilePath =  "./logs/logs.txt"
    val logFile = new PrintWriter( logFilePath )
    
    val from = 1 //start id
    val to = 30 // end id

    val urls: Seq[Future[String]] = List.range( from, to + 1) map {
      id => Future{
          val result = getTargets( id, SERVER_URL )
          val resultJson = parse( result )
          val bodyJson = ( resultJson \ "result" )
          val rCode = ( bodyJson \ "code" ).extract[String]
          val rValue = ( bodyJson \ "value" ).extract[String]
          val resultStatus = messageBuilder( rCode )
          val message = s"$id\t$rValue\t$resultStatus"
          write2LogByLine( logFile, message )
          ""
        }
    }

    val futureSeq = Future.sequence(urls)
    Await.result(futureSeq, Duration.Inf)
    logFile.close()
  }

  def getTargets( id: Int, url: String ): String = {
    val req = new HttpGet
    val client = HttpClientBuilder.create().build()
    req.setURI( new URI( url + id ))
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

