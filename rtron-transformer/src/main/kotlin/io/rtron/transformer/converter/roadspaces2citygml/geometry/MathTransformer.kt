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

package io.rtron.transformer.converter.roadspaces2citygml.geometry

import io.rtron.math.transform.Affine3D
import org.citygml4j.core.model.core.TransformationMatrix4x4 as GmlTransformationMatrix4x4

/**
 * Converts a [GmlTransformationMatrix4x4] object from an affine matrix.
 */
fun Affine3D.toGmlTransformationMatrix4x4(): GmlTransformationMatrix4x4 = GmlTransformationMatrix4x4.ofRowMajorList(toDoubleList())
