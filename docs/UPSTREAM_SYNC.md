# Syncing this fork

The source repository is `https://github.com/whuppi/pdf_manipulator`.
Its development branch is `dev`; `prod` holds release promotions. Its
`master` branch still contains the old Android implementation. Fetching and
merging `master` therefore does not bring in current development.

The fork commit `9e25794` copied the Rust/Dart implementation from upstream
`65f4233` without preserving that commit's ancestry. The integration of
`e28b2a6` (`dev`) uses that snapshot to distinguish upstream updates from
fork changes, while preserving both histories in a merge commit.

Resolution decisions:

- Advance the active Dart API, bridge protocol, workers, tests, documentation,
  and engine pins together. Their operation codes and argument contracts
  must agree with the Rust engine.
- Restore the upstream SDK pin, CI workflows, hooks, publication rules, and
  submodule definitions omitted from the copied snapshot.
- Keep upstream's explicit public named constructor arguments. The private
  named parameter shorthand in `85f2e5d` requires Dart 3.12, whereas the
  upstream package declares support for Dart 3.10. The public API names
  remain unchanged.
- Use upstream dependency ranges and lockfiles, including its added fixture
  dependency, rather than the copied snapshot's dependency floors.
- Preserve the complete fork-owned `android/` tree and `features.md` and
  `todo.md`. These are historical Android sources and notes; the current
  package uses the Rust engine, and does not declare a Kotlin plugin in its
  pubspec. Their presence does not establish Rust feature coverage.
- Preserve the local example analyzer exclusions.

For subsequent integrations, fetch and merge the development branch
explicitly (the remote name below assumes the current checkout):

```sh
git fetch origin
git merge origin/dev
git submodule update --init --recursive
make check
```

Check `git remote -v` before using these commands in another clone. A fork's
`origin` may point to the fork, in which case configure the source repository
as `upstream` and use `upstream/dev`. Fetching alone never merges changes.
