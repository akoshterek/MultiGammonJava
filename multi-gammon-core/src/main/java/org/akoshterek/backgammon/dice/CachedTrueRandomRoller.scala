package org.akoshterek.backgammon.dice

import com.typesafe.scalalogging.LazyLogging
import java.io.{BufferedReader, FileReader}
import java.net.{HttpURLConnection, URL}
import java.nio.file.{Files, Path, StandardCopyOption}
import java.util.StringTokenizer
import scala.util.{Failure, Success, Try}

class CachedTrueRandomRoller(stateDir: Path, initialSeed: Int) extends DiceRoller with LazyLogging {
  private val CACHE_DIR = "random_dice_cache"
  private val BASE_URL = "https://www.bgblitz.com/download/random/random_dice_"
  private val MIN_FILE_SIZE = 3 * 1000 * 1000 // 3MB (almost)
  private val MAX_SEED = 8
  private val MIN_SEED = 1

  private var currentSeed = initialSeed.max(MIN_SEED).min(MAX_SEED)
  private var currentReader: Option[BufferedReader] = None
  private var tokenizer: Option[StringTokenizer] = None

  private val cacheDirectory = stateDir.resolve(CACHE_DIR)

  // Create cache directory if it doesn't exist
  if (!Files.exists(cacheDirectory)) {
    logger.info(s"Creating cache directory: $cacheDirectory")
    Files.createDirectories(cacheDirectory)
  }

  private def ensureFileExists(seed: Int): Path = {
    val fileName = s"random_dice_$seed.txt"
    val filePath = cacheDirectory.resolve(fileName)

    if (!Files.exists(filePath) || Files.size(filePath) < MIN_FILE_SIZE) {
      logger.info(s"Downloading file: $fileName")
      downloadFile(s"$BASE_URL$seed.txt", filePath)
    }

    filePath
  }

  private def downloadFile(url: String, destination: Path): Unit = {
    try {
      val connection = new URL(url).openConnection().asInstanceOf[HttpURLConnection]
      connection.setRequestMethod("GET")

      if (connection.getResponseCode == HttpURLConnection.HTTP_OK) {
        Files.copy(connection.getInputStream, destination, StandardCopyOption.REPLACE_EXISTING)
        logger.info(s"Successfully downloaded $url to $destination")
      } else {
        logger.error(s"Failed to download $url, response code: ${connection.getResponseCode}")
        throw new RuntimeException(s"Download failed with status code ${connection.getResponseCode}")
      }
    } catch {
      case e: Exception =>
        logger.error(s"Error downloading file: ${e.getMessage}", e)
        throw e
    }
  }

  private def openNextFile(): Unit = {
    try {
      // Close the current reader if it exists
      currentReader.foreach(_.close())

      val filePath = ensureFileExists(currentSeed)
      logger.debug(s"Opening file: $filePath")

      currentReader = Some(new BufferedReader(new FileReader(filePath.toFile)))
      readNextLine()

      // Prepare for the next file
      currentSeed = if (currentSeed >= MAX_SEED) MIN_SEED else currentSeed + 1
    } catch {
      case e: Exception =>
        logger.error(s"Error opening file: ${e.getMessage}", e)
        throw e
    }
  }

  private def readNextLine(): Unit = {
    currentReader.foreach { reader =>
      val line = reader.readLine()
      if (line == null) {
        // End of file, open the next one
        openNextFile()
      } else {
        tokenizer = Some(new StringTokenizer(line))
        if (!tokenizer.exists(_.hasMoreTokens)) {
          // If the line is empty, read the next one
          readNextLine()
        }
      }
    }
  }

  private def ensureReaderIsOpen(): Unit = {
    if (currentReader.isEmpty) {
      openNextFile()
    }
  }

  override def roll(): (Int, Int) = {
    ensureReaderIsOpen()

    // Get next token
    if (tokenizer.exists(!_.hasMoreTokens)) {
      readNextLine()
    }

    tokenizer match {
      case Some(t) if t.hasMoreTokens =>
        val token = t.nextToken()
        Try {
          if (token.length == 2) {
            val die1 = token.charAt(0).toString.toInt
            val die2 = token.charAt(1).toString.toInt
            (die1, die2)
          } else {
            throw new IllegalStateException(s"Invalid dice token: $token")
          }
        } match {
          case Success(dice) => dice
          case Failure(e) =>
            logger.error(s"Error parsing dice: ${e.getMessage}", e)
            roll() // Try again with the next token
        }
      case _ =>
        logger.warn("No tokens available, opening next file")
        openNextFile()
        roll() // Recursive call to try again
    }
  }
}

object CachedTrueRandomRoller extends LazyLogging {
  def apply(stateDir: Path, seed: Int = 1): CachedTrueRandomRoller = {
    new CachedTrueRandomRoller(stateDir, seed)
  }
}
