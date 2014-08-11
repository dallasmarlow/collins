package collins.softlayer

import scala.util.control.Exception.allCatch

import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.util.CharsetUtil.UTF_8
import org.jboss.netty.handler.codec.http.HttpRequest
import org.jboss.netty.handler.codec.http.HttpResponse
import org.jboss.netty.handler.codec.http.QueryStringEncoder

import com.twitter.finagle.Service
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.http.Http
import com.twitter.finagle.http.RequestBuilder
import com.twitter.finagle.http.Response
import com.twitter.util.Future

import models.Asset
import play.api.Application
import play.api.Plugin
import play.api.libs.json.Json
import play.api.libs.json.JsString
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import play.api.libs.json.JsArray
import play.api.libs.json.JsBoolean

class SoftLayerPlugin(app: Application) extends Plugin with SoftLayer {
  type ClientSpec = ClientBuilder.Complete[HttpRequest, HttpResponse]
  protected[this] val clientSpec: ClientSpec = ClientBuilder()
    .tlsWithoutValidation()
    .codec(Http())
    .hosts(SOFTLAYER_API_HOST)
    .hostConnectionLimit(1)

  override def enabled: Boolean = {
    SoftLayerConfig.pluginInitialize(app.configuration)
    SoftLayerConfig.enabled
  }

  override def onStart() {
    SoftLayerConfig.validateConfig()
  }
  override def onStop() {
  }

  override def username = SoftLayerConfig.username
  override def password = SoftLayerConfig.password

  // start plugin API
  override def isSoftLayerAsset(asset: Asset): Boolean = {
    asset.tag.startsWith("sl-")
  }
  override def softLayerId(asset: Asset): Option[Long] = isSoftLayerAsset(asset) match {
    case true => try {
      Some(asset.tag.split("-", 2).last.toLong)
    } catch {
      case _ => None
    }
    case false => None
  }

  private[this] val TicketExtractor = "^.* ([0-9]+).*$".r
  override def cancelServer(id: Long, reason: String = "No longer needed"): Future[Long] = {
    val encoder = new QueryStringEncoder(cancelServerPath(id))
    encoder.addParam("attachmentId", id.toString)
    encoder.addParam("reason", "No longer needed")
    encoder.addParam("content", reason)
    val url = softLayerUrl(encoder.toString())
    val request = RequestBuilder()
      .url(url)
      .buildGet();

    makeRequest(request) map { r =>
      val response = Response(r)
      val json = Json.parse(response.contentString)
      (json \ "error") match {
        case JsString(value) => allCatch[Long].opt {
          val TicketExtractor(number) = value
          number.toLong
        }.getOrElse(0L)
        case _ =>
          (json \ "id") match {
            case JsNumber(number) => number.longValue()
            case _ => 0L
          }
      }
    } handle {
      case e => 0L
    }
  }

  /*
  override def powerCycle(e: Asset): PowerStatus = {
    doPowerOperation(e, "/SoftLayer_Hardware_Server/%d/powerCycle.json")
  }
  */
  override def powerSoft(e: Asset): PowerStatus = {
    // This does not actually exist at SL
    doPowerOperation(e, "/SoftLayer_Hardware_Server/%d/powerSoft.json")
  }
  override def powerOff(e: Asset): PowerStatus = {
    doPowerOperation(e, "/SoftLayer_Hardware_Server/%d/powerOff.json")
  }
  override def powerOn(e: Asset): PowerStatus = {
    doPowerOperation(e, "/SoftLayer_Hardware_Server/%d/powerOn.json")
  }
  override def powerState(e: Asset): PowerStatus = {
    doPowerOperation(e, "/SoftLayer_Hardware_Server/%d/getServerPowerState.json", Some({ s =>
      s.replace("\"", "")
    }))
  }
  override def rebootHard(e: Asset): PowerStatus = {
    doPowerOperation(e, "/SoftLayer_Hardware_Server/%d/rebootHard.json")
  }
  override def rebootSoft(e: Asset): PowerStatus = {
    doPowerOperation(e, "/SoftLayer_Hardware_Server/%d/rebootSoft.json")
  }
  override def verify(e: Asset): PowerStatus = {
    Future.value(Failure("verify not implemented for softlayer"))
  }
  override def identify(e: Asset): PowerStatus = {
    Future.value(Failure("identify not implemented for softlayer"))
  }

  override def activateServer(id: Long): Future[Boolean] = {
    val url = softLayerUrl("/SoftLayer_Hardware_Server/%d/sparePool.json".format(id))
    val query = JsObject(Seq("parameters" -> JsArray(List(JsString("activate")))))
    val queryString = Json.stringify(query)
    val value = ChannelBuffers.copiedBuffer(queryString, UTF_8)
    val request = RequestBuilder()
      .url(url)
      .setHeader("Content-Type", "application/json")
      .setHeader("Content-Length", queryString.length.toString)
      .buildPost(value)
    makeRequest(request) map { r =>
      val response = Response(r)
      Json.parse(response.contentString) match {
        case JsBoolean(v) => v
        case o => false
      }
    } handle {
      case e => false
    }
  }

  override def setNote(id: Long, note: String): Future[Boolean] = {
    val url = softLayerUrl("/SoftLayer_Hardware_Server/%d/editObject.json".format(id))
    val query = JsObject(Seq("parameters" -> JsArray(List(JsObject(Seq("notes" -> JsString(note)))))))
    val queryString = Json.stringify(query)
    val value = ChannelBuffers.copiedBuffer(queryString, UTF_8)
    val request = RequestBuilder()
      .url(url)
      .setHeader("Content-Type", "application/json")
      .setHeader("Content-Length", queryString.length.toString)
      .buildPut(value)
    makeRequest(request) map { r =>
      true
    } handle {
      case e => false
    }
  }

  protected def makeRequest(request: HttpRequest): Future[HttpResponse] = {
    val client: Service[HttpRequest, HttpResponse] = clientSpec.build()
    client(request) ensure {
      client.release()
    }
  }

  private def doPowerOperation(e: Asset, url: String, captureFn: Option[String => String] = None): PowerStatus = {
    softLayerId(e).map { id =>
      val request = RequestBuilder()
        .url(softLayerUrl(url.format(id)))
        .setHeader("Accept", "application/json")
        .buildGet();
      makeRequest(request).map { r =>
        Response(r).contentString.toLowerCase match {
          case rl if rl.contains("at this time") => RateLimit
          case err if err.contains("error") => Failure()
          case responseString => captureFn match {
            case None => Success()
            case Some(fn) => Success(fn(responseString))
          }
        }
      } handle {
        case e => Failure("IPMI may not be enabled, internal error")
      }
    }.getOrElse(Future(Failure("Asset can not be managed with SoftLayer API")))
  }

}
