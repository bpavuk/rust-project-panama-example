package org.example.app

import org.example.interop.ExternStruct
import org.example.interop.RustApi
import org.example.interop.jvm_interop_h

// if IDEA or Kotlin LSP is scared, run ./gradlew :interop-panama:generateJextractBindings.
// jvm_interop_h is a jextract-generated class.
fun main() {
    println("Hello from Kotlin!")
    println("Heavy Rust calculation: 40 + 2 = ${RustApi.add(40, 2)}")
    println("Testing generated Java code: 40 + 2 = ${jvm_interop_h.add(40, 2)}")

    // operating on Rust/C-defined structs
    val struct = jvm_interop_h.extern_struct_new(60, 9) // this returns MemorySegment!
    val xFromRust = ExternStruct.x(struct) // accessor methods - they read from MemorySegment
    val yFromRust = ExternStruct.y(struct)
    println("Rust owns these. x: $xFromRust, y: $yFromRust. also, xy = nice!")
    jvm_interop_h.extern_struct_free(struct) // remember to clean after yourself!
}
