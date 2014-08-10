package controllers
package actions
package ipaddress

import scala.annotation.implicitNotFound
import scala.math.BigDecimal.int2bigDecimal

import controllers.SecureController
import controllers.actions.AssetAction
import controllers.actions.RequestDataHolder
import controllers.actions.SecureAction
import models.Asset
import models.IpAddresses
import play.api.data.Form
import play.api.libs.json.JsNumber
import play.api.libs.json.JsObject
import util.ApiTattler
import util.security.SecuritySpecification
import validators.ParamValidation

// Delete addressed for an asset, optionally by pool
case class DeleteAction(
  assetTag: String,
  spec: SecuritySpecification,
  handler: SecureController
) extends SecureAction(spec, handler) with AssetAction with ParamValidation {

  case class ActionDataHolder(asset: Asset, pool: Option[String]) extends RequestDataHolder

  val dataForm = Form(
    "pool" -> validatedOptionalText(1)
  )

  override def validate(): Validation = withValidAsset(assetTag) { asset =>
    val pool: Option[String] = dataForm.bindFromRequest()(request).fold(
      err => None,
      str => str
    )
    Right(ActionDataHolder(asset, pool))
  }

  override def execute(rd: RequestDataHolder) = rd match {
    case ActionDataHolder(asset, pool) =>
      val deleted = IpAddresses.deleteByAssetAndPool(asset, pool)
      ApiTattler.notice(asset, userOption, "Deleted %d IP addresses".format(deleted))
      ResponseData(Status.Ok, JsObject(Seq("DELETED" -> JsNumber(deleted))))
  }
}
