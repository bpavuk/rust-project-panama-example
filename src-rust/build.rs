fn main() {
    let crate_dir = std::env::var("CARGO_MANIFEST_DIR").unwrap();
    let header_path = "bindings/jvm_interop.h";
    cbindgen::Builder::new()
        .with_crate(crate_dir.clone())
        .with_language(cbindgen::Language::C)
        .generate()
        .expect("Unable to generate bindings")
        .write_to_file(header_path);
    strip_unneeded_includes(header_path);
}

fn strip_unneeded_includes(header_path: &str) {
    // HACK: jextract generates incorrect code unless stdlib and stdarg are excluded
    let contents = std::fs::read_to_string(header_path).expect("Unable to read generated header");
    let filtered = contents
        .lines()
        .filter(|line| {
            *line != "#include <stdlib.h>" && *line != "#include <stdarg.h>"
        })
        .collect::<Vec<_>>()
        .join("\n");
    std::fs::write(header_path, filtered).expect("Unable to write filtered header");
}
