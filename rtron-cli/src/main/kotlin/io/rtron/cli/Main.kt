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

@file:JvmName("Main")

package io.rtron.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption

class MainCommand : CliktCommand(name = "rtron") {
    override fun run() = Unit
}

/**
 * Entry point for command line interface.
 *
 * @param args arguments of the cli
 */
fun main(args: Array<String>) =
    MainCommand()
        .versionOption("1.3.1")
        .subcommands(SubcommandValidateOpendrive(), SubcommandOpendriveToCitygml())
        .main(args)
