# panama-exploration

This is an example configuration of Project Panama:

- `rust-src` cdylib crate connected as a library
- Dynamically generated bindings in `interop-panama`
- `app` Kotlin consumer module
- A Nix flake that includes everything you need to reproduce the results everywhere Nix is available

I **strongly advise** you to use what Nix flake provides through `nix develop` or `direnv allow`.
It’ll set up Cargo, glibc, and Azul Zulu Community JDK. That's all you need.

To run, simply execute `./gradlew run`. If you want to learn more about how it works, dive into `src-rust` crate
and `interop-panama` Gradle module, especially buildscript files.