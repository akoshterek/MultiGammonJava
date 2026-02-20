package org.akoshterek.backgammon.util

import java.io.FileWriter
import java.nio.file.{Files, Path, Paths, StandardCopyOption}
import scala.util.Using
import org.json4s._
import org.json4s.native.Serialization.writePretty

/**
 * Utility for atomic JSON file operations
 */
object JsonUtils {
  
  /**
   * Atomically write JSON to file with pretty formatting.
   * Uses temp file + atomic rename to prevent corruption.
   */
  def saveJsonPretty[T <: AnyRef](data: T, path: Path)(implicit formats: Formats): Unit = {
    val tempFile = Paths.get(path.toString + ".tmp")
    
    try {
      Files.createDirectories(path.getParent)
      
      // Write to temp file with pretty formatting
      val json = writePretty(data)
      Using(new FileWriter(tempFile.toFile)) { writer =>
        writer.write(json)
      }.get
      
      // Atomic rename
      Files.move(tempFile, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
    } catch {
      case e: Exception =>
        // Clean up temp file on error
        Files.deleteIfExists(tempFile)
        throw new RuntimeException(s"Failed to save JSON to $path", e)
    }
  }
}
