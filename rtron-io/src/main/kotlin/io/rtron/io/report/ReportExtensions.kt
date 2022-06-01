package io.rtron.io.report

fun List<Message>.mergeToReport(): Report = Report(this)
fun List<Report>.merge(): Report = Report(flatMap { it.getMessages() })
