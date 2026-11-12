#!/usr/bin/env python3
"""Pixel assertions for screenshots from actual game clients. Requires Pillow.

Usage: verify_captures.py [--sheet out.png] <capture.png>...
Captures are named <anything>-<kind>.png or <kind>.png. --sheet also writes a contact sheet to look at.
"""
import argparse
from pathlib import Path
import sys

from PIL import Image, ImageDraw

KINDS = ['normal', 'zoomed', 'aspect-4-3', 'aspect-1-1', 'regular-500x', 'exit', 'settings', 'linear-midpoint', 'smooth-midpoint', 'spyglass', 'replay']

def verify(path, kind):
    image = Image.open(path).convert('RGB')
    width, height = image.size
    # At 500x one block fills the view, so a flat color is correct there. It still can't be black.
    colors = len(image.resize((64, 36)).getcolors(64 * 36))
    blank = max(image.getpixel((width // 2, height // 2))) <= 3 if kind == 'regular-500x' else colors < 8
    if blank:
        raise AssertionError(f'{kind}: capture is blank')
    def black(x, y):
        return max(image.getpixel((x, y))) <= 3
    left = next((x for x in range(width // 2) if not black(x, height // 2)), width // 2)
    top = next((y for y in range(height // 2) if not black(width // 2, y)), height // 2)
    expected_left = max(0, (width - height * (4 / 3 if kind == 'aspect-4-3' else 1)) / 2) if kind in ('aspect-4-3', 'aspect-1-1') else 0
    # The spyglass and replay steps zoom with the cinematic profile, so they have the same bars.
    expected_top = max(0, (height - width / 2.39) / 2) if kind in ('zoomed', 'spyglass', 'replay') else 0
    if kind in ('linear-midpoint', 'smooth-midpoint'):
        good = left == 0 and 0 < top < max(0, (height - width / 2.39) / 2)
    elif kind in ('zoomed', 'spyglass', 'replay', 'aspect-4-3', 'aspect-1-1', 'regular-500x', 'exit'):
        good = abs(left - expected_left) <= 8 and abs(top - expected_top) <= 8
    else:
        return
    if not good:
        raise AssertionError(f'{kind}: visible bar insets ({left}, {top}) do not match expected ({expected_left}, {expected_top})')


def kind_of(path):
    name = Path(path).stem
    return next((kind for kind in KINDS if name == kind or name.endswith('-' + kind)), None)


def write_sheet(paths, destination, cell=(480, 270), columns=len(KINDS)):
    rows = (len(paths) + columns - 1) // columns
    sheet = Image.new('RGB', (cell[0] * columns, (cell[1] + 18) * rows), '#202020')
    draw = ImageDraw.Draw(sheet)
    for index, path in enumerate(paths):
        x, y = cell[0] * (index % columns), (cell[1] + 18) * (index // columns)
        draw.text((x + 4, y + 3), Path(path).parent.name + '/' + Path(path).stem, fill='white')
        sheet.paste(Image.open(path).convert('RGB').resize(cell), (x, y + 18))
    sheet.save(destination)


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument('--sheet')
    parser.add_argument('captures', nargs='+')
    args = parser.parse_args()
    failed = 0
    for path in args.captures:
        kind = kind_of(path)
        try:
            if kind is None:
                raise AssertionError('unknown capture kind')
            verify(path, kind)
        except AssertionError as error:
            failed += 1
            print(f'{path}: {error}')
    if args.sheet:
        write_sheet(args.captures, args.sheet)
    print(f'{len(args.captures) - failed} of {len(args.captures)} captures pass')
    return 1 if failed else 0


if __name__ == '__main__':
    sys.exit(main())
