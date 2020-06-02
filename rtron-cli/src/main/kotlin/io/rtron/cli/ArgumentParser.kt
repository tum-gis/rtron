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

package io.rtron.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.path
import com.github.ajalt.mordant.TermColors
import org.apache.commons.lang3.SystemUtils
import io.rtron.io.files.toPath
import io.rtron.main.batch.BatchConfiguration

/**
 * Color formatter for the command line interface.
 */
class ColorHelpFormatter : CliktHelpFormatter() {
    private val tc = TermColors(TermColors.Level.ANSI16)

    override fun renderTag(tag: String, value: String) = tc.green(super.renderTag(tag, value))
    override fun renderOptionName(name: String) = tc.green(super.renderOptionName(name))
    override fun renderArgumentName(name: String) = tc.green(super.renderArgumentName(name))
    override fun renderSubcommandName(name: String) = tc.green(super.renderSubcommandName(name))
    override fun renderSectionTitle(title: String) = (tc.bold + tc.underline)(super.renderSectionTitle(title))
    override fun optionMetavar(option: HelpFormatter.ParameterHelp.Option) = tc.green(super.optionMetavar(option))
}


/**
 * Parser of the command line interface arguments.
 */
class ArgumentParser : CliktCommand(
        name = "rtron".toLowerCase(),
        help = """r:trån transforms road networks described in OpenDRIVE into the virtual 3D city model standard CityGML.""".trimMargin(),
        printHelpOnEmptyArgs = true
) {

    // Properties and Initializers
    init {
        if(!SystemUtils.IS_OS_WINDOWS)
            context { helpFormatter = ColorHelpFormatter() }

        versionOption("1.1.0")
    }

    private val inputPath by argument(
            help = "Path to the directory containing OpenDRIVE datasets").path(exists = true)
    private val outputPath by argument(
            help = "Path to the output directory into which the transformed CityGML models are written").path()
    private val noRecursive by option("--no-recursive",
            help = "Do not search recursively for input files in given input directory").flag(default = false)
    private val parallel by option(help = "Run processing in parallel")
            .flag(default = false)
    private val clean by option("--clean", "-c",
            help = "Clean output directory by deleting its current content before starting").flag(default = false)


    // Methods
    override fun run() {}

    /**
     * Parses arguments to the [BatchConfiguration].
     */
    fun parseConfiguration() = BatchConfiguration(inputPath.toPath(), outputPath.toPath(),
            !noRecursive, parallelProcessing = parallel, clean = clean)
}
