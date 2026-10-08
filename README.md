# COMP 371 Project 1: Maze Rescue (Imperative Paradigm)

Loyola University Chicago — Department of Computer Science  
**Course:** COMP 371 / COMP 471: Programming Languages (F26)
**Instructor:** Konstantin Läufer  
**Grade:** Points (max 6.0)

---

## Story & Overview

A rescue robot has boarded a damaged space station. The interior structure is unstable, and rooms are unlit or collapsed. The robot must navigate through the corridors, collect crucial energy cells to recharge its failing battery, use an emergency flood-fill sonar scanner to map out connected chambers, and reach the escape pod before its power cell is completely drained!

```
###########
#R..#....E#
#.#.#.##..#
#.#...C#..#
#.#####.#.#
#...C...#.#
###########
```

### Map Legend
* `R`: Robot starting position (mutated to `.` once the game initializes)
* `@`: Robot's current live location on the rendered display
* `E`: Escape pod exit
* `C`: Energy cell (+3 energy bonus when collected)
* `#`: Impassable wall
* `.`: Walkable open corridor / floor

---

## Learning Objectives

In this project, you will:
1. **Transition from Java to Scala 3**: Practice writing idiomatic Scala 3 syntax using significant indentation without unnecessary curly braces `{}`.
2. **Master Imperative & Procedural Programming**: Model dynamic state using built-in Scala mutable data structures (`Array[Array[Char]]`, `Array[Int]`, scalar `var`s, tuples, and `Option`) without domain-model classes, traits, or ADTs.
3. **Analyze Parameter Passing & Aliasing**: Deepen your understanding of call-by-value evaluation, reference copying, aliasing, and why parameter variables cannot be rebound to alter the caller's reference.
4. **Contrast Evaluation Strategies**: Explore Scala's call-by-name evaluation (`=> Boolean`) and see how it differs from call-by-value.
5. **Implement Recursion & Trace Activation Records**: Implement a multi-way recursive flood fill (`revealReachable`), reason about base cases and termination guarantees, and draw call stack frames.
6. **Apply Test-Driven Development (TDD)**: Work with JUnit 5 (Jupiter), interpret compiler contracts under strict typing (`-language:strictEquality`, `-Yexplicit-nulls`, `-Werror`), and write targeted unit tests for boundary conditions.
7. **Reflect on Architectural Paradigms**: Experience firsthand why a purely procedural approach with loosely associated arrays and scalar variables becomes unwieldy as software scales—setting the stage for Object-Oriented design and Algebraic Data Types in Project 2.

---

## Project Structure

```
.
├── build.sbt                               # Build definitions and compiler options
├── src
│   ├── main
│   │   ├── resources
│   │   │   └── mazes                       # External ASCII maze definition files
│   │   │       ├── default.txt             # Standard 11×7 maze
│   │   │       ├── tiny.txt                # Minimal 7×5 maze
│   │   │       ├── corridor.txt            # Long corridor 15×5 maze
│   │   │       ├── chambers.txt            # Multi-chamber 13×7 maze
│   │   │       ├── arena.txt               # Open arena 16×5 maze
│   │   │       └── labyrinth.txt           # Complex 17×8 maze
│   │   └── scala
│   │       └── mazesolver
│   │           └── MazeSolver.scala        # Procedures & interactive CLI entry point
│   └── test
│       └── scala
│           └── mazesolver
│               └── MazeSolverTest.scala    # JUnit 5 Jupiter test suite
└── README.md                               # Assignment specification & written report
```

---

## Functional Requirements

The assignment is staged in four sequential parts:

### Part 1: State & Procedural Abstraction

In `src/main/scala/mazesolver/MazeSolver.scala`, implement the core imperative procedures:

1. **`isWalkable(maze: Array[Array[Char]], row: Int, col: Int): Boolean`**
   * Return `true` if `(row, col)` is within the grid boundaries and the character at that position is not a wall (`'#'`).
2. **`directionDelta(direction: Char): Option[(Int, Int)]`**
   * Map movement commands (`'U'`, `'D'`, `'L'`, `'R'`) to coordinate offset deltas:
     * `'U'` $\rightarrow$ `(-1, 0)`
     * `'D'` $\rightarrow$ `(1, 0)`
     * `'L'` $\rightarrow$ `(0, -1)`
     * `'R'` $\rightarrow$ `(0, 1)`
     * Any other character $\rightarrow$ `None`
3. **`moveRobot(maze: Array[Array[Char]], robot: Array[Int], direction: Char): Boolean`**
   * Check if the destination cell in the specified direction is walkable.
   * If walkable, mutate the `robot` array in place (`robot(0) = newRow`, `robot(1) = newCol`) and return `true`.
   * If blocked or invalid direction, leave `robot` unchanged and return `false`.
4. **`collectCell(maze: Array[Array[Char]], robot: Array[Int]): Int`**
   * If the robot is currently on an energy cell (`'C'`), overwrite the grid cell with floor (`'.'`) and return `1`.
   * Otherwise, return `0`. (An energy cell can only be collected once!)
5. **`isAtExit(maze: Array[Array[Char]], robot: Array[Int]): Boolean`**
   * Return `true` if the robot's current position matches the exit marker (`'E'`).
6. **`render(maze: Array[Array[Char]], robot: Array[Int], energy: Int, cellsCollected: Int): String`**
   * Format the 2-D maze grid into a multi-line string with the robot rendered as `'@'` at its live position, followed by a status line:  
     `s"Energy: $energy  Cells: $cellsCollected"`
7. **`playMoves(maze: Array[Array[Char]], robot: Array[Int], moves: String, startEnergy: Int): Int`**
   * Execute moves sequentially from `moves`.
   * Each successful move costs 1 energy; collecting a cell awards +3 energy. Blocked moves cost 0 energy.
   * Stop immediately if energy reaches 0 (stranded) or if the robot reaches the exit (`'E'`). Return remaining energy.
8. **`playGame(maze: Array[Array[Char]], robot: Array[Int], moves: String, startEnergy: Int): (Int, Int, Boolean)`**
   * Execute moves tracking `(remainingEnergy, cellsCollected, reachedExit)`.

### Part 2: Parameter Passing & Aliasing Lab

Complete and study the parameter-passing procedures:

1. **`moveNorth(position: Array[Int]): Unit`**
   * Decrement `position(0)` by 1. Because Scala uses call-by-value with reference copying for arrays, the caller's array is mutated in place.
2. **`localReset(position: Array[Int]): Unit`**
   * Rebind a local `val replacement = Array(1, 1)`. Explain why assigning to a local variable does not alter which array the caller's variable references.
3. **`repeatUntilStopped(action: => Boolean): Int`**
   * Execute the call-by-name parameter `action` in a while-loop as long as it evaluates to `true`.
   * Return the total count of successful attempts. Explain why call-by-name evaluates `action` anew on every iteration.

### Part 3: Recursive Emergency Scanner

Implement the emergency flood-fill scanner:

1. **`revealReachable(maze: Array[Array[Char]], revealed: Array[Array[Boolean]], row: Int, col: Int): Int`**
   * Base cases:
     * `(row, col)` is out of maze bounds $\rightarrow$ return `0`
     * `maze(row)(col) == '#'` (wall) $\rightarrow$ return `0`
     * `revealed(row)(col) == true` (already visited) $\rightarrow$ return `0`
   * Recursive step:
     * Mark `revealed(row)(col) = true`
     * Recursively explore the 4 cardinal neighbors: up, down, left, right.
     * Return `1 + sum of newly revealed neighbors`.
   * **Termination condition:** Explain why setting `revealed(row)(col) = true` *before* the recursive calls guarantees termination even in mazes with loops.

### Part 4: Written Reasoning & Reflections

Complete all sections in the **Written Report Deliverables** section at the bottom of this `README.md`.

1. **State-Transition Table**: Trace 3 consecutive moves on the small test maze (`R`, `D`, `D`).
2. **Parameter-Passing Memory Diagram**: Visual diagram showing stack frames and heap arrays for `moveNorth` and `localReset`.
3. **Scanner Call-Stack Trace**: Detailed activation record trace for the 4×4 maze starting at `(1, 1)`.
4. **Recursion vs. Iteration Analysis**: Space complexity comparison (call stack vs heap stack).
5. **Procedural vs. OO Reflection**: Analysis of why loosely bundled arrays and procedures become awkward and how ADTs / OO will solve this.

---

## Nonfunctional Requirements

* **Strictly Imperative / Procedural**: Use only built-in types (`Array`, `String`, `Int`, `Boolean`, tuples, and `Option`). **Do NOT create custom classes, case classes, traits, or domain ADTs.**
* **Syntax & Style**: Use Scala 3 significant indentation (2 spaces, no unnecessary `{}`). Code must pass `sbt scalafmtCheckAll`.
* **Compiler Flags**: Code must compile cleanly with `-language:strictEquality`, `-Yexplicit-nulls`, and `-Werror` (warnings treated as errors).
* **Test Verification**: All existing tests in `MazeSolverTest.scala` must pass.
* **Student Unit Tests**: Add at least four original test cases to `StudentMazeSolverTest` covering edge cases.

---

## Extra Credit (+0.5 pt)

Implement **`revealReachableIterative`** in `MazeSolver.scala`:
* Use an explicit mutable stack (`scala.collection.mutable.Stack[(Int, Int)]`) on the heap to perform flood fill instead of recursive call stack frames.
* In your written report, contrast heap allocation with JVM stack frame limits, detailing when and why an iterative stack avoids `StackOverflowError`.

---

## Development & Testing Commands

Open a terminal in the project directory:

```bash
# Compile the project
sbt compile

# Run the test suite
sbt test

# Check code formatting (reformat with 'sbt scalafmtAll')
sbt scalafmtCheckAll

# Run test coverage verification
sbt clean coverage test coverageReport

# Stage the application executable
sbt stage

# Run the interactive game with default maze
sbt run

# Run with a specific sample maze
sbt "run labyrinth.txt"
sbt "run chambers.txt"
```

### Interactive REPL Commands
When running `sbt run`:
* `U`: Move up (costs 1 energy)
* `D`: Move down (costs 1 energy)
* `L`: Move left (costs 1 energy)
* `R`: Move right (costs 1 energy)
* `S`: Emergency scanner flood fill
* `H`: Display help message
* `Q`: Quit

---

## Submission Instructions

1. Ensure all your work is committed and pushed to your assigned GitHub Classroom repository.
2. Verify that the GitHub Actions CI workflow runs and passes cleanly.
3. Submit a brief inline note on Sakai with the URL of your repository before the deadline.

---

## Grading Criteria (Total: 6.0 Points)

| Points | Criterion | Description |
|:------:|:----------|:------------|
| **1.0** | **Navigation & Bounds** | Correct implementation of `isWalkable`, `directionDelta`, and in-place `moveRobot`. |
| **0.5** | **Game Mechanics & State** | Correct implementation of `collectCell`, `isAtExit`, `playMoves`, and `playGame`. |
| **0.5** | **Rendering** | Correct implementation of `render` with robot `@` overlay and formatted status line. |
| **0.5** | **Parameter Passing Lab** | Correct implementation of `moveNorth`, `localReset`, and memory aliasing demonstration. |
| **0.5** | **Call-by-Name Evaluation** | Correct implementation of `repeatUntilStopped` and explanation of re-evaluation. |
| **1.0** | **Recursive Flood Fill** | Correct implementation of `revealReachable` with base cases, 4-way recursion, and termination. |
| **0.5** | **Memory Diagram & Stack Trace** | Completed written deliverables: parameter passing memory diagram and scanner activation record trace. |
| **0.5** | **State Table & OO Reflection** | Completed written deliverables: state-transition table and procedural vs. OO architectural reflection. |
| **0.5** | **Student Unit Tests** | Thorough unit test cases added in `StudentMazeSolverTest` verifying edge cases. |
| **0.5** | **Scala 3 Style & Strict Typing** | Code conforms to `-Yexplicit-nulls`, `-language:strictEquality`, passes `scalafmtCheckAll`, and avoids custom domain classes. |

### Possible Deductions: up to...
* **-1.0 pt**: Modification of existing procedure signatures, test fixtures, or project architecture.
* **-1.0 pt**: Introduction of custom domain classes, case classes, traits, or ADTs (violating the imperative project constraint); we will soon work on a fully-object oriented version of this project!
* **-1.0 pt**: Compiler warnings under `-Werror` or unformatted code failing `scalafmtCheckAll`.
* **-1.0 pt**: Missing AI transcript or affidavit that AI was not used.

---

# Written Report Deliverables (Student Submissions)

> *Fill in your answers to each of the five sections below directly within this file.*

### Deliverable 1: State-Transition Table

Trace the three moves `R`, `D`, `D` on the `smallLines` 5×5 maze starting at `(1, 1)` with `energy = 10` and `cellsCollected = 0`:

```
#####
#R.C#
#.#.#
#..E#
#####
```

| Step | Move Command | Attempted Pos | Walkable? | Updated Robot Pos | Remaining Energy | Cells Collected | Note |
|:----:|:------------:|:-------------:|:---------:|:-----------------:|:----------------:|:---------------:|:-----|
| 0 | *Init* | — | — | `(1, 1)` | 10 | 0 | Initial state |
| 1 | `R` | `(1, 2)` | Yes | `(1, 2)` | 9 | 0 | Open floor `.`, costs 1 energy|
| 2 | `D` | `(2, 2)` | No | `(1, 2)` | 9 | 0 | Wall `#`, blocked, cost 0 energy |
| 3 | `D` | `(2, 2)` | No | `(1, 2)` | 9 | 0 | Still wall, blocked, cost 0 energy |

---

### Deliverable 2: Parameter-Passing & Aliasing Memory Diagram

Illustrate with an ASCII diagram the stack and heap layout during execution of:
1. `moveNorth(pos)`
2. `localReset(pos)`

```
[TODO: Provide your ASCII memory diagram here showing Stack Frames, Callee Parameters, and Heap Arrays]
```

**Written Explanation:**
1. Why does `moveNorth` mutate the caller's array, whereas passing an `Int` variable (e.g. `energy`) cannot be mutated by the callee?
   > [TODO: Your answer here]

2. Why does Scala reject `position = Array(1, 1)` in parameter lists, and what happens when you rebind a local `val replacement = Array(1, 1)`?
   > [TODO: Your answer here]

3. How does call-by-name (`action: => Boolean` in `repeatUntilStopped`) fundamentally differ from passing a reference or passing a primitive value?
   > [TODO: Your answer here]

---

### Deliverable 3: Scanner Call-Stack Trace & Activation Records

Given the 4×4 `tinyLines` maze:
```
####
#..#
#.##
####
```
Trace `revealReachable(maze, revealed, 1, 1)`:

```
[TODO: Show the sequence of activation records pushed and popped from the call stack, including arguments (row, col) and return values]
```

**Questions:**
1. What values are stored in each activation record (stack frame) on the JVM?
   > [TODO: Your answer here]

2. Which invocations hit the base cases (out of bounds, wall `#`, or already revealed)?
   > [TODO: Your answer here]

3. Why is marking `revealed(row)(col) = true` *prior* to recursive neighbor exploration essential to avoid infinite recursion?
   > [TODO: Your answer here]

4. What is the maximum theoretical stack depth in the worst-case maze of size $M \times N$?
   > [TODO: Your answer here]

---

### Deliverable 4: Recursion vs. Iteration (Call Stack vs. Heap Stack)

Compare `revealReachable` (recursive) with `revealReachableIterative` (explicit heap stack):
* **Memory location of state:**
  > [TODO: Your answer here]
* **Risk of `StackOverflowError` vs. `OutOfMemoryError`:**
  > [TODO: Your answer here]
* **Performance tradeoffs (function invocation overhead vs object allocation):**
  > [TODO: Your answer here]

---

### Deliverable 5: Architectural Reflection (Procedural vs. Object-Oriented)

In this assignment, all state is represented using raw arrays (`Array[Array[Char]]`, `Array[Int]`) and loose variables passed into top-level procedures.
1. What problems or vulnerabilities arise when multiple procedures directly read and mutate naked arrays?
   > [TODO: Your answer here]

2. How would an Object-Oriented or ADT-based design (e.g., `Maze`, `Robot`, `Cell`, `Position`) improve encapsulation, type safety, and maintainability in Project 2?
   > [TODO: Your answer here]

---

### AI Usage Disclosure
* [ ] No AI tools were used on this assignment.
* [ ] AI tools were used (describe tool, prompts, and provide transcript link below or transcript document(s) in doc/ subdirectory):
  > [TODO: AI transcript link or statement]
