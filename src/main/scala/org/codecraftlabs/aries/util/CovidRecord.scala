package org.codecraftlabs.aries.util

case class CovidRecord(country: String,
                       stateProvince: String,
                       lastUpdate: String,
                       confirmed: Long,
                       deaths: Long,
                       recovered: Long)
