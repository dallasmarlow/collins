package controllers

import models.Truthy
import controllers.actions.ipaddress.FindAssetAction
import controllers.actions.ipaddress.FindAssetsByPoolAction
import controllers.actions.ipaddress.FindByAssetAction
import controllers.actions.ipaddress.GetPoolsAction
import controllers.actions.ipaddress.UpdateAction
import controllers.actions.ipaddress.DeleteAction
import controllers.actions.ipaddress.CreateAction

trait IpAddressApi {
  this: Api with SecureController =>

  // POST /api/asset/:tag/address
  def updateAddress(tag: String) =
    UpdateAction(tag, Permissions.IpAddressApi.UpdateAddress, this)

  // PUT /api/asset/:tag/address
  def allocateAddress(tag: String) =
    CreateAction(tag, Permissions.IpAddressApi.AllocateAddress, this)

  // GET /api/assets/with/addresses/in/:pool
  def assetsFromPool(pool: String) =
    FindAssetsByPoolAction(pool, Permissions.IpAddressApi.AssetsFromPool, this)

  // GET /api/asset/with/address/:address
  def assetFromAddress(address: String) =
    FindAssetAction(address, Permissions.IpAddressApi.AssetFromAddress, this)

  // GET /api/asset/:tag/addresses
  def getForAsset(tag: String) =
    FindByAssetAction(tag, Permissions.IpAddressApi.GetForAsset, this)

  // GET /api/address/pools
  def getAddressPools(all: String) =
    GetPoolsAction(Truthy(all), Permissions.IpAddressApi.GetAddressPools, this)

  // DELETE /api/asset/:tag/addresses
  def purgeAddresses(tag: String) =
    DeleteAction(tag, Permissions.IpAddressApi.PurgeAddresses, this)
}
