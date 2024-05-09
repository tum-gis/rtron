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

package io.rtron.std

import kotlin.reflect.KProperty

/**
 * Read-only property of type [T] with context information, whether the property contains a default value.
 * Usable as property delegate.
 *
 * @param value actual value of the property
 * @param isDefault true, if value constitutes a default value
 */
open class Property<T>(value: T, isDefault: Boolean = false) {
    // Properties and Initializers
    var value: T = value
        protected set
    var isDefault: Boolean = isDefault
        protected set
    val isNotDefault get() = !isDefault

    // Methods

    /**
     * Read method for property delegation.
     */
    operator fun getValue(
        thisRef: Any?,
        prop: KProperty<*>,
    ): T {
        return value
    }

    /**
     * Merges the [other] property into this property.
     *
     * @param other property to be merged with lower prioritization
     * @return new property, whereas this property and not-default-properties are prioritized
     */
    infix fun leftMerge(other: Property<T>): Property<T> =
        when {
            this.isDefault && other.isNotDefault -> other
            else -> this
        }
}

/**
 * Mutable property usable as property delegate.
 *
 * @param value actual value of the property
 * @param isDefault true, if value constitutes a default value and is set to false, when [value] is overwritten
 */
class SettableProperty<T>(value: T, isDefault: Boolean = false) : Property<T>(value, isDefault) {
    // Secondary Constructors
    constructor(property: Property<T>) : this(property.value, property.isDefault)

    // Methods

    /**
     * Set method for property delegation.
     */
    operator fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: T,
    ) {
        this.value = value
        this.isDefault = false
    }
}
