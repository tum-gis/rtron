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

package io.rtron.std

import arrow.core.Either

/**
 * Returns the value of map[key] as [Either.Right], if it exists, or as [Either.Left] otherwise.
 *
 * @receiver the map on which the [key] is requested
 * @param key requested key to be accessed
 * @return [Either] of the request
 */
fun <K : Any, V : Any?> Map<K, V>.getValueEither(key: K): Either<NoSuchElementException, V> {
    val value = this[key] ?: return Either.Left(NoSuchElementException(key.toString()))
    return Either.Right(value)
}

data class NoSuchElementException(val elementName: String) : BaseException("No element found with name $elementName.")
