"""Writes public/assets/view/settings.json: the mod's Cloth Config screen, read from its source, for the settings demo.

Run after changing a setting: python3 tools/settings-data.py
"""
import json, re
from pathlib import Path

HERE = Path(__file__).resolve().parent.parent
SRC = HERE.parent / 'src'
SCREEN = (SRC / 'modern/java/mix/cinematiczoom/integrations/ModMenuIntegration.java').read_text()
CONFIG = (SRC / 'common/java/mix/cinematiczoom/ZoomConfig.java').read_text()
LANG = json.loads((SRC / 'main/resources/assets/cinematiczoom/lang/en_us.json').read_text())
OUT = HERE / 'public/assets/view/settings.json'
TEXT = r'Component\.(translatable|literal)\("([^"]+)"\)'

def text(kind, value):
    return LANG[value] if kind == 'translatable' else value

MATH = (SRC / 'common/java/mix/cinematiczoom/ZoomMath.java').read_text()
defaults = {}
for kind, rest in re.findall(r'public (float|int|boolean|String|ZoomCurve|BarAnimationMode|FovMode) (.+?);', CONFIG):
    for name, value in re.findall(r'(\w+) = ([^,]+)', rest):
        defaults[name] = value.strip().strip('"').rstrip('f')

def enum(name):
    body = (SRC / f'common/java/mix/cinematiczoom/{name}.java').read_text()
    shown = re.findall(r'([A-Z_]+)\("([^"]+)"\)', body)
    if shown:
        return dict(shown)
    names = re.search(r'enum \w+ \{\s*([A-Z_, ]+);?', body).group(1)
    return {n.strip(): n.strip() for n in names.split(',') if n.strip()}

def number(value, whole):
    return str(int(float(value))) if whole else str(float(value))

tabs, names = [], {}
for var, kind, value in re.findall(r'ConfigCategory (\w+) = builder\.getOrCreateCategory\(' + TEXT, SCREEN):
    names[var] = {'name': text(kind, value), 'rows': []}
    tabs.append(names[var])

chunks = re.split(r'(?=\b\w+\.addEntry\()', SCREEN)
for chunk in chunks:
    head = re.match(r'(\w+)\.addEntry\(\s*eb\.start(\w+)\(' + TEXT + r',\s*(.+)', chunk)
    if not head:
        continue
    var, start, kind, label, rest = head.groups()
    tip = re.search(r'setTooltip\(' + TEXT, chunk)
    row = {'label': text(kind, label)}
    if tip:
        row['tip'] = text(*tip.groups())
    field = re.search(r'cfg\.(\w+)', rest).group(1)
    value = defaults.get(field, '')
    if value.startswith('ZoomMath.'):
        top, bottom = re.search(value.split('.')[1] + r' = ([\d.]+)f? / ([\d.]+)f?', MATH).groups()
        value = str(float(top) / float(bottom))
    if '1f / cfg.' in rest.split(')')[0] and value:
        value = str(1 / float(value))
    if start == 'BooleanToggle':
        row.update(kind='toggle', value=value == 'true')
    elif start == 'EnumSelector':
        options = enum(re.match(r'(\w+)\.class', rest).group(1))
        row.update(kind='enum', options=list(options.values()), value=options[value.split('.')[-1]])
    elif start == 'StrList':
        row.update(kind='list')
    else:
        row.update(kind='field', value=number(value, start == 'IntField') if start != 'StrField' else value)
    names[var]['rows'].append(row)

OUT.write_text(json.dumps({'title': LANG['cinematiczoom.config.title'], 'tabs': tabs}, indent=1, ensure_ascii=False) + '\n')
print(f'{OUT.relative_to(HERE)}: ' + ', '.join(f"{t['name']} {len(t['rows'])}" for t in tabs))
