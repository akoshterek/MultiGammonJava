package org.akoshterek.backgammon

import org.apache.commons.io.IOUtils

import resource.managed

/**
  * @author Alex
  *         date 04.08.2015.
  */
object License {
  def printLicense(): Unit = {
    printResourse("/org/akoshterek/backgammon/COPYING")
  }

  def printWarranty(): Unit = {
    printResourse("/org/akoshterek/backgammon/WARRANTY")
  }

  def printBanner(): Unit = {
    printResourse("/org/akoshterek/backgammon/BANNER")
  }

  private def printResourse(resourceName: String) {
    managed(this.getClass.getResourceAsStream(resourceName)).acquireAndGet(inputStream => {
      IOUtils.copy(inputStream, System.out)
    })
  }
}
