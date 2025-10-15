# Git hooks

The code base utilises various git hooks that exist within the [Git hooks directory]. This helps
to ensure that code quality meets expectations before permanently adding code into the repository.

## Installation

### Via Cocogitto

As the repository uses [Cocogitto] for [Semantic versioning] via [Conventional commits], there's
a command that helps to install the relevant git hooks for developers:

```shell
cog install-hook --all
```

### Via git configuration

Git configuration can override the `core.hooksPath` property to point to a non-default directory:

```shell
git config core.hooksPath $(git rev-parse --show-toplevel)/.config/githooks
```

[Cocogitto]: https://docs.cocogitto.io/
[Conventional commits]: https://www.conventionalcommits.org/en/v1.0.0/
[Git hooks directory]: /.config/githooks
[Semantic versioning]: https://semver.org/