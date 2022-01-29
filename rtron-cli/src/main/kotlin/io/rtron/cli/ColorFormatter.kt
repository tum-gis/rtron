package io.rtron.cli

import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.output.HelpFormatter
import com.github.ajalt.mordant.TermColors

/**
 * Color formatter for the command line interface.
 */
class ColorFormatter : CliktHelpFormatter() {
    private val tc = TermColors(TermColors.Level.ANSI16)

    override fun renderTag(tag: String, value: String) = tc.green(super.renderTag(tag, value))
    override fun renderOptionName(name: String) = tc.green(super.renderOptionName(name))
    override fun renderArgumentName(name: String) = tc.green(super.renderArgumentName(name))
    override fun renderSubcommandName(name: String) = tc.green(super.renderSubcommandName(name))
    override fun renderSectionTitle(title: String) = (tc.bold + tc.underline)(super.renderSectionTitle(title))
    override fun optionMetavar(option: HelpFormatter.ParameterHelp.Option) = tc.green(super.optionMetavar(option))
}
