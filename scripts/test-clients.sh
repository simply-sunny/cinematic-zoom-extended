#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ALL_TARGETS=(1.20.1 1.21.1 1.21.2 1.21.5 1.21.7 1.21.11 26.1 26.2 26.3)
# Each jar also covers these versions. Run one as <jar target>@<Minecraft version>. They need CLEAN=1
# because a world from a newer version won't open in an older one.
COMPAT_TARGETS=(1.21.2@1.21.3 1.21.2@1.21.4 1.21.5@1.21.6 1.21.7@1.21.8 1.21.11@1.21.9 1.21.11@1.21.10 26.1@26.1.1 26.1@26.1.2)
TARGETS=("$@")
if [ "${#TARGETS[@]}" -eq 0 ]; then
	TARGETS=("${ALL_TARGETS[@]}")
elif [ "$1" = "compat" ]; then
	TARGETS=("${COMPAT_TARGETS[@]}")
	CLEAN=1
fi

OUT="$ROOT/build/testclient${QUILT:+/quilt}"
LOGS="$OUT/logs"
SHOTS="$OUT/screenshots"
TIMEOUT_SECONDS="${TIMEOUT:-300}"
CLEAN="${CLEAN:-0}"
OPEN_RESULTS="${OPEN_RESULTS:-1}"
mkdir -p "$LOGS" "$SHOTS"

if command -v timeout >/dev/null 2>&1; then
	TIMEOUT_BIN="$(command -v timeout)"
elif command -v gtimeout >/dev/null 2>&1; then
	TIMEOUT_BIN="$(command -v gtimeout)"
else
	echo "timeout is required (install coreutils or use gtimeout)" >&2
	exit 1
fi

java_for() {
	case "$1" in
		1.20.1|1.21.*) echo "${JAVA21:-/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home}" ;;
		26.*) echo "${JAVA25:-/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home}" ;;
		*) echo "${JAVA_HOME:-}" ;;
	esac
}

ensure_option() {
	local file="$1" key="$2" value="$3"
	touch "$file"
	if grep -q "^$key:" "$file"; then
		sed -i '' "s/^$key:.*/$key:$value/" "$file"
	else
		printf '%s:%s\n' "$key" "$value" >>"$file"
	fi
}

run_target() {
	local label="$1"
	local target="${label%@*}"
	local compat=""
	if [ "$label" != "$target" ]; then compat="${label#*@}"; fi
	local java_home
	java_home="$(java_for "$target")"
	if [ ! -x "$java_home/bin/java" ]; then
		echo "[$target] missing Java at $java_home" >&2
		return 1
	fi

	local run="$ROOT/versions/$target/run"
	local log="$LOGS/$label.log"
	mkdir -p "$run"
	if [ "$CLEAN" = "1" ]; then
		rm -rf "$run/saves"
	fi
	rm -f "$run/config/cinematiczoom.json"
	rm -rf "$run/screenshots" "$run/autotest"
	mkdir -p "$run/autotest"
	ensure_option "$run/options.txt" onboardAccessibility false
	ensure_option "$run/options.txt" tutorialStep none
	ensure_option "$run/options.txt" pauseOnLostFocus false
	# Leave the no-bars key unset so the run uses and saves the mod's default.
	sed -i.bak '/^key_key.cinematiczoom.zoom_no_bars:/d' "$run/options.txt" && rm -f "$run/options.txt.bak"
	ensure_option "$run/options.txt" key_key.cinematiczoom.previous_aspect key.keyboard.left
	ensure_option "$run/options.txt" key_key.cinematiczoom.next_aspect key.keyboard.right
	: >"$log"

	local -a gradle=(./gradlew ":$target:runClient" -PtestClient --console=plain)
	if [ -n "$compat" ]; then gradle+=("-PcompatProject=$target" "-PcompatVersion=$compat"); fi
	if [ "${QUILT:-0}" = "1" ]; then gradle+=(-PquiltTest); fi

	if [ ! -d "$run/saves/Demo_World" ]; then
		echo "[$target] generating demo world"
		if ! (cd "$ROOT" && JAVA_HOME="$java_home" "$TIMEOUT_BIN" -k 10 "$TIMEOUT_SECONDS" \
			"${gradle[@]}" -Pbootstrap --args="--demo --width 1280 --height 720") >>"$log" 2>&1; then
			echo "[$target] bootstrap failed (see $log)" >&2
			return 1
		fi
		if ! grep -q '^ok$' "$run/autotest/status.txt" 2>/dev/null; then
			echo "[$target] bootstrap did not report ok (see $log)" >&2
			return 1
		fi
	fi

	echo "[$target] launching client"
	if ! (cd "$ROOT" && JAVA_HOME="$java_home" "$TIMEOUT_BIN" -k 10 "$TIMEOUT_SECONDS" \
		"${gradle[@]}" --args="--quickPlaySingleplayer Demo_World --width 1280 --height 720") >>"$log" 2>&1; then
		echo "[$target] client run failed (see $log)" >&2
		return 1
	fi
	if ! grep -q '^ok$' "$run/autotest/status.txt" 2>/dev/null; then
		echo "[$target] automation did not report ok (see $log)" >&2
		tail -n 20 "$log" >&2
		return 1
	fi

	local kind
	for kind in normal zoomed aspect-4-3 aspect-1-1 regular-500x exit settings linear-midpoint smooth-midpoint; do
		local source="$run/screenshots/$target-$kind.png"
		if [ ! -f "$source" ]; then
			echo "[$target] missing screenshot $source" >&2
			return 1
		fi
		cp "$source" "$SHOTS/$label-$kind.png"
	done
	if ! python3 "$ROOT/scripts/verify_captures.py" "$SHOTS/$label"-[a-z]*.png >>"$log" 2>&1; then
		echo "[$target] pixel check failed:" >&2
		tail -n 5 "$log" >&2
		return 1
	fi
	echo "[$target] OK"
}

write_gallery() {
	local file="$OUT/index.html"
	{
		cat <<'HTML'
<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Cinematic Zoom Extended - client screenshots</title>
<style>
:root { color-scheme: dark; }
body { margin: 0; background: #0d1117; color: #e6edf3; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif; }
main { max-width: 1400px; margin: 0 auto; padding: 32px 24px 80px; }
h1 { margin-top: 0; }
section { margin: 0 0 48px; }
h2 { border-bottom: 1px solid #30363d; padding-bottom: 8px; }
.grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(420px, 1fr)); gap: 16px; }
figure { margin: 0; background: #161b22; border: 1px solid #30363d; border-radius: 8px; overflow: hidden; }
figure img { display: block; width: 100%; height: auto; }
figcaption { padding: 8px 12px; color: #9198a1; font-size: 14px; }
.fail { color: #f85149; }
.ok { color: #3fb950; }
</style>
</head>
<body>
<main>
<h1>Cinematic Zoom Extended - live client screenshots</h1>
<p>Every capture from the test entrypoint. Each one passed the pixel check.</p>
HTML
		local target
		for target in ${SUCCEEDED[@]+"${SUCCEEDED[@]}"}; do
			echo "<section><h2>$target</h2><div class=\"grid\">"
			local kind
			for kind in normal zoomed aspect-4-3 aspect-1-1 regular-500x exit settings linear-midpoint smooth-midpoint; do
				echo "<figure><img src=\"screenshots/$target-$kind.png\" alt=\"$target $kind\"><figcaption>$kind</figcaption></figure>"
			done
			echo "</div></section>"
		done
		if [ "${#FAILED[@]}" -gt 0 ]; then
			echo "<section><h2>Failed</h2><p class=\"fail\">${FAILED[*]}</p></section>"
		fi
		cat <<'HTML'
</main>
</body>
</html>
HTML
	} >"$file"
	echo "Gallery: $file"
	if [ "$OPEN_RESULTS" = "1" ] && command -v open >/dev/null 2>&1; then
		open "$file"
	fi
}

SUCCEEDED=()
FAILED=()
for target in "${TARGETS[@]}"; do
	if run_target "$target"; then
		SUCCEEDED+=("$target")
	else
		FAILED+=("$target")
	fi
done

write_gallery
echo
if [ "${#SUCCEEDED[@]}" -gt 0 ]; then
	echo "Passed: ${SUCCEEDED[*]}"
	echo "Screenshots: $SHOTS"
fi
if [ "${#FAILED[@]}" -gt 0 ]; then
	echo "Failed: ${FAILED[*]}"
	exit 1
fi
