#!/usr/bin/env python3
"""Verify the LTW native library is packaged for the requested Android ABI(s)."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path
from zipfile import BadZipFile, ZipFile

ABI_FOR_ARCH = {
    "arm": "armeabi-v7a",
    "arm64": "arm64-v8a",
    "x86": "x86",
    "x86_64": "x86_64",
}
ALL_ABIS = tuple(ABI_FOR_ARCH.values())
# Expected ELF class (EI_CLASS) and machine (e_machine) for each Android ABI.
# The verifier also requires little-endian ET_DYN shared objects.
ELF_ABI = {
    "armeabi-v7a": (1, 40),  # ELFCLASS32, EM_ARM
    "arm64-v8a": (2, 183),  # ELFCLASS64, EM_AARCH64
    "x86": (1, 3),  # ELFCLASS32, EM_386
    "x86_64": (2, 62),  # ELFCLASS64, EM_X86_64
}


def verify(arch: str, apk_path: Path) -> None:
    if arch == "all":
        expected_abis = ALL_ABIS
    elif arch in ABI_FOR_ARCH:
        expected_abis = (ABI_FOR_ARCH[arch],)
    else:
        raise ValueError(f"Unsupported build architecture: {arch}")

    if apk_path.is_file():
        apks = [apk_path]
    else:
        apks = sorted(apk_path.glob("*.apk"))
    if not apks:
        raise ValueError(f"No APKs found in {apk_path}")

    for apk in apks:
        try:
            with ZipFile(apk) as archive:
                names = set(archive.namelist())
                missing = [abi for abi in expected_abis if f"lib/{abi}/libltw.so" not in names]
                if missing:
                    raise ValueError(
                        f"{apk.name} is missing LTW native libraries for: {', '.join(missing)}"
                    )

                for abi in expected_abis:
                    entry = f"lib/{abi}/libltw.so"
                    library = archive.read(entry)
                    if len(library) < 20 or library[:4] != b"\x7fELF":
                        raise ValueError(f"{apk.name}: {entry} is not a valid ELF library")

                    elf_class, data_encoding = library[4], library[5]
                    if data_encoding != 1:
                        raise ValueError(f"{apk.name}: {entry} is not little-endian ELF")
                    if int.from_bytes(library[16:18], "little") != 3:
                        raise ValueError(f"{apk.name}: {entry} is not an ELF shared object")

                    machine = int.from_bytes(library[18:20], "little")
                    if (elf_class, machine) != ELF_ABI[abi]:
                        raise ValueError(
                            f"{apk.name}: {entry} has ELF class {elf_class}, machine {machine}; "
                            f"expected class {ELF_ABI[abi][0]}, machine {ELF_ABI[abi][1]}"
                        )
        except (OSError, BadZipFile) as error:
            raise ValueError(f"Could not read APK {apk}: {error}") from error

        print(f"Verified LTW ELF libraries for {', '.join(expected_abis)} in {apk.name}")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("arch", choices=("all", *ABI_FOR_ARCH.keys()))
    parser.add_argument("apk_path", type=Path, help="an APK file or a directory of APK files")
    args = parser.parse_args()

    try:
        verify(args.arch, args.apk_path)
    except ValueError as error:
        print(f"LTW APK verification error: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
