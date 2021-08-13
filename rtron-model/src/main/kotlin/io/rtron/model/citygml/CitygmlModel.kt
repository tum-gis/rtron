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

package io.rtron.model.citygml

import io.rtron.model.AbstractModel
import org.citygml4j.model.core.AbstractCityObject
import org.xmlobjects.gml.model.feature.BoundingShape

/**
 * Implementation of the CityGML data model according to version 2.0.
 * See the [official page](https://www.ogc.org/standards/citygml) from the Open Geospatial Consortium (OGC) for more.
 *
 * @param name name of the model
 * @param boundingShape bounding shape containing coordinate reference system
 * @param cityObjects list of city objects
 */
class CitygmlModel(
    val name: String,
    val boundingShape: BoundingShape,
    val cityObjects: List<AbstractCityObject>
) : AbstractModel()
