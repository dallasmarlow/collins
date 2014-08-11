package controllers.actions.ipaddress

import controllers.actions.RequestDataHolder
import controllers.actions.SecureAction
import models.IpAddresses
import models.shared.IpAddressConfig
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import util.IpAddress

trait AddressActionHelper { self: SecureAction =>
  class AddressDecorator(addresses: Seq[IpAddresses]) {
    import models.Conversions._
    def toJson = JsObject(Seq("ADDRESSES" -> Json.toJson(addresses)))
  }

  implicit def seq2json(addresses: Seq[IpAddresses]): AddressDecorator =
    new AddressDecorator(addresses)

  def withValidAddress(a: String)(f: String => Validation): Validation =
    IpAddress.toOptLong(a) match {
      case Some(_) => f(a)
      case None => Left(RequestDataHolder.error400(
        "Invalid IP address '%s' specified".format(a)))
    }

  def convertPoolName(name: String, emptyToDef: Boolean = false) = name match {
    case default if default.equalsIgnoreCase(IpAddressConfig.DefaultPoolName) =>
      ""
    case empty if empty.isEmpty && emptyToDef =>
      IpAddressConfig.DefaultPoolName
    case other => other.toUpperCase
  }
}
