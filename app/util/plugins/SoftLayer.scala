package util.plugins

import collins.provisioning.ProvisionerConfig
import collins.softlayer.SoftLayerConfig
import collins.softlayer.SoftLayerPlugin

import models.Asset
import play.api.Play

object SoftLayer {

  def plugin: Option[SoftLayerPlugin] = pluginEnabled

  def pluginEnabled: Option[SoftLayerPlugin] = {
    Play.maybeApplication.flatMap { app =>
      app.plugin[SoftLayerPlugin].filter(_.enabled)
    }
  }

  def pluginEnabled[T](fn: SoftLayerPlugin => T): Option[T] = {
    pluginEnabled.map { p =>
      fn(p)
    }
  }

  def ticketLink(id: String): Option[String] = {
    pluginEnabled.flatMap { p =>
      try {
        Some(p.ticketUrl(id.toLong))
      } catch {
        case _ => None
      }
    }
  }

  def assetLink(asset: models.asset.AssetView): Option[String] = asset match {
    case a: models.Asset => {
      pluginEnabled.flatMap { p =>
        p.softLayerUrl(a)
      }
    }
    case _ => None
  }

  def canCancel(asset: Asset): Boolean = {
    validAsset(asset) && SoftLayerConfig.allowedCancelStatus.contains(asset.status)
  }

  def canActivate(asset: Asset): Boolean = {
    validAsset(asset) && asset.isIncomplete
  }

  protected def validAsset(asset: Asset): Boolean = {
    plugin.isDefined &&
      plugin.map(_.isSoftLayerAsset(asset)).getOrElse(false) &&
      ProvisionerConfig.allowedType(asset.asset_type)
  }

}
