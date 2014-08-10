package controllers

import actions.asset.UpdateRequestRouter.Matcher.StatusOnly
import views.html
import models.{Status => AStatus}
import play.api.http.{Status => StatusValues}
import java.util.Date
import controllers.actions.asset.FindSimilarAction
import controllers.actions.asset.UpdateForMaintenanceAction
import controllers.actions.asset.DeleteAttributeAction
import controllers.actions.asset.GetAction
import controllers.actions.asset.DeleteAction
import controllers.actions.asset.CreateAction
import controllers.actions.asset.FindAction
import controllers.actions.asset.UpdateAction
import controllers.actions.asset.UpdateForMaintenanceAction
import controllers.actions.asset.UpdateRequestRouter
import controllers.actions.asset.UpdateStatusAction
import models.PageParams


trait AssetApi {
  this: Api with SecureController =>

  // GET /api/asset/:tag
  def getAsset(tag: String, location: Option[String] = None) = GetAction(
    tag, location, Permissions.AssetApi.GetAsset, this
  )

  // GET /api/assets?params
  def getAssets(page: Int, size: Int, sort: String, sortField: String, details: String) =
    FindAction(PageParams(page, size, sort, sortField), Permissions.AssetApi.GetAssets, this)

  // PUT /api/asset/:tag
  def createAsset(tag: String) = CreateAction(Some(tag), None, Permissions.AssetApi.CreateAsset, this)

  // POST /api/asset/:tag
  def updateAsset(tag: String) = UpdateRequestRouter {
    case StatusOnly =>
      UpdateStatusAction(tag, Permissions.AssetApi.UpdateAssetStatus, this)
    case _ =>
      UpdateAction(tag, Permissions.AssetApi.UpdateAsset, this)
  }

  // POST /api/asset/:tag/status
  def updateAssetStatus(tag: String) =
    UpdateStatusAction(tag, Permissions.AssetApi.UpdateAssetStatus, this)

  def updateAssetForMaintenance(tag: String) = UpdateForMaintenanceAction(
    tag, Permissions.AssetApi.UpdateAssetForMaintenance, this
  )

  // DELETE /api/asset/attribute/:attribute/:tag
  def deleteAssetAttribute(tag: String, attribute: String) = DeleteAttributeAction(
    tag, attribute, Permissions.AssetApi.DeleteAssetAttribute, this
  )

  // DELETE /api/asset/:tag
  def deleteAsset(tag: String) = DeleteAction(tag, false, Permissions.AssetApi.DeleteAsset, this)

  //GET /api/asset/:tag/similar
  def similar(tag: String, page: Int, size: Int, sort: String) = 
    FindSimilarAction(tag, PageParams(page, size, sort, "sparse"), Permissions.AssetApi.GetAssets, this)

}
