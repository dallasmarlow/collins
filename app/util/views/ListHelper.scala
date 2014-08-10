package util.views

import models.Asset
import models.Page
import models.asset.AssetView
import util.plugins.SoftLayer
import util.power.PowerComponent
import util.power.PowerUnits
import util.power.PowerUnits

// Mostly used with views/asset/list, also for comprehensions
object ListHelper {
  def showHostname(assets: Page[AssetView]): Boolean = {
    assets.items.find(_.getHostnameMetaValue.isDefined).map(_ => true).getOrElse(false)
  }
  def showSoftLayerLink(assets: Page[AssetView]): Boolean = {
    SoftLayer.pluginEnabled { plugin =>
      assets.items.collectFirst { case asset: Asset if (plugin.isSoftLayerAsset(asset)) => true }.getOrElse(false)
    }.getOrElse(false)
  }
  def getPowerComponentsInOrder(units: PowerUnits): Seq[PowerComponent] = {
    val components = units.flatMap { unit =>
      unit.components
    }
    components.toSeq.sorted
  }
  def getPowerComponentsInOrder(): Seq[PowerComponent] = {
    getPowerComponentsInOrder(PowerUnits())
  }
}
