# Developer dependencies outside of Android Studio

* The [Brew file] contains dependencies obtained via [Homebrew]. Developers use this to quickly
  set up their development environment with the proceeding command:

  ```shell
  brew bundle --file=$(git rev-parse --show-toplevel)/Brewfile
  ```

# Gotchas / considerations

* If a Developer has multiple Java Development Kit versions installed, the `org.gradle.java.home`
  property needs to point to a valid `JAVA_HOME` path. This property should exist within the User's
  `~/.gradle/gradle.properties` file.
* Be mindful that Homebrew by default is a 'Rolling release' package manager. This means that by
  default, it downloads the latest version of a dependency.

[Brew file]: /Brewfile
[Homebrew]: https://brew.sh