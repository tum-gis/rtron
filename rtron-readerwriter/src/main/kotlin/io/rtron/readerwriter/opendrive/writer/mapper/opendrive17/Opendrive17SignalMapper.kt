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

package io.rtron.readerwriter.opendrive.writer.mapper.opendrive17

import arrow.core.Option
import io.rtron.model.opendrive.core.ECountryCode
import io.rtron.model.opendrive.signal.RoadSignals
import io.rtron.model.opendrive.signal.RoadSignalsSignal
import io.rtron.model.opendrive.signal.RoadSignalsSignalPositionInertial
import io.rtron.model.opendrive.signal.RoadSignalsSignalPositionRoad
import io.rtron.readerwriter.opendrive.writer.mapper.common.OpendriveCommonMapper
import org.asam.opendrive17.T_Road_Signals
import org.asam.opendrive17.T_Road_Signals_Signal
import org.asam.opendrive17.T_Road_Signals_Signal_PositionInertial
import org.asam.opendrive17.T_Road_Signals_Signal_PositionRoad
import org.mapstruct.Mapper
import org.mapstruct.NullValueCheckStrategy

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, uses = [OpendriveCommonMapper::class, Opendrive17CoreMapper::class, Opendrive17ObjectMapper::class])
abstract class Opendrive17SignalMapper {

    //
    // Signal
    //
    fun mapOptionRoadSignals(source: Option<RoadSignals>): T_Road_Signals? = source.fold({ null }, { mapRoadSignals(it) })
    abstract fun mapRoadSignals(source: RoadSignals): T_Road_Signals

    abstract fun mapRoadSignalsSignal(source: RoadSignalsSignal): T_Road_Signals_Signal

    fun mapOptionECountryCode(source: Option<ECountryCode>): String? = source.fold({ null }, { mapECountryCode(it) })
    abstract fun mapECountryCode(source: ECountryCode): String

    fun mapOptionRoadSignalsSignalPositionInertial(source: Option<RoadSignalsSignalPositionInertial>): T_Road_Signals_Signal_PositionInertial? = source
        .fold({ null }, { mapRoadSignalsSignalPositionInertial(it) })
    abstract fun mapRoadSignalsSignalPositionInertial(source: RoadSignalsSignalPositionInertial): T_Road_Signals_Signal_PositionInertial

    fun mapOptionRoadSignalsSignalPositionRoad(source: Option<RoadSignalsSignalPositionRoad>): T_Road_Signals_Signal_PositionRoad? = source
        .fold({ null }, { mapRoadSignalsSignalPositionRoad(it) })
    abstract fun mapRoadSignalsSignalPositionRoad(source: RoadSignalsSignalPositionRoad): T_Road_Signals_Signal_PositionRoad
}
