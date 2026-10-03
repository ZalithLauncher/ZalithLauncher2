#!/usr/bin/env python3
"""Verify the bundled native renderer libraries are packaged for the requested Android ABI(s).

Mirai ships two wrapper libraries that are built from source rather than vendored as
prebuilt blobs, and both must survive packaging:

  * ``libltw.so``       - LTW,     the OpenGL 3.2 core wrapper (MC 1.17+)
  * ``libltwlegacy.so`` - LTW Legacy, the OpenGL 1.x/2.1 wrapper (MC 1.8 - 1.16.5)

A missing or wrong-architecture library is invisible until someone launches the matching
Minecraft version on a device, so it is checked here instead.
"""

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
# Libraries built from source in this repository. A prebuilt library that is only
# committed to jniLibs (libgl4es_114.so and friends) is not listed here.
BUILT_FROM_SOURCE_LIBRARIES = ("libltw.so", "libltwlegacy.so")


def expected_abis(arch: str) -> tuple[str, ...]:
    if arch == "all":
        return ALL_ABIS
    if arch in ABI_FOR_ARCH:
        return (ABI_FOR_ARCH[arch],)
    raise ValueError(f"Unsupported build architecture: {arch}")


def verify_library(apk_name: str, archive: ZipFile, entry: str, abi: str) -> None:
    if entry not in archive.namelist():
        raise ValueError(f"{apk_name} is missing {entry}")

    library = archive.read(entry)
    if len(library) < 20 or library[:4] != b"\x7fELF":
        raise ValueError(f"{apk_name}: {entry} is not a valid ELF library")

    elf_class, data_encoding = library[4], library[5]
    if data_encoding != 1:
        raise ValueError(f"{apk_name}: {entry} is not little-endian ELF")
    if int.from_bytes(library[16:18], "little") != 3:
        raise ValueError(f"{apk_name}: {entry} is not an ELF shared object")

    machine = int.from_bytes(library[18:20], "little")
    if (elf_class, machine) != ELF_ABI[abi]:
        raise ValueError(
            f"{apk_name}: {entry} has ELF class {elf_class}, machine {machine}; "
            f"expected class {ELF_ABI[abi][0]}, machine {ELF_ABI[abi][1]}"
        )


def verify(arch: str, apk_path: Path, libraries: tuple[str, ...]) -> None:
    abis = expected_abis(arch)

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
                missing = [
                    f"{abi}/{library}"
                    for library in libraries
                    for abi in abis
                    if f"lib/{abi}/{library}" not in names
                ]
                if missing:
                    raise ValueError(
                        f"{apk.name} is missing native libraries for: {', '.join(missing)}"
                    )

                for library in libraries:
                    for abi in abis:
                        verify_library(apk.name, archive, f"lib/{abi}/{library}", abi)
        except (OSError, BadZipFile) as error:
            raise ValueError(f"Could not read APK {apk}: {error}") from error

        print(f"Verified {', '.join(libraries)} for {', '.join(abis)} in {apk.name}")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("arch", choices=("all", *ABI_FOR_ARCH.keys()))
    parser.add_argument("apk_path", type=Path, help="an APK file or a directory of APK files")
    parser.add_argument(
        "--library",
        dest="libraries",
        action="append",
        metavar="NAME",
        help=(
            "restrict the check to a specific packaged library name; "
            "repeatable. Defaults to checking every library Mirai builds from source: "
            + ", ".join(BUILT_FROM_SOURCE_LIBRARIES)
        ),
    )
    args = parser.parse_args()

    libraries = tuple(args.libraries) if args.libraries else BUILT_FROM_SOURCE_LIBRARIES

    try:
        verify(args.arch, args.apk_path, libraries)
    except ValueError as error:
        print(f"Native library APK verification error: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
