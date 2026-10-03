#!/usr/bin/env python3
"""Turn Gradle unit test failures into GitHub workflow annotations.

Without this, a failing unit test reports only "Process completed with exit code 1" and the
actual assertion is buried in the job log, which is not visible from the checks UI or from any
API. Emitting ``::error`` annotations puts the failing test, its message and its stack trace
directly on the check run.

Handles both ways a Gradle test task fails:

  * JUnit XML reports exist  -> one annotation per failing test case.
  * No reports at all        -> the test source set failed to compile, so the tail of the
                               Gradle log is published instead.
"""

from __future__ import annotations

import os
import pathlib
import sys
import xml.etree.ElementTree as ElementTree

MAX_ANNOTATIONS = 15
MAX_MESSAGE_CHARS = 1200


def escape(message: str) -> str:
    """Escape a string for use as a GitHub workflow command message."""
    return message.replace("%", "%25").replace("\r", "").replace("\n", "%0A")


def annotate(message: str, title: str) -> None:
    print(f"::error title={escape(title)}::{escape(message)}")


def from_reports(report_dir: pathlib.Path) -> int:
    emitted = 0
    for xml_file in sorted(report_dir.glob("TEST-*.xml")):
        try:
            root = ElementTree.parse(xml_file).getroot()
        except ElementTree.ParseError:
            continue

        for case in root.iter("testcase"):
            problems = list(case.findall("failure")) + list(case.findall("error"))
            if not problems:
                continue

            for problem in problems:
                body = "\n".join(
                    part
                    for part in (
                        (problem.get("message") or "").strip(),
                        (problem.text or "").strip(),
                    )
                    if part
                )
                annotate(
                    f"{case.get('classname')}.{case.get('name')}\n\n"
                    f"{body[:MAX_MESSAGE_CHARS]}",
                    title=f"Unit test failed: {case.get('name')}",
                )
                emitted += 1
                if emitted >= MAX_ANNOTATIONS:
                    return emitted
    return emitted


def from_gradle_log(log_path: pathlib.Path) -> None:
    """Publish the interesting part of a Gradle log, for compile failures with no reports."""
    if not log_path.is_file():
        print(
            "::error title=Unit tests failed::No test reports and no Gradle log were "
            "produced; the test task did not run."
        )
        return

    lines = log_path.read_text(errors="replace").splitlines()
    patterns = (
        "e: ",
        "error:",
        "fatal error:",
        "FAILED",
        "Execution failed for task",
        "> Task :",
        "What went wrong:",
        "Could not ",
        "UnknownHostException",
        "OutOfMemoryError",
        "Test .*FAILED",
    )
    interesting = [
        line[:400]
        for line in lines
        if any(pattern in line for pattern in patterns)
    ]
    if not interesting:
        interesting = [line[:400] for line in lines[-40:]]

    for line in interesting[-MAX_ANNOTATIONS:]:
        annotate(line, title="Unit test build output")


def main() -> int:
    report_dir = pathlib.Path(os.environ.get("MIRAI_TEST_REPORT_DIR", ""))
    log_path = pathlib.Path(os.environ.get("MIRAI_TEST_LOG", ""))

    if report_dir.is_dir() and any(report_dir.glob("TEST-*.xml")):
        emitted = from_reports(report_dir)
        if emitted:
            print(f"Published {emitted} failing unit test(s) as annotations.")
            return 0
        print(
            "::error title=Unit tests failed::Test reports exist but contain no failures; "
            "the task probably failed for an infrastructure reason. See the job log."
        )
        return 0

    from_gradle_log(log_path)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
