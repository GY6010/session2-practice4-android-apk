# Interview Prep — Android APK built entirely by CI

An Android flashcard app for the UT Group (UTC / UTT) master's registration
interview, built and signed **without any local Android toolchain**.

The whole build environment — JDK, Android SDK, Gradle — lives in a YAML file
and runs on a clean GitHub-hosted runner. Nothing was installed on the author's
machine to produce the APK.

---

## What the app does

The app is a self-test deck for a technical interview conducted in English:

- **Countdown** to the interview date, so the remaining preparation time is the
  first thing on screen.
- **Flashcards** — the question is shown and the answer stays hidden until you
  reveal it, so the deck works as a test rather than a document to read.
- **Swipe or tap** to move through the deck; positions marked as mastered are
  remembered across launches.
- **Progress bar** tracking how many of the cards have been mastered.

The deck is written in `Deck.kt`. Editing the questions means editing one Kotlin
list — no database, no schema, no migration. That is a deliberate choice: the
content is small and static, so a data file that is version-controlled and
reviewable in a pull request is a better fit than any storage layer.

## Why this app, and why it matters

Two things were being solved at once.

**The pipeline problem.** Configuring Android locally is the classic first wall:
SDK levels, JDK versions, Gradle, signing keys, and the mismatch between one
machine and another. None of that is part of the actual engineering task, yet it
consumes the first day of every project. Declaring the environment in YAML
removes the wall entirely — the build is reproducible because the runner starts
clean every time, and the APK is a downloadable artifact rather than something
that exists only on one laptop.

**The preparation problem.** Interview preparation is usually a document, and a
document does not test anything — reading an answer produces a feeling of
familiarity without producing recall. Under pressure, the answer has to be
retrievable *without* the text in front of you. Hiding the answer until it is
requested turns preparation material into an actual test, and tracking which
cards are mastered turns an unbounded task into a finite one with a visible
finish line.

The two problems meet in the same artifact: the app is useful, and it is also
the proof that a real, signed, installable application can be produced by a
pipeline alone. The application content is independent of the pipeline; the
pipeline only needs a repository, a Gradle wrapper, and a keystore secret.

---

## Build pipeline

Two workflows, deliberately separate.

### `android-debug.yml` — every push

| Step | Purpose |
|---|---|
| `actions/checkout@v4` | The runner has no code without this; it must come first. |
| `actions/setup-java@v4` | Provisions Temurin JDK 17 — the runner does not ship the version the project targets. |
| `gradle/actions/setup-gradle@v4` | Gradle caching. `cache-read-only` on non-`main` refs stops pull requests from writing a poisoned cache entry. |
| `./gradlew assembleDebug` | Builds the APK and acts as the quality gate. |
| `actions/upload-artifact@v4` | Makes `app-debug.apk` downloadable from the run page. |

The Android SDK is preinstalled on `ubuntu-latest` and `ANDROID_HOME` is already
set, so no manual SDK installation step is needed.

### `android-release.yml` — on a `v*` tag

Adds four things on top of the debug job:

1. **Decode the keystore** from the `KEYSTORE_BASE64` secret into
   `$RUNNER_TEMP`, which is wiped when the job ends.
2. **Build** with the signing config, which reads its four values from
   environment variables — no keystore path or password is ever in the source.
3. **Verify the signature** with `apksigner verify --print-certs` before
   publishing, so an unsigned build can never reach a release.
4. **Attach the APK** to a GitHub Release.

Release builds run on tags only, never on every push.

---

## Repository secrets

| Secret | Value |
|---|---|
| `KEYSTORE_BASE64` | Base64 of the `.jks` file, single line, no wrapping |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

`bootstrap-keystore.yml` generates all four via a manual run: it creates a
keystore with `keytool` inside the runner, prints the generated password to the
job summary, and uploads the base64 as a one-day artifact. Run it once, copy the
values into Secrets, then delete the artifact and the workflow file. This is how
the keystore was created without a local JDK.

---

## Producing a release

```bash
git tag v1.0
git push origin v1.0
```

The tag triggers `android-release.yml`; the signed APK appears both as a
workflow artifact and as a release download.

---

## Project layout

```
app/build.gradle.kts                  signing config reads environment variables
app/src/main/java/com/gy/interview/
  Deck.kt                             the question deck
  MainActivity.kt                     countdown, cards, swipe, mastery tracking
app/src/main/res/layout/activity_main.xml
.github/workflows/
  android-debug.yml                   push → debug APK artifact
  android-release.yml                 tag → verified signed APK + release
  bootstrap-keystore.yml              one-off keystore generation
```

---

## Notes and limitations

- `minSdk` is 24, so the APK installs on Android 7.0 and later.
- An APK cannot be installed on iOS: `.apk` and `.ipa` are unrelated formats with
  different signing models. The artifact from this pipeline targets Android.
- The release build is not minified (`isMinifyEnabled = false`). Enabling R8
  would shrink the APK, but would also require keep rules for anything reached
  by reflection — not worth it for an app this size.
