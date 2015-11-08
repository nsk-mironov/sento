package io.sento.compiler

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.util.zip.ZipFile

public interface Opener {
  public fun open(): ByteArray
}

public class FileOpener(private val file: File) : Opener {
  override fun open(): ByteArray {
    return FileUtils.readFileToByteArray(file)
  }
}

public class JarOpener(private val file: File, private val entry: String) : Opener {
  override fun open(): ByteArray {
    return ZipFile(file).use {
      it.getInputStream(it.getEntry(entry)).use {
        IOUtils.toByteArray(it)
      }
    }
  }
}
