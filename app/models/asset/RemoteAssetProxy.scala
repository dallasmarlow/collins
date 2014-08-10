package models.asset

import conversions.AssetFormat
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsValue
import play.api.libs.json.Json

abstract class RemoteAssetProxy(jsonAsset: JsValue) extends RemoteAsset {

  private[this] val asset = Json.fromJson[AssetView](jsonAsset)

  def id = asset.id
  def tag = asset.tag
  def state = asset.state
  def status = asset.status
  def asset_type = asset.asset_type
  def created = asset.created
  def updated = asset.updated
  def deleted = asset.deleted

  def toJsValue() = {
    AssetFormat.writes(asset) ++ JsObject(Seq("LOCATION" -> JsString(hostTag)))
  }
}
