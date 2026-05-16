#!/usr/bin/env zsh

set -euo pipefail

usage() {
  cat <<'USAGE'
Usage:
  scripts/release.sh <version> [options]

Example:
  scripts/release.sh 1.0.1

Options:
  --no-push       Build, test, commit, and tag locally, but do not push.
  --no-upload     Build the signed Central zip, but do not upload to Central.
  --skip-swift    Skip Swift Package tests.
  --force-tag     Move an existing local/remote tag to the release commit.
  -h, --help      Show this help.

The script expects SIGNING_KEY, SIGNING_PASSWORD, and Central Portal credentials
to be available after loading ~/.zshrc with `zsh -ic`.
USAGE
}

fail() {
  print -u2 "release: $1"
  exit 1
}

step() {
  print "\n==> $1"
}

version=""
push_release=1
upload_release=1
run_swift=1
force_tag=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --no-push)
      push_release=0
      ;;
    --no-upload)
      upload_release=0
      ;;
    --skip-swift)
      run_swift=0
      ;;
    --force-tag)
      force_tag=1
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    -*)
      usage
      fail "unknown option: $1"
      ;;
    *)
      [[ -z "$version" ]] || fail "only one version argument is supported"
      version="$1"
      ;;
  esac
  shift
done

[[ -n "$version" ]] || {
  usage
  fail "missing version"
}

[[ "$version" =~ '^[0-9]+[.][0-9]+[.][0-9]+([-.][0-9A-Za-z]+)*$' ]] ||
  fail "version must look like 1.0.0 or 1.0.0-beta.1"

script_dir="${0:A:h}"
repo_root="${script_dir:h}"
cd "$repo_root"

[[ -f README.md ]] || fail "README.md not found from $repo_root"
[[ -x ./gradlew ]] || fail "Gradle wrapper not found or not executable"

branch="$(git branch --show-current)"
[[ -n "$branch" ]] || fail "not on a branch"

if [[ -n "$(git status --porcelain)" ]]; then
  git status --short
  fail "working tree must be clean before release"
fi

if git rev-parse -q --verify "refs/tags/$version" >/dev/null; then
  (( force_tag == 1 )) || fail "tag $version already exists; pass --force-tag to move it"
fi

step "Updating README install snippets to $version"
RELEASE_VERSION="$version" perl -0pi -e '
  s/one[.]adverse:perimeter-progress:[^"]+/one.adverse:perimeter-progress:$ENV{RELEASE_VERSION}/g;
  s/(perimeter-progress[.]git", from: ")[^"]+/$1$ENV{RELEASE_VERSION}/g;
' README.md

grep -Fq "one.adverse:perimeter-progress:$version" README.md ||
  fail "Android README dependency was not updated"
grep -Fq "from: \"$version\"" README.md ||
  fail "Swift README dependency was not updated"

step "Checking signing environment from ~/.zshrc"
zsh -ic '[[ -n "${SIGNING_KEY:-}" ]] || { print -u2 "SIGNING_KEY is not set"; exit 1; }; [[ -n "${SIGNING_PASSWORD:-}" ]] || { print -u2 "SIGNING_PASSWORD is not set"; exit 1; }'

if (( upload_release == 1 )); then
  zsh -ic '[[ -n "${CENTRAL_PORTAL_USERNAME:-}" || -n "${CENTRAL_USERNAME:-}" ]] || { print -u2 "Central Portal username is not set"; exit 1; }; [[ -n "${CENTRAL_PORTAL_PASSWORD:-}" || -n "${CENTRAL_PASSWORD:-}" ]] || { print -u2 "Central Portal password is not set"; exit 1; }'
fi

step "Running Android unit tests"
./gradlew :android:testDebugUnitTest -PVERSION_NAME="$version"

step "Assembling Android release AAR"
./gradlew :android:assembleRelease -PVERSION_NAME="$version"

if (( run_swift == 1 )); then
  step "Running Swift Package tests"
  swift test
fi

step "Building signed Maven Central zip"
zsh -ic "./gradlew nmcpZipAggregation -PVERSION_NAME=$version"

if ! git diff --quiet -- README.md; then
  step "Committing README release version"
  git add README.md
  git commit -m "Release $version"
else
  step "README already referenced $version"
fi

step "Tagging $version"
if (( force_tag == 1 )); then
  git tag -f "$version"
else
  git tag "$version"
fi

if (( push_release == 1 )); then
  step "Pushing $branch and tag $version"
  git push origin "$branch"
  if (( force_tag == 1 )); then
    git push --force origin "$version"
  else
    git push origin "$version"
  fi
else
  step "Skipping git push"
fi

if (( upload_release == 1 )); then
  step "Uploading to Sonatype Central Portal"
  zsh -ic "./gradlew nmcpPublishAggregationToCentralPortal -PVERSION_NAME=$version"
  print "\nCentral upload is USER_MANAGED. Publish the validated deployment from the Central Portal UI."
else
  step "Skipping Central upload"
fi

print "\nRelease $version complete."
