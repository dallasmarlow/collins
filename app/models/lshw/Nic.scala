package models.lshw

import play.api.libs.json.Format
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson
import util.BitStorageUnit

object Nic {
  import Json.toJson
  implicit object NicFormat extends Format[Nic] {
    override def reads(json: JsValue) = Nic(
      BitStorageUnit((json \ "SPEED").as[Long]),
      (json \ "MAC_ADDRESS").as[String],
      (json \ "DESCRIPTION").as[String],
      (json \ "PRODUCT").as[String],
      (json \ "VENDOR").as[String]
    )
    override def writes(nic: Nic) = JsObject(Seq(
      "SPEED" -> toJson(nic.speed.inBits),
      "SPEED_S" -> toJson(nic.speed.inBits.toString),
      "SPEED_HUMAN" -> toJson(nic.speed.toHuman),
      "MAC_ADDRESS" -> toJson(nic.macAddress),
      "DESCRIPTION" -> toJson(nic.description),
      "PRODUCT" -> toJson(nic.product),
      "VENDOR" -> toJson(nic.vendor)
    ))
  }
}

case class Nic(
  speed: BitStorageUnit, macAddress: String, description: String, product: String, vendor: String
) extends LshwAsset {
  import Nic._
  override def toJsValue() = Json.toJson(this)
}
