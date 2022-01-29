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

@file:JvmName("Main")

package io.rtron.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption
import io.rtron.cli.opendrive2citygml2.SubcommandOpendriveToCitygml2

class MainCommand : CliktCommand(name = "rtron") {
    override fun run() = Unit
}

class Drop : CliktCommand(help = "Drop the database") {
    override fun run() {
        echo("Dropped the database.")
    }
}

/**
 * Entry point for command line interface.
 *
 * @param args arguments of the cli
 */
fun main(args: Array<String>) = MainCommand()
    .versionOption("1.2.2")
    .subcommands(SubcommandOpendriveToCitygml2(), Drop())
    .main(args)
