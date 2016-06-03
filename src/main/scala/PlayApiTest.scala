import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils
import org.json4s.native.JsonMethods._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import java.net.URI
import scala.concurrent.duration._
import java.io._

object PlayApiTest { 
  def main(args: Array[String]):Unit = {
    val SERVER_URL = args(0)
    val logFilePath =  "./logs/logs.txt"
    val logFile = new PrintWriter( logFilePath )
    
    val from = 1 //start id
    val to = 100 // end id
    val numId = to - from + 1

    val urls: Seq[Future[String]] = List.fill( numId )( SERVER_URL ).zipWithIndex map{
      case (v, i) =>
        val id = from + i
        val url = v + id
        Future{
          getTargets(url, logFile)
        }
    }
    val futureSeq = Future.sequence(urls)
    Await.result(futureSeq, Duration.Inf)
    logFile.close()
  }

  def getTargets( url: String, printWriter : PrintWriter ):String = {
    val req = new HttpGet
    val client = HttpClientBuilder.create().build()
    req.setURI(new URI( url ))
    req.setHeader(new BasicHeader("Accept","application/json"))
    val res = client.execute(req)
    val body = EntityUtils.toString(res.getEntity)
    val resultStatus = messageBuilder(body)
    val message = s"$url\t$resultStatus"
    write2LogByLine( printWriter, message )
    ""
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

