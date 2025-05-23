package io.rtron.transformer.modifiers.opendrive.applier

import kotlinx.serialization.Serializable

@Serializable
data class OpendriveApplierRules(
    val roads: HashMap<String, OpendriveApplierRoadRules> = hashMapOf(),
)

@Serializable
data class OpendriveApplierRoadRules(
    val name: String?,
)
