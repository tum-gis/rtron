/*
 * Copyright 2019-2020 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.math.linear

import org.apache.commons.math3.linear.SingularValueDecomposition as CMSingularValueDecomposition


/**
 * Performs a singular value decomposition (SVD) on a [RealMatrix].
 * See the wikipedia article on [singular value decomposition](https://en.wikipedia.org/wiki/Singular_value_decomposition).
 *
 * @param matrix matrix to be decomposed
 */
class SingularValueDecomposition(
        matrix: RealMatrix
) {

    // Properties and Initializers
    private val _singularValueDecomposition by lazy { CMSingularValueDecomposition(matrix.toRealMatrixCM()) }

    val rank by lazy { _singularValueDecomposition.rank }
    val norm by lazy { _singularValueDecomposition.norm }

    val matrixU by lazy { RealMatrix(_singularValueDecomposition.u) }
    val matrixS by lazy { RealMatrix(_singularValueDecomposition.s) }
    val matrixV by lazy { RealMatrix(_singularValueDecomposition.v) }
    val matrixVT by lazy { RealMatrix(_singularValueDecomposition.vt) }
}
