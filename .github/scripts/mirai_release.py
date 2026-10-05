#!/usr/bin/env python3
"""Validate Aerix release APKs and build the launcher update manifest."""

from __future__ import annotations

import argparse
import json
import os
import sys
from pathlib import Path
from urllib.parse import quote

EXPECTED_ARCHES = {"all", "arm", "arm64", "x86", "x86_64"}
ARCH_SUFFIXES = {
    "-armeabi-v7a.apk": "arm",
    "-arm64-v8a.apk": "arm64",
    "-x86.apk": "x86",
    "-x86_64.apk": "x86_64",
}


def classify_apk(name: str) -> str:
    if " " in name:
        raise ValueError(f"APK name contains a space; GitHub renames it on upload: {name}")
    if not name.startswith(("MiraiLauncher-", "Aerix.Launcher-")):
        raise ValueError(f"Unexpected APK name: {name}")
    for suffix, arch in ARCH_SUFFIXES.items():
        if name.endswith(suffix):
            return arch
    if name.endswith(".apk"):
        return "all"
    raise ValueError(f"Could not identify APK architecture from filename: {name}")


def collect_apks(directory: Path) -> list[tuple[Path, str]]:
    apks = sorted(directory.glob("*.apk"))
    if len(apks) != len(EXPECTED_ARCHES):
        raise ValueError(f"Expected five APKs; found {len(apks)}: {[apk.name for apk in apks]}")

    for apk in apks:
        if not apk.is_file() or apk.stat().st_size <= 0:
            raise ValueError(f"APK is missing or empty: {apk}")

    classified = [(apk, classify_apk(apk.name)) for apk in apks]
    architectures = [arch for _, arch in classified]
    if len(set(architectures)) != len(EXPECTED_ARCHES) or set(architectures) != EXPECTED_ARCHES:
        raise ValueError(
            f"Expected exactly one APK for {sorted(EXPECTED_ARCHES)}; found {sorted(architectures)}"
        )
    return classified


def verify(directory: Path) -> None:
    for apk, arch in collect_apks(directory):
        print(f"{arch}: {apk.name} ({apk.stat().st_size} bytes)")


def write_metadata(
    apks_directory: Path,
    output: Path,
    repository: str,
    tag: str,
    published_at: str,
    version_code: int,
    version_name: str,
    body: str,
) -> None:
    if not repository or not tag or not published_at:
        raise ValueError("Repository, release tag, and published timestamp are required")
    if version_code <= 0 or not version_name:
        raise ValueError("A positive version code and a non-empty version name are required")

    base_url = f"https://github.com/{repository}/releases/download/{quote(tag, safe='')}"
    files = []
    for apk, arch in collect_apks(apks_directory):
        files.append(
            {
                "file_name": apk.name,
                "uri": f"{base_url}/{quote(apk.name, safe='')}",
                "arch": arch,
                "size": apk.stat().st_size,
            }
        )

    release_notes = body.strip() or f"Aerix Launcher {version_name}"
    metadata = {
        "code": version_code,
        "version": version_name,
        "created_at": published_at,
        "files": files,
        "default_body": {"language": "en", "markdown": release_notes},
        "bodies": [{"language": "en", "markdown": release_notes}],
    }
    output.write_text(json.dumps(metadata, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Generated update metadata for Aerix Launcher {version_name} ({len(files)} APKs).")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    subparsers = parser.add_subparsers(dest="command", required=True)

    verify_parser = subparsers.add_parser("verify", help="verify the five expected APK architectures")
    verify_parser.add_argument("apks_directory", type=Path)

    metadata_parser = subparsers.add_parser("metadata", help="write a release update manifest")
    metadata_parser.add_argument("--apks-directory", type=Path, required=True)
    metadata_parser.add_argument("--output", type=Path, required=True)
    metadata_parser.add_argument("--repository", required=True)
    metadata_parser.add_argument("--tag", required=True)
    metadata_parser.add_argument("--published-at", required=True)
    metadata_parser.add_argument("--version-code", type=int, required=True)
    metadata_parser.add_argument("--version-name", required=True)
    metadata_parser.add_argument("--body", default=os.environ.get("RELEASE_BODY", ""))

    args = parser.parse_args()
    try:
        if args.command == "verify":
            verify(args.apks_directory)
        else:
            write_metadata(
                apks_directory=args.apks_directory,
                output=args.output,
                repository=args.repository,
                tag=args.tag,
                published_at=args.published_at,
                version_code=args.version_code,
                version_name=args.version_name,
                body=args.body,
            )
    except (OSError, ValueError) as error:
        print(f"release validation error: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
