"""Pixel assertions for screenshots from actual game clients. Requires Pillow."""
from PIL import Image

def verify(path, kind):
    image = Image.open(path).convert('RGB')
    width, height = image.size
    def black(x, y):
        return max(image.getpixel((x, y))) <= 3
    left = next((x for x in range(width // 2) if not black(x, height // 2)), width // 2)
    top = next((y for y in range(height // 2) if not black(width // 2, y)), height // 2)
    expected_left = max(0, (width - height * (4 / 3 if kind == 'aspect-4-3' else 1)) / 2) if kind in ('aspect-4-3', 'aspect-1-1') else 0
    expected_top = max(0, (height - width / 2.39) / 2) if kind == 'zoomed' else 0
    if kind in ('linear-midpoint', 'smooth-midpoint'):
        good = left == 0 and 0 < top < max(0, (height - width / 2.39) / 2)
    elif kind in ('zoomed', 'aspect-4-3', 'aspect-1-1', 'regular-500x', 'exit'):
        good = abs(left - expected_left) <= 8 and abs(top - expected_top) <= 8
    else:
        return
    if not good:
        raise AssertionError(f'{kind}: visible bar insets ({left}, {top}) do not match expected ({expected_left}, {expected_top})')
