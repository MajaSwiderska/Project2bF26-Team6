# Project Guidelines for AI Agents

## Environment & Build
- **Language**: Scala 3 (3.8.x+)
- **Build Tool**: sbt (1.13.x+)
- **Test Framework**: JUnit 5 (Jupiter) via `jupiter-interface`
- **Packaging**: `sbt-native-packager` (`JavaAppPackaging`)
- **Code Coverage**: `sbt-scoverage`
- **Primary Commands**:
  - Compile: `sbt compile`
  - Test: `sbt test`
  - Format/Check: `sbt scalafmtCheckAll` (reformat with `sbt scalafmtAll`)
  - Coverage: `sbt clean coverage test coverageReport`
  - Stage App: `sbt stage`
  - Run App: `sbt run` (or `sbt "run <maze-name>"`, e.g. `sbt "run labyrinth.txt"`)

---

## 1. Architectural Philosophy: Purely Imperative & Procedural
This project is strictly on the **imperative/procedural** side of the curriculum:
- **No Domain Classes, Traits, or ADTs**: Do not define custom classes, case classes, traits, or domain-model ADTs.
- **Built-in Types Only**: Use `Array`, `String`, `Int`, `Boolean`, tuples (`(Int, Int)`), and `Option`.
- **State & Procedures**: State is explicitly stored in mutable arrays (`Array[Array[Char]]`, `Array[Int]`) and scalar variables; procedures take representations as parameters and mutate state in place or return updated values.
- **Recursion**: Used strictly where procedurally purposeful (e.g. the flood-fill emergency scanner `revealReachable`).

---

## 2. Syntax & Indentation (Significant Indent)
Always use Scala 3 significant indentation. Avoid curly braces `{}` for structural blocks.

- Use a colon `:` to introduce class, trait, object, enum, method, and control block
- Use `then` for `if` expressions and `do` for loops.
- Use 2 spaces per indentation level.
- Keep `end` markers optional and only for long blocks (> 25-30 lines) where context is lost (e.g., `end LongClass`).

---

## 3. Compiler Options & Strict Typing
The project compiles with:
`-language:strictEquality`, `-Yexplicit-nulls`, `-deprecation`, `-unchecked`, `-feature`, `-Werror`

### Multiverse Equality (`-language:strictEquality`)
- Values of type `T` and `U` cannot be compared with `==` or `!=` unless a given instance of `CanEqual[T, U]` is in scope.
- If data types are ever introduced, they MUST include `derives CanEqual`.
- Standard primitives (`Int`, `Char`, `Boolean`, `String`) and `Option` have standard `CanEqual` instances.
- For nullable references (e.g. `String | Null`), pattern match or narrow with `.nn` rather than comparing directly with `== null`.

### Explicit Nulls (`-Yexplicit-nulls`)
- Reference types (`String`, `Array`, etc.) are non-nullable by default.
- Nullable types from Java interop (such as `StdIn.readLine()`) are typed as union types: `T | Null`.
- Avoid returning `null`. Always use `Option[T]` for missing values in pure Scala code.
- Interacting with Java / nullable APIs:
  - Pattern match (`case line: String if line.nonEmpty => ...`)
  - Narrow with guards or unwrap with `.nn` extension method when non-null safety is guaranteed.

```scala
// Interacting with Java / nullable APIs:
scala.io.StdIn.readLine() match
  case line: String if line.nonEmpty =>
    val cmd = line.trim.nn.toUpperCase.nn.head
    // process non-null command
  case _ =>
    // handle empty or null input
```

---

## 4. Externalized Mazes & Resource Organization
- Maze definitions are stored as plain-text ASCII grids under `src/main/resources/mazes/`.
- Available sample mazes include:
  - `default.txt` (11x7 standard starter maze)
  - `tiny.txt` (7x5)
  - `corridor.txt` (15x5, non-square)
  - `chambers.txt` (13x7, non-square)
  - `arena.txt` (16x5, non-square)
  - `labyrinth.txt` (17x8, non-square)
- Procedures `loadMazeFromResource(name)` and `loadMazeFromFile(path)` load grids into `Array[String]`.
- Grids must use standard ASCII markers:
  - `R`: Robot start position (replaced with `.` upon initialization)
  - `E`: Escape exit
  - `C`: Energy cell (+3 energy bonus when collected)
  - `#`: Impassable wall
  - `.`: Walkable open floor

---

## 5. Interactive CLI & REPL
The application provides an interactive `@main def main(mazeName: String = "default.txt")` entry point with the following commands:
- `U` (up) / `D` (down) / `L` (left) / `R` (right): Movement commands (cost 1 energy per valid move).
- `S` (scan): Emergency scanner using recursive flood fill (`revealReachable`).
- `H` (help): Displays available commands and sample mazes via `printHelp()`.
- `Q` (quit): Exits the interactive session.

---

## 6. Testing with JUnit (Jupiter)
- Use JUnit Jupiter annotations (`org.junit.jupiter.api.*`).
- Avoid curly braces in test classes and test methods; use indentation.
- Write descriptive test method names with `@DisplayName`.
- Prefer standard Jupiter assertions (`assertEquals`, `assertTrue`, `assertFalse`, `assertArrayEquals`).
- Maintain thorough test suites for procedural methods:
  - State transitions, movement, collection, and exit detection.
  - Parameter passing, aliasing, and call-by-name (`repeatUntilStopped`).
  - Recursive and iterative flood fill with base cases (wall, out of bounds, already revealed).
  - External maze loading and parsing across all sample mazes.

```scala
package mazesolver

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName

class MazeSolverTest:

  @Test @DisplayName("isWalkable returns true for open floor, false for walls")
  def testIsWalkable(): Unit =
    val maze = parseMaze(Array("###", "#.#", "###"))
    assertTrue(isWalkable(maze, 1, 1))
    assertFalse(isWalkable(maze, 0, 0))

  @Test @DisplayName("moveRobot moves robot and mutates position array")
  def testMoveRobot(): Unit =
    val maze = parseMaze(Array("###", "#..", "###"))
    val robot = Array(1, 1)
    assertTrue(moveRobot(maze, robot, 'R'))
    assertArrayEquals(Array(1, 2), robot)
```

---

## 7. Continuous Integration & Code Coverage
- **GitHub Actions**: Every pull request and push to main/master MUST pass the GitHub Actions CI pipeline ([`.github/workflows/ci.yml`](file:///.github/workflows/ci.yml)).
- **Setup Actions**: Must use `actions/setup-java@v4` (JDK 21 Temurin) and `sbt/setup-sbt@v1`.
- **Coverage Tool**: `sbt-scoverage` plugin must be enabled in `project/plugins.sbt`.
- **Coverage Standard**: High statement and branch coverage must be maintained. Run `sbt clean coverage test coverageReport` to verify coverage locally before pushing changes.
