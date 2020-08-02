package org.codecraftlabs.aries.util

import java.text.SimpleDateFormat

object DateTimeFormatters {
  val YYYY_MM_DD_HH_MM_SS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val YYYY_MM_DD_T_HH_MM_SS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
  val MM_DD_YYYY_HHMM = new SimpleDateFormat("M/dd/yyyy HH:mm")
  val YYYY_MM_DD = new SimpleDateFormat("yyyy/MM/dd")
}
