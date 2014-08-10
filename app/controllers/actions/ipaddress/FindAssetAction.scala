package controllers.actions.ipaddress

import scala.annotation.implicitNotFound
import controllers.SecureController
import controllers.actions.AssetAction
import controllers.actions.RequestDataHolder
import controllers.actions.SecureAction
import models.IpAddresses
import util.security.SecuritySpecification
import controllers.ResponseData

// Find the asset associated with an address
case class FindAssetAction(
  address: String,
  spec: SecuritySpecification,
  handler: SecureController
) extends SecureAction(spec, handler) with AssetAction with AddressActionHelper {

  case class ActionDataHolder(validAddress: String) extends RequestDataHolder

  override def validate(): Validation = withValidAddress(address) { validAddress =>
    Right(ActionDataHolder(validAddress))
  }

  override def execute(rd: RequestDataHolder) = rd match {
    case ActionDataHolder(validAddress) =>
      IpAddresses.findByAddress(validAddress) match {
        case Some(asset) =>
          ResponseData(Status.Ok, asset.toJsValue)
        case None =>
          handleError(RequestDataHolder.error404("No assets found with specified address"))
      }
  }

}
