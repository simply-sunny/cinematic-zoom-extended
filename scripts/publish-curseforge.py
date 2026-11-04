#!/usr/bin/env python3
"""Upload the built jars to CurseForge using the release sheets and gradle.properties.

Usage: CURSEFORGE_API_KEY=... python3 scripts/publish-curseforge.py <project-id> [--dry-run]
"""
import json
import os
import re
import sys
import urllib.request
import uuid
from pathlib import Path

API = "https://minecraft.curseforge.com/api"
ROOT = Path(__file__).resolve().parent.parent
PROPERTIES = dict(re.findall(r"^([a-z_]+)=(.+)$", (ROOT / "gradle.properties").read_text(), re.M))
VERSION = PROPERTIES["mod_version"].strip()
ARCHIVE = PROPERTIES["archives_base_name"].strip()

TARGETS = ["1.20.1", "1.21.1", "1.21.2", "1.21.5", "1.21.7", "1.21.11", "26.1", "26.2", "26.3"]
LABELS = {"fabric": "Fabric", "forge": "Forge", "neoforge": "NeoForge"}
# Fabric jars that cover more than one Minecraft version: target -> (jar name, versions).
FABRIC_RANGES = {
    "1.21.2": ("1.21.2-1.21.4", ["1.21.2", "1.21.3", "1.21.4"]),
    "1.21.5": ("1.21.5-1.21.6", ["1.21.5", "1.21.6"]),
    "1.21.7": ("1.21.7-1.21.8", ["1.21.7", "1.21.8"]),
    "1.21.11": ("1.21.9-1.21.11", ["1.21.9", "1.21.10", "1.21.11"]),
    "26.1": ("26.1", ["26.1", "26.1.1", "26.1.2"]),
}
FABRIC_API = {"slug": "fabric-api", "type": "requiredDependency"}
CLOTH_CONFIG = {"slug": "cloth-config", "type": "requiredDependency"}
MOD_MENU = {"slug": "modmenu", "type": "requiredDependency"}


def java_for(minecraft):
    return "Java 17" if minecraft == "1.20.1" else "Java 21" if minecraft.startswith("1.") else "Java 25"


def jar_dir(minecraft, loader):
    if loader == "fabric":
        return ROOT / "build" / "libs"
    if minecraft == "1.20.1":
        return ROOT / "platform" / "forge-1.20.1" / "build" / loader / "libs"
    return ROOT / "platform" / loader / "build" / minecraft / "libs"


def builds():
    """Yield (target, jar name, versions, loader, loader tags, relations). Forge published no 1.21.2 loader."""
    for target in TARGETS:
        name, versions = FABRIC_RANGES.get(target, (target, [target]))
        yield target, name, versions, "fabric", ["Fabric", "Quilt"], [FABRIC_API, CLOTH_CONFIG, MOD_MENU]
        yield target, target, [target], "neoforge", ["NeoForge"], [CLOTH_CONFIG]
        if target != "1.21.2":
            yield target, target, [target], "forge", ["Forge"], [CLOTH_CONFIG] if target == "1.20.1" else []


def request(path, data=None, content_type=None):
    req = urllib.request.Request(API + path, data=data)
    req.add_header("X-Api-Token", os.environ["CURSEFORGE_API_KEY"])
    if content_type:
        req.add_header("Content-Type", content_type)
    with urllib.request.urlopen(req) as resp:
        return json.load(resp)


def version_ids():
    """Map names to IDs, preferring Minecraft version types over the untyped/addon duplicates."""
    types = {t["id"]: t["slug"] for t in request("/game/version-types")}
    ids = {}
    for v in request("/game/versions"):
        slug = types.get(v["gameVersionTypeID"], "")
        if slug.startswith("minecraft-") or slug in ("modloader", "environment", "java"):
            ids.setdefault(v["name"], v["id"])
    return ids


def changelog():
    text = (ROOT / "CHANGELOG.md").read_text()
    match = re.search(rf"^## (?:\[{re.escape(VERSION)}\]|{re.escape(VERSION)}\b)[^\n]*\n(.*?)(?=^## |\Z)", text, re.S | re.M)
    if match is None:
        sys.exit(f"missing changelog section for {VERSION}")
    return match.group(1).strip()


def multipart(fields, file_path):
    boundary = uuid.uuid4().hex
    parts = []
    for name, value in fields.items():
        parts.append(f'--{boundary}\r\nContent-Disposition: form-data; name="{name}"\r\n\r\n{value}\r\n'.encode())
    parts.append(
        f'--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="{file_path.name}"\r\n'
        f"Content-Type: application/java-archive\r\n\r\n".encode()
    )
    parts.append(file_path.read_bytes())
    parts.append(f"\r\n--{boundary}--\r\n".encode())
    return b"".join(parts), f"multipart/form-data; boundary={boundary}"


def main():
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    project_id = sys.argv[1]
    dry_run = "--dry-run" in sys.argv
    ids = version_ids()
    notes = changelog()

    for target, name, versions, loader, loader_tags, relations in builds():
        jar = jar_dir(target, loader) / f"{ARCHIVE}-{name}-{loader}-{VERSION}.jar"
        if not jar.is_file():
            sys.exit(f"missing {jar}")
        names = [*versions, *loader_tags, "Client", java_for(target)]
        missing = [n for n in names if n not in ids]
        if missing:
            sys.exit(f"CurseForge has no version tag for {missing}")
        metadata = {
            "changelog": notes,
            "changelogType": "markdown",
            "displayName": f"Cinematic Zoom Extended {VERSION} ({name.replace('-', '–')}, {LABELS[loader]})",
            "gameVersions": [ids[n] for n in names],
            "releaseType": "release",
        }
        if relations:  # CurseForge rejects an empty projects list.
            metadata["relations"] = {"projects": relations}
        if dry_run:
            print(jar.name, json.dumps({**metadata, "changelog": f"<{len(notes)} chars>"}))
            continue
        body, content_type = multipart({"metadata": json.dumps(metadata)}, jar)
        result = request(f"/projects/{project_id}/upload-file", body, content_type)
        print(f"{jar.name} -> file {result['id']}")


if __name__ == "__main__":
    main()
