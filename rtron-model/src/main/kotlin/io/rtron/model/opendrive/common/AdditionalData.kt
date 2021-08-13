/*
 * Copyright 2019-2022 Chair of Geoinformatics, Technical University of Munich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rtron.model.opendrive.common

data class UserData(
    // TODO: sequence
    var code: String = "",
    var value: String = ""
)

data class Include(
    var file: String = ""
)

data class DataQuality(
    var error: DataQualityError = DataQualityError(),
    var rawData: DataQualityRawData = DataQualityRawData()
)

data class DataQualityError(
    var xyAbsolute: Double = Double.NaN,
    var zAbsolute: Double = Double.NaN,
    var xyRelative: Double = Double.NaN,
    var zRelative: Double = Double.NaN
)

data class DataQualityRawData(
    var date: String = "",
    var source: EDataQualityRawDataSource = EDataQualityRawDataSource.CUSTOM,
    var sourceComment: String = "",
    var postProcessing: EDataQualityRawDataPostProcessing = EDataQualityRawDataPostProcessing.RAW,
    var postProcessingComment: String = ""
)

enum class EDataQualityRawDataSource { SENSOR, CADASTER, CUSTOM }
enum class EDataQualityRawDataPostProcessing { RAW, CLEANED, PROCESSED, FUSED }

data class AdditionalData(
    var userData: List<UserData> = listOf(),
    var include: List<Include> = listOf(),
    var dataQuality: DataQuality = DataQuality()
)
