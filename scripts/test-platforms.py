#!/usr/bin/env python3
"""Run real Forge/NeoForge clients and require the in-game acceptance report.
Requires JDK 17, 21, 25 (JAVA_INSTALLATIONS may list paths for Gradle detection).
Demo worlds and options are isolated under platform/*/runs or platform/*/run.
"""
import argparse
import json
import os
from pathlib import Path
import shutil
import subprocess
from verify_captures import verify

ROOT = Path(__file__).resolve().parents[1]
VERSIONS = ['1.20.1', '1.21.1', '1.21.2', '1.21.5', '1.21.7', '1.21.11', '26.1', '26.2', '26.3']
FEATURES = ['normal', 'zoomed', 'aspect-4-3', 'aspect-1-1', 'regular-500x', 'exit', 'settings', 'linear-midpoint', 'smooth-midpoint']
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--loader', required=True, choices=['forge', 'neoforge'])
parser.add_argument('versions', nargs='*')
args = parser.parse_args()
out = ROOT / 'build/testclient' / args.loader
out.mkdir(parents=True, exist_ok=True)
results = []
for version in args.versions or VERSIONS:
    if version not in VERSIONS:
        parser.error('Unknown target: ' + version)
    if args.loader == 'forge' and version == '1.21.2':
        results.append(dict(version=version, status='unavailable', reason='Forge published no 1.21.2 loader'))
        continue
    legacy = version == '1.20.1'
    project = ROOT / 'platform' / ('forge-1.20.1' if legacy else args.loader)
    run = project / (('run-neoforge' if args.loader == 'neoforge' else 'run') if legacy else 'runs/' + version)
    run.mkdir(parents=True, exist_ok=True)
    fixture = ROOT / 'versions' / version / 'run/saves/Demo_World'
    world = run / 'saves/Demo_World'
    if not world.exists() and fixture.exists():
        shutil.copytree(fixture, world)
    # Explicit test controls: old worlds may have the pre-refactor regular binding unbound.
    options = run / 'options.txt'
    lines = options.read_text().splitlines() if options.exists() else []
    # Modern key names require the current options schema version, not the pre-LWJGL3 fixer.
    if not any(line.startswith('version:') for line in lines):
        fixture_options = ROOT / 'versions' / version / 'run/options.txt'
        if fixture_options.exists():
            lines.extend(line for line in fixture_options.read_text().splitlines() if line.startswith('version:'))
    overrides = {'onboardAccessibility':'false', 'tutorialStep':'none', 'pauseOnLostFocus':'false',
                 'key_key.cinematiczoom.zoom':'key.keyboard.c',
                 'key_key.cinematiczoom.previous_aspect':'key.keyboard.left', 'key_key.cinematiczoom.next_aspect':'key.keyboard.right'}
    # The no-bars key stays unset so the run uses and saves the mod's default.
    lines = [line for line in lines if line.split(':',1)[0] not in overrides and not line.startswith('key_key.cinematiczoom.zoom_no_bars:')]
    options.write_text('\n'.join(lines + [key + ':' + value for key,value in overrides.items()]) + '\n')
    (run/'config/cinematiczoom.json').unlink(missing_ok=True)
    status = run/'autotest/status.txt'
    status.unlink(missing_ok=True)
    command = [str(ROOT/'gradlew'), '-p', str(project), 'runClient', '-PtestClient', '-Pmc='+version, '--console=plain']
    if legacy and args.loader == 'neoforge': command.append('-PlegacyNeo')
    if os.getenv('JAVA_INSTALLATIONS'): command.append('-Porg.gradle.java.installations.paths='+os.environ['JAVA_INSTALLATIONS'])
    log = out/(version+'.log')
    print(args.loader, version, 'starting', flush=True)
    good = False
    with log.open('w') as stream:
        try:
            if not world.exists():
                bootstrap = subprocess.run(command+['-Pbootstrap'], stdout=stream, stderr=subprocess.STDOUT, timeout=420)
                if bootstrap.returncode or not status.exists() or not status.read_text().startswith('ok\n'):
                    raise RuntimeError('Demo bootstrap failed')
            status.unlink(missing_ok=True)
            process = subprocess.run(command, stdout=stream, stderr=subprocess.STDOUT, timeout=420)
            good = process.returncode == 0 and status.exists() and status.read_text().startswith('ok\nfeatures=7\n')
        except (subprocess.TimeoutExpired, RuntimeError) as error:
            stream.write('\n'+str(error)+'\n')
    target_name = 'cinematiczoom-forge-1.20.1' if legacy else 'cinematiczoom-'+args.loader+'-'+version
    destination = out/'screenshots'/version
    destination.mkdir(parents=True, exist_ok=True)
    for feature in FEATURES:
        image = run/'screenshots'/(target_name+'-'+feature+'.png')
        if good and image.exists():
            shutil.copy(image, destination/(feature+'.png'))
            try: verify(image, feature)
            except AssertionError as error:
                print(args.loader, version, 'pixel failure:', error, flush=True)
                good = False
        elif good: good = False
    result = dict(version=version, status='pass' if good else 'fail', log=str(log.relative_to(ROOT)))
    results.append(result)
    print(args.loader, version, result['status'], flush=True)
    (out/'results.json').write_text(json.dumps(results,indent=2)+'\n')
raise SystemExit(any(result['status']=='fail' for result in results))
