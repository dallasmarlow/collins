package controllers.actions.logs

import models.Asset
import models.AssetLog
import models.Page
import models.PageParams
import models.Conversions.AssetLogFormat
import util.security.SecuritySpecification
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import controllers.actions.SecureAction
import controllers.actions.AssetAction
import controllers.actions.RequestDataHolder
import controllers.SecureController
import controllers.ResponseData

case class FindAction(
  assetTag: Option[String],
  pageParams: PageParams,
  filter: String,
  spec: SecuritySpecification,
  handler: SecureController) extends SecureAction(spec, handler) with AssetAction {

  case class ActionDataHolder(asset: Option[Asset], params: PageParams, filter: String) extends RequestDataHolder

  override def validate(): Validation = {
    assetTag.map { a =>
      withValidAsset(a) { asset =>
        Right(ActionDataHolder(Some(asset), pageParams, filter))
      }
    }.getOrElse(Right(ActionDataHolder(None, pageParams, filter)))
  }

  override def execute(rd: RequestDataHolder) = rd match {
    case adh @ ActionDataHolder(asset, params, filter) =>
      val logs = getLogs(adh)
      val pageMap = getPaginationMap(logs)
      ResponseData(Status.Ok, JsObject(pageMap ++ Seq(
        "Data" -> Json.toJson(logs.items))), logs.getPaginationHeaders)
  }

  protected def getLogs(adh: ActionDataHolder): Page[AssetLog] = {
    val ActionDataHolder(asset, params, filter) = adh
    AssetLog.list(asset, params.page, params.size, params.sort.toString, filter)
  }

  protected def getPaginationMap(logs: Page[AssetLog]) = logs.getPaginationJsObject

}
