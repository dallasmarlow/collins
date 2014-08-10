package controllers.actions.ipaddress

import models.Asset
import models.IpAddresses
import util.security.SecuritySpecification
import controllers.actions.SecureAction
import controllers.actions.AssetAction
import controllers.SecureController
import controllers.actions.RequestDataHolder
import controllers.ResponseData

// Get the addresses associated with an asset
case class FindByAssetAction(
  assetTag: String,
  spec: SecuritySpecification,
  handler: SecureController) extends SecureAction(spec, handler) with AssetAction with AddressActionHelper {

  case class ActionDataHolder(asset: Asset) extends RequestDataHolder

  override def validate(): Validation = withValidAsset(assetTag) { asset =>
    Right(ActionDataHolder(asset))
  }

  override def execute(rd: RequestDataHolder) = rd match {
    case ActionDataHolder(asset) =>
      val addresses = IpAddresses.findAllByAsset(asset).toJson
      ResponseData(Status.Ok, addresses)
  }
}
