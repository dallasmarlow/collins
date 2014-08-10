package models.lshw

import play.api.libs.json.Format
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson

object Cpu {
  import Json._
  implicit object CpuFormat extends Format[Cpu] {
    override def reads(json: JsValue) = Cpu(
      (json \ "CORES").as[Int],
      (json \ "THREADS").as[Int],
      (json \ "SPEED_GHZ").as[Double],
      (json \ "DESCRIPTION").as[String],
      (json \ "PRODUCT").as[String],
      (json \ "VENDOR").as[String])
    override def writes(cpu: Cpu) = JsObject(Seq(
      "CORES" -> toJson(cpu.cores),
      "THREADS" -> toJson(cpu.threads),
      "SPEED_GHZ" -> toJson(cpu.speedGhz),
      "DESCRIPTION" -> toJson(cpu.description),
      "PRODUCT" -> toJson(cpu.product),
      "VENDOR" -> toJson(cpu.vendor)))
  }
}

case class Cpu(
  cores: Int, threads: Int, speedGhz: Double, description: String, product: String, vendor: String) extends LshwAsset {
  import Cpu._
  override def toJsValue() = Json.toJson(this)
}
