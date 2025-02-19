/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.readerwriter.opendrive.reader.mapper.opendrive18

import arrow.core.Option
import io.rtron.model.opendrive.signal.RoadSignals
import io.rtron.model.opendrive.signal.RoadSignalsSignalPositionInertial
import io.rtron.model.opendrive.signal.RoadSignalsSignalPositionRoad
import io.rtron.readerwriter.opendrive.reader.mapper.common.OpendriveCommonMapper
import org.asam.opendrive18.T_Road_Signals
import org.asam.opendrive18.T_Road_Signals_Signal_PositionInertial
import org.asam.opendrive18.T_Road_Signals_Signal_PositionRoad
import org.mapstruct.Mapper
import org.mapstruct.NullValueCheckStrategy

@Mapper(
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
    uses = [OpendriveCommonMapper::class, Opendrive18CoreMapper::class, Opendrive18ObjectMapper::class],
    imports = [Option::class],
)
abstract class Opendrive18SignalMapper {
    //
    // Signal
    //
    abstract fun mapSignals(source: T_Road_Signals): RoadSignals

    abstract fun mapSignalsSignalPositionInertial(source: T_Road_Signals_Signal_PositionInertial): RoadSignalsSignalPositionInertial

    abstract fun mapSignalsSignalPositionRoad(source: T_Road_Signals_Signal_PositionRoad): RoadSignalsSignalPositionRoad
}
