package org.codecraftlabs.aries.util

import java.util.Date

case class CovidRecord(country: String,
                       stateProvince: String,
                       lastUpdate: Date,
                       confirmed: Long,
                       deaths: Long,
                       recovered: Long)
