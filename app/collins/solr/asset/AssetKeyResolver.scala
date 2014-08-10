package collins.solr

import SolrKeyFlag.Dynamic
import SolrKeyFlag.MultiValued
import SolrKeyFlag.NotSortable
import SolrKeyFlag.SingleValued
import SolrKeyFlag.Sortable
import SolrKeyFlag.Static
import collins.solr.SolrKeyFlag.namedboolean2boolean
import collins.solr.UpperCaseString.UppercaseString2String
import collins.solr.UpperCaseString.string2UpperCaseString
import models.AssetMeta
import models.AssetMeta.ValueType.Integer
import models.AssetMeta.ValueType.String
import models.AssetType
import models.IpmiInfo.Enum.IpmiAddress
import models.IpmiInfo.Enum.IpmiGateway
import models.IpmiInfo.Enum.IpmiNetmask
import models.IpmiInfo.Enum.IpmiPassword
import models.IpmiInfo.Enum.IpmiUsername
import models.State
import models.Status

object AssetKeyResolver extends SolrKeyResolver {

  /**
   * each key is an "incoming" field from a query, the ValueType is the
   * expected type of the key, and the Boolean indicates whether the key in
   * Solr is static(false) or dynamic(true)
   *
   * NOTE - For now, any single-valued field that needs to be sortable has to be explicitly declared
   */
  lazy val nonMetaKeys: Seq[SolrKey] = List(
    SolrKey("ID", Integer, Static, SingleValued, Sortable),
    SolrKey("TAG", String, Static, SingleValued, Sortable),
    SolrKey("CREATED", String, Static, SingleValued, Sortable),
    SolrKey("UPDATED", String, Static, SingleValued, Sortable),
    SolrKey("DELETED", String, Static, SingleValued, Sortable),
    SolrKey("IP_ADDRESS", String, Static, MultiValued, NotSortable),
    SolrKey("PRIMARY_ROLE", String, Static, SingleValued, Sortable),
    SolrKey("HOSTNAME", String, Static, SingleValued, Sortable),
    SolrKey(IpmiAddress.toString, String, Dynamic, SingleValued, Sortable),
    SolrKey(IpmiUsername.toString, String, Dynamic, SingleValued, Sortable),
    SolrKey(IpmiPassword.toString, String, Dynamic, SingleValued, Sortable),
    SolrKey(IpmiGateway.toString, String, Dynamic, SingleValued, Sortable),
    SolrKey(IpmiNetmask.toString, String, Dynamic, SingleValued, Sortable)) ++ Solr.plugin.map { _.assetSerializer.generatedFields }.getOrElse(List())

  val typeKey = new SolrKey("TYPE", String, Static, SingleValued, Sortable, Set("ASSETTYPE")) with EnumKey {
    def lookupByName(value: String) = AssetType.findByName(value.toUpperCase).map(_.name)
    def lookupById(value: Int) = AssetType.findById(value).map(_.name)

  }

  val statusKey = new SolrKey("STATUS", String, Static, SingleValued, Sortable) with EnumKey {
    def lookupByName(value: String) = Status.findByName(value).map { _.name }
    def lookupById(value: Int) = Status.findById(value).map { _.name }
  }

  val stateKey = new SolrKey("STATE", String, Static, SingleValued, Sortable) with EnumKey {
    def lookupByName(value: String) = State.findByName(value).map { _.name }
    def lookupById(value: Int) = State.findById(value).map { _.name }
  }

  val enumKeys = typeKey :: statusKey :: stateKey :: Nil

  def docSpecificKey(key: UpperCaseString): Option[SolrKey] = {
    nonMetaKeys.find(_ matches key)
      .orElse(enumKeys.find(_ matches key))
      .orElse(AssetMeta.findByName(key).map { _.getSolrKey })
  }

}
