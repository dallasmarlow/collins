package models.lldp

import play.api.libs.json.Format
import play.api.libs.json.JsObject
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson

object Interface {
  import Chassis._
  import Port._
  import Vlan._
  import Json._
  implicit object InterfaceFormat extends Format[Interface] {
    override def reads(json: JsValue) = Interface(
      (json \ "NAME").as[String],
      (json \ "CHASSIS").as[Chassis],
      (json \ "PORT").as[Port],
      (json \ "VLANS").as[Seq[Vlan]])
    override def writes(iface: Interface) = JsObject(Seq(
      "NAME" -> toJson(iface.name),
      "CHASSIS" -> toJson(iface.chassis),
      "PORT" -> toJson(iface.port),
      "VLANS" -> toJson(iface.vlans)))
  }
}

case class Interface(
  name: String, chassis: Chassis, port: Port, vlans: Seq[Vlan]) extends LldpAttribute {

  import Interface._

  def isPortLocal: Boolean = port.isLocal
  def portAsInt: Int = port.toInt
  def macAddress: String = chassis.macAddress

  override def toJsValue() = Json.toJson(this)
}
