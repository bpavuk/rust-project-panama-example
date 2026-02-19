package org.example.interop

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout.JAVA_LONG
import java.nio.file.Path

object RustApi {
    // Most of the time, you do not need to interact with these low-level APIs,
    //  as jextract generates bindings for you.
    private val addHandle by lazy {
        val libPath = System.getProperty("rust.library.path")
            ?: error("Missing -Drust.library.path JVM property")

        val arena = Arena.ofAuto()
        val symbols = SymbolLookup.libraryLookup(Path.of(libPath), arena)
        val symbol = symbols.find("add")
            .orElseThrow { IllegalStateException("Symbol 'add' not found in $libPath") }

        Linker.nativeLinker().downcallHandle(
            symbol,
            FunctionDescriptor.of(JAVA_LONG, JAVA_LONG, JAVA_LONG)
        )
    }

    fun add(left: Long, right: Long): Long =
        addHandle.invoke(left, right) as Long
}
