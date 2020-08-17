package org.codecraftlabs.aries.util

import java.text.SimpleDateFormat
import java.util.Date

object DateTimeFormatters {
  val YYYY_MM_DD_HH_MM_SS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val YYYY_MM_DD_T_HH_MM_SS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
  val MM_DD_YYYY_HHMM = new SimpleDateFormat("M/dd/yy HH:mm")
  private val YYYY_MM_DD = new SimpleDateFormat("yyyy/MM/dd")

  def generateDateTimeInPartitionFormat(ts: Date): String = {
    val firstFormat = YYYY_MM_DD.format(ts)
    val tokens = firstFormat.split("/")
    val buffer = new StringBuilder()
    buffer.append("year=").append(tokens(0)).append("/month=").append(tokens(1)).append("/day=").append(tokens(2))
    buffer.toString()
  }
}
