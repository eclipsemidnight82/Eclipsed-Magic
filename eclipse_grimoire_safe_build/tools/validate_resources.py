#!/usr/bin/env python3
"""No dependencies: static JSON, TOML, registry/asset consistency check. Not a Minecraft test."""
from pathlib import Path
import json, struct, tomllib, sys
root=Path(__file__).resolve().parent.parent
errors=[]
for path in root.rglob('*.json'):
    try: json.loads(path.read_text(encoding='utf-8'))
    except Exception as exc: errors.append(f'{path.relative_to(root)}: {exc}')
try: tomllib.loads((root/'src/main/resources/META-INF/mods.toml').read_text(encoding='utf-8'))
except Exception as exc: errors.append(f'mods.toml: {exc}')
for path in root.rglob('*.png'):
    if path.read_bytes()[:8] != b'\x89PNG\r\n\x1a\n': errors.append(f'{path.name}: invalid PNG signature')
for name in ('salt','grimoire'):
    if not (root/f'src/main/resources/assets/eclipse_grimoire/models/item/{name}.json').exists():
        errors.append(f'{name}: missing item model')
if not (root/'src/main/resources/assets/eclipse_grimoire/blockstates/salt_line.json').exists():
    errors.append('missing salt blockstate')
print('Static resource validation:', 'PASSED' if not errors else 'FAILED')
print('Java source files:', len(list(root.rglob('*.java'))))
print('Resource JSON files:', len(list(root.rglob('*.json'))))
for error in errors: print('ERROR:',error)
sys.exit(1 if errors else 0)
