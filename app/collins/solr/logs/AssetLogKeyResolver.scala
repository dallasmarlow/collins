package collins.solr

import SolrKeyFlag.NotSortable
import SolrKeyFlag.SingleValued
import SolrKeyFlag.Sortable
import SolrKeyFlag.Static
import collins.solr.SolrKeyFlag.namedboolean2boolean
import collins.solr.UpperCaseString.string2UpperCaseString
import models.AssetMeta.ValueType.Integer
import models.AssetMeta.ValueType.String
import models.logs.LogMessageType

object AssetLogKeyResolver extends SolrKeyResolver {

  val messageTypeKey = new SolrKey("MESSAGE_TYPE", String, Static, SingleValued, Sortable, Set("SEVERITY")) with EnumKey {
    def lookupById(id: Int) = try {
      Some(LogMessageType(id).toString)
    } catch {
      case _ => None
    }

    def lookupByName(name: String) = try {
      Some(LogMessageType.withName(name.toUpperCase).toString)
    } catch {
      case _ => None
    }
  }

  val keys = List(
    SolrKey("ID", Integer, Static, SingleValued, Sortable),
    SolrKey("MESSAGE", String, Static, SingleValued, NotSortable),
    SolrKey("CREATED", String, Static, SingleValued, Sortable, Set("DATE")),
    SolrKey("ASSET_ID", Integer, Static, SingleValued, Sortable),
    SolrKey("ASSET_TAG", String, Static, SingleValued, Sortable), 
    messageTypeKey
  )

  def docSpecificKey(rawKey: UpperCaseString): Option[SolrKey] = {
    keys.find{_ matches rawKey}
  }
}
