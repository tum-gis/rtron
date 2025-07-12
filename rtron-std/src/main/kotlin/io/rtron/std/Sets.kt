/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.std

import com.google.common.collect.Sets as GSets

/**
 * Returns the set of all possible subsets.
 * See wikipedia article of [power set](http://en.wikipedia.org/wiki/Power_set).
 *
 * @receiver the set for which all possible subsets are constructed
 * @return all possible subsets
 */
fun <T: Any> Set<T>.powerSet(): Set<Set<T>> = GSets.powerSet(this)

/**
 * Returns all possible subsets of [this] with [size].
 *
 * @receiver the set for which all possible subsets with [size] are constructed
 * @param size the number of elements per combination
 * @return all possible subsets of provided [size]
 */
fun <T: Any> Set<T>.combinations(size: Int): Set<Set<T>> = GSets.combinations(this, size)
