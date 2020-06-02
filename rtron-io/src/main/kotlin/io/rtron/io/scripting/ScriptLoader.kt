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

package io.rtron.io.scripting

import de.swirtz.ktsrunner.objectloader.KtsObjectLoader
import io.rtron.io.files.BufferedReader
import io.rtron.io.files.Path


/**
 * Loads Kotlin scripts and executes them.
 */
object ScriptLoader {

    // Methods

    /**
     * Loads and executes the Kotlin [script].
     */
    inline fun <reified T> load(script: String): T = createKtsObjectLoader().load(script)

    /**
     * Reads and loads the Kotlin script located at [path].
     */
    inline fun <reified T> load(path: Path): T {
        val scriptReader = BufferedReader(path)
        return createKtsObjectLoader().load(scriptReader.toBufferedReaderJ())
    }

    fun createKtsObjectLoader(): KtsObjectLoader {
        setIdeaIoUseFallback()
        return KtsObjectLoader()
    }

    /**
     * This fixes the warning: "WARN: Failed to initialize native filesystem for Windows"
     * See this [Kotlin discussion](https://discuss.kotlinlang.org/t/kotlin-script-engine-error/5654) for more.
     */
    private fun setIdeaIoUseFallback() {
        val properties = System.getProperties()!!
        properties.setProperty("idea.io.use.nio2", java.lang.Boolean.TRUE.toString())
    }
}
