use std::ffi::c_int;

#[unsafe(no_mangle)] // FFI is always unsafe.
pub extern "C" fn add(left: u64, right: u64) -> u64 {
    left + right
}

#[repr(C)]
pub struct ExternStruct {
    x: c_int, // exposed structs should use C types from std::ffi
    y: c_int,
}

// your code should provide a constructor method...
#[unsafe(no_mangle)]
pub extern "C" fn extern_struct_new(x: c_int, y: c_int) -> *mut ExternStruct {
    let s = Box::new(ExternStruct { x, y });
    Box::into_raw(s)
}

// ...as well as a destructor.
#[unsafe(no_mangle)]
pub extern "C" fn extern_struct_free(s: *mut ExternStruct) {
    if !s.is_null() {
        unsafe { drop(Box::from_raw(s)) }; // KABOOM!
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn it_works() {
        let result = add(2, 2);
        assert_eq!(result, 4);
    }
}
