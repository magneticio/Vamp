package io.magnetic.vamp_core.model.artifact

abstract class Blueprint extends Artifact

case class DefaultBlueprint(name: String, clusters: List[Cluster], endpoints: List[Port], parameters: Map[Trait.Name, Any]) extends Blueprint

case class BlueprintReference(name: String) extends Blueprint with Reference


case class Cluster(name: String, services: List[Service], sla: Option[Sla]) extends Artifact


case class Service(breed: Breed, scale: Option[Scale], routing: Option[Routing])


trait Sla extends Artifact

case class SlaReference(name: String, escalations: List[Escalation]) extends Reference with Sla

case class DefaultSla(name: String, `type`: String, escalations: List[Escalation], parameters: Map[String, Any]) extends Sla with Type


trait Escalation extends Artifact

case class EscalationReference(name: String) extends Reference with Escalation

case class DefaultEscalation(name: String, `type`: String, parameters: Map[String, Any]) extends Escalation with Type


trait Scale extends Artifact

case class ScaleReference(name: String) extends Reference with Scale

case class DefaultScale(name: String, cpu: Double, memory: Double, instances: Int) extends Scale


trait Routing extends Artifact

case class RoutingReference(name: String) extends Reference with Routing

case class DefaultRouting(name: String, weight: Option[Int], filters: List[Filter]) extends Routing


trait Filter extends Artifact

case class FilterReference(name: String) extends Reference with Filter

case class DefaultFilter(name: String, condition: String) extends Filter
