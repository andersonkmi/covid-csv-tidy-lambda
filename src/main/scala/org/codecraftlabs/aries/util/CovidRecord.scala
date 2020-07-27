package org.codecraftlabs.aries.util

import java.util.Date

case class CovidRecord(country: String,
                       stateProvince: String,
                       lastUpdate: Date,
                       confirmed: String,
                       deaths: String,
                       recovered: String)
