"""Writes public/assets/view/data.json: where the game's files the zoom view shows sit in Mojang's servers.

Nothing from the game is copied. The page reads the HUD sprites out of Minecraft's client jar with one range
request each (this file records each one's offset, size and compression), and the title screen panorama from
Mojang's asset server, looked up by name in the version's asset index.

Run with the version's client jar to hand: python3 tools/view-data.py [path/to/client.jar]
"""
import hashlib, json, os, struct, sys, urllib.request
from pathlib import Path

VERSION = '26.3'
HERE = Path(__file__).resolve().parent.parent
OUT = HERE / 'public/assets/view/data.json'
JAR = Path(sys.argv[1]) if len(sys.argv) > 1 else Path.home() / f'Library/Application Support/PrismLauncher/libraries/com/mojang/minecraft/{VERSION}/minecraft-{VERSION}-client.jar'
SPRITES = ['hud/hotbar', 'hud/hotbar_selection', 'hud/heart/container', 'hud/heart/full', 'hud/food_empty', 'hud/food_full',
           'hud/armor_full', 'hud/experience_bar_background', 'hud/experience_bar_progress', 'hud/crosshair']
ITEMS = ['diamond_sword', 'iron_pickaxe', 'bow', 'spyglass', 'apple', 'bread', 'map', 'compass_00']

def version():
    manifest = json.load(urllib.request.urlopen('https://piston-meta.mojang.com/mc/game/version_manifest_v2.json'))
    url = next(v['url'] for v in manifest['versions'] if v['id'] == VERSION)
    return json.load(urllib.request.urlopen(url))

def central_directory(data):
    end = data.rfind(b'PK\x05\x06')
    count, size, offset = struct.unpack_from('<HII', data, end + 10)
    entries, i = {}, offset
    for _ in range(count):
        method, csize, nlen, xlen, clen, local = (struct.unpack_from('<H', data, i + 10)[0], struct.unpack_from('<I', data, i + 20)[0],
            *struct.unpack_from('<HHH', data, i + 28), struct.unpack_from('<I', data, i + 42)[0])
        entries[data[i + 46:i + 46 + nlen].decode()] = [local, csize, method]
        i += 46 + nlen + xlen + clen
    return entries

v = version()
client = v['downloads']['client']
data = JAR.read_bytes()
if hashlib.sha1(data).hexdigest() != client['sha1']:
    sys.exit(f"{JAR} isn't the {VERSION} client jar Mojang serves")
cd = central_directory(data)
paths = [f'assets/minecraft/textures/gui/sprites/{s}.png' for s in SPRITES] + [f'assets/minecraft/textures/item/{i}.png' for i in ITEMS]
missing = [p for p in paths if p not in cd]
if missing: sys.exit(f'not in the jar: {missing}')
OUT.parent.mkdir(parents=True, exist_ok=True)
OUT.write_text(json.dumps({
    'version': VERSION,
    'jar': client['url'],
    'index': v['assetIndex']['url'],
    'panorama': [f'minecraft/textures/gui/title/background/panorama_{i}.png' for i in range(6)],
    'entries': {p: cd[p] for p in paths},
}, indent=1) + '\n')
print(f'{OUT.relative_to(HERE)}: {len(paths)} files in the jar, 6 panorama faces')
