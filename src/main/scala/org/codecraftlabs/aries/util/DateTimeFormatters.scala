package org.codecraftlabs.aries.util

import java.text.SimpleDateFormat

object DateTimeFormatters {
  val YYYY_MM_DD_HH_MM_SS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val YYYY_MM_DD_HH_MM_SS_SSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss")
  val YYYY_MM_DD_T_HH_MM_SS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
  val MM_DD_YYYY_HHMM = new SimpleDateFormat("M/dd/yy HH:mm")
  private val YYYY_MM_DD = new SimpleDateFormat("yyyy/MM/dd")

  def generateDateTimeInPartitionFormat(ts: String): String = {
    val convertedDate = YYYY_MM_DD_HH_MM_SS_SSS.parse(ts)
    val firstFormat = YYYY_MM_DD.format(convertedDate)
    val tokens = firstFormat.split("/")
    val buffer = new StringBuilder()
    buffer.append("year=").append(tokens(0)).append("/month=").append(tokens(1)).append("/day=").append(tokens(2))
    buffer.toString()
  }
}
