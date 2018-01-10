package org.thp.cortex.models

import javax.inject.{ Inject, Singleton }

import scala.util.Try

import play.api.libs.json.{ JsObject, Json }

import org.elastic4play.models.JsonFormat.enumFormat
import org.elastic4play.models.{ AttributeDef, EntityDef, HiveEnumeration, ModelDef, AttributeFormat ⇒ F, AttributeOption ⇒ O }

object JobStatus extends Enumeration with HiveEnumeration {
  type Type = Value
  val Waiting, InProgress, Success, Failure = Value
  implicit val reads = enumFormat(this)
}

trait JobAttributes { _: AttributeDef ⇒
  val analyzerDefinitionId = attribute("analyzerDefinitionId", F.stringFmt, "Analyzer definition id", O.readonly)
  val analyzerId = attribute("analyzerId", F.stringFmt, "Analyzer id", O.readonly)
  val analyzerName = attribute("analyzerName", F.stringFmt, "Analyzer name", O.readonly)
  val organizationId = attribute("organizationId", F.stringFmt, "Organization ID", O.readonly)
  val status = attribute("status", F.enumFmt(JobStatus), "Status of the job")
  val startDate = optionalAttribute("startDate", F.dateFmt, "Analysis start date")
  val endDate = optionalAttribute("endDate", F.dateFmt, "Analysis end date")
  val dataType = attribute("dataType", F.stringFmt, "Type of the artifact", O.readonly)
  val data = optionalAttribute("data", F.stringFmt, "Content of the artifact", O.readonly)
  val attachment = optionalAttribute("attachment", F.attachmentFmt, "Artifact file content", O.readonly)
  val tlp = attribute("tlp", TlpAttributeFormat, "TLP level", 2L)
  val message = optionalAttribute("message", F.textFmt, "Message associated to the analysis")
  val errorMessage = optionalAttribute("message", F.textFmt, "Message returned by the analyzer when it fails")
  val parameters = attribute("parameters", F.textFmt, "Parameters for this job", "{}")
}

@Singleton
class JobModel @Inject() () extends ModelDef[JobModel, Job]("job", "Job", "/job") with JobAttributes with AuditedModel {

}

class Job(model: JobModel, attributes: JsObject) extends EntityDef[JobModel, Job](model, attributes) with JobAttributes {
  val params: JsObject = Try(Json.parse(parameters()).as[JsObject]).getOrElse(JsObject.empty)

  override def toJson: JsObject = super.toJson + ("date" -> Json.toJson(createdAt))
}