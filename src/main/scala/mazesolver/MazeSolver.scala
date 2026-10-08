package mazesolver

// ─── Part 1: State and procedural abstraction ────────────────────────────────

/** Available external sample maze resource names. */
val sampleMazeNames: Array[String] = Array(
  "default.txt",
  "tiny.txt",
  "corridor.txt",
  "chambers.txt",
  "arena.txt",
  "labyrinth.txt"
)

/** Load maze lines from a classpath resource in `mazes/` (e.g., "default.txt" or
  * "mazes/default.txt").
  */
def loadMazeFromResource(resourceName: String): Array[String] =
  val path = if resourceName.startsWith("mazes/") then resourceName else s"mazes/$resourceName"
  val source = scala.io.Source.fromResource(path)
  try source.getLines().toArray
  finally source.close()

/** Load maze lines from a local file path. */
def loadMazeFromFile(filePath: String): Array[String] =
  val source = scala.io.Source.fromFile(filePath)
  try source.getLines().toArray
  finally source.close()

/** The default maze loaded from resource "mazes/default.txt". */
val defaultMazeLines: Array[String] = loadMazeFromResource("default.txt")

/** Parse an array of strings into a mutable 2-D character grid. */
def parseMaze(lines: Array[String]): Array[Array[Char]] =
  lines.map(_.toCharArray)

/** Locate the first occurrence of `target` in the maze.
  *
  * @return
  *   Some((row, col)) if found, None otherwise
  */
def findChar(maze: Array[Array[Char]], target: Char): Option[(Int, Int)] =
  var r = 0
  while r < maze.length do
    var c = 0
    while c < maze(r).length do
      if maze(r)(c) == target then return Some((r, c))
      c += 1
    r += 1
  None

/** Create a fresh maze together with the robot position and initial energy.
  *
  * The robot's start marker 'R' is replaced with '.' in the grid, and the position array is set to
  * (startRow, startCol).
  *
  * @return
  *   (maze, robot, energy, cellsCollected) where robot = Array(row, col)
  */
def initGame(lines: Array[String] = defaultMazeLines, startEnergy: Int = 12)
    : (Array[Array[Char]], Array[Int], Int, Int) =
  val maze = parseMaze(lines)
  val (sr, sc) = findChar(maze, 'R').getOrElse((1, 1))
  maze(sr)(sc) = '.' // clear the start marker
  val robot = Array(sr, sc)
  (maze, robot, startEnergy, 0)

// ─── Movement helpers ────────────────────────────────────────────────────────

/** True when the cell at (row, col) is inside the grid and is not a wall. */
def isWalkable(maze: Array[Array[Char]], row: Int, col: Int): Boolean =
  // TODO: Check if (row, col) is within maze bounds and not a wall ('#')
  false

/** Translate a direction character into a (dRow, dCol) delta.
  *
  * Supported directions: U (up), D (down), L (left), R (right).
  *
  * @return
  *   Some((dr, dc)) for a recognised direction, None otherwise
  */
def directionDelta(direction: Char): Option[(Int, Int)] =
  // TODO: Map 'U', 'D', 'L', 'R' to their corresponding (dRow, dCol) coordinate offsets
  None

/** Try to move the robot one step in the given direction.
  *
  * The robot array is mutated in place when the move succeeds.
  *
  * @return
  *   true if the move was carried out, false if blocked or direction invalid
  */
def moveRobot(
    maze: Array[Array[Char]],
    robot: Array[Int],
    direction: Char
): Boolean =
  // TODO: If the target cell in the given direction is walkable, update robot(0) and robot(1)
  // in place and return true; otherwise return false
  false

/** If the robot is standing on an energy cell ('C'), collect it.
  *
  * The cell is replaced with '.' and 1 is returned; otherwise 0.
  */
def collectCell(maze: Array[Array[Char]], robot: Array[Int]): Int =
  // TODO: If the robot is standing on an energy cell ('C'), replace it with '.' and return 1;
  // otherwise return 0
  0

/** True when the robot is standing on the exit ('E'). */
def isAtExit(maze: Array[Array[Char]], robot: Array[Int]): Boolean =
  // TODO: Return true if the robot is standing on the exit marker ('E')
  false

// ─── Rendering ───────────────────────────────────────────────────────────────

/** Render the maze with the robot shown as '@' at its current position.
  *
  * The status line shows remaining energy and cells collected.
  */
def render(
    maze: Array[Array[Char]],
    robot: Array[Int],
    energy: Int,
    cellsCollected: Int = 0
): String =
  // TODO: Render the maze grid with '@' at the robot's current position,
  // followed by a status line: s"Energy: $energy  Cells: $cellsCollected"
  ""

// ─── Game loop ───────────────────────────────────────────────────────────────

/** Execute a sequence of moves on the maze.
  *
  * Each move costs 1 energy. Collecting an energy cell restores 3 energy. The game ends when energy
  * reaches 0 or the robot reaches the exit.
  *
  * @return
  *   remaining energy at the end of the run (0 means stranded)
  */
def playMoves(
    maze: Array[Array[Char]],
    robot: Array[Int],
    moves: String,
    startEnergy: Int = 12
): Int =
  // TODO: Execute moves string sequentially, deducting 1 energy per valid move,
  // adding 3 energy per collected cell ('C'), stopping when stranded (0 energy) or at exit ('E').
  // Return remaining energy.
  0

/** A cleaner playMoves that properly tracks cell collection and energy bonus.
  *
  * Each step: move (costs 1 energy), then check for cell (+3 energy) and exit.
  *
  * @return
  *   (remainingEnergy, cellsCollected, reachedExit)
  */
def playGame(
    maze: Array[Array[Char]],
    robot: Array[Int],
    moves: String,
    startEnergy: Int = 12
): (Int, Int, Boolean) =
  // TODO: Execute moves string, deducting 1 energy per valid move, adding 3 per collected cell.
  // Track cells collected and whether the robot reached the exit.
  // Return (remainingEnergy, cellsCollected, reachedExit).
  (startEnergy, 0, false)

// ─── Part 2: Parameter passing and aliasing ──────────────────────────────────

/** Mutates the shared array — caller sees the change. */
def moveNorth(position: Array[Int]): Unit =
  // TODO: Mutate position(0) to move north (decrement row by 1)
      position(0) = position(0) - 1

/** Demonstrates that rebinding a local val cannot affect the caller's reference.
  *
  * NOTE: `position = Array(1, 1)` would be rejected by Scala because parameters are vals, not vars.
  * This version shows the same principle.
  */
def localReset(position: Array[Int]): Unit =
  // TODO: Explore parameter passing semantics: create a local val `replacement = Array(1, 1)`
  // and demonstrate why the caller's position array is unaffected.
      val replacement = Array(1, 1)

// ─── Part 2 (cont.): Call by name ────────────────────────────────────────────

/** Repeatedly evaluate `action` (call-by-name) until it returns false.
  *
  * Because `action` is declared `=> Boolean`, it is re-evaluated on every iteration of the while
  * loop — unlike call-by-value where it would be evaluated once and the result reused.
  *
  * @return
  *   the number of times action returned true
  */
def repeatUntilStopped(action: => Boolean): Int =
  // TODO: Repeatedly evaluate call-by-name action until it evaluates to false,
  // returning the number of times action returned true.
  var count = 0
  while action do
    count += 1
  count

// ─── Part 3: Recursive flood-fill scanner ────────────────────────────────────

/** Create a blank revealed grid of the same dimensions as the maze. */
def makeRevealed(maze: Array[Array[Char]]): Array[Array[Boolean]] =
  Array.fill(maze.length)(Array.fill(maze(0).length)(false))

/** Reveal all reachable (non-wall) cells from (row, col) using recursive flood fill.
  *
  * Base cases:
  *   - out of bounds → return 0
  *   - wall ('#') → return 0
  *   - already revealed → return 0
  *
  * Recursive case: mark (row, col) as revealed, then recurse into all four neighbours.
  *
  * @return
  *   the number of newly revealed cells
  */
def revealReachable(
    maze: Array[Array[Char]],
    revealed: Array[Array[Boolean]],
    row: Int,
    col: Int
): Int =
  // TODO: Reveal all reachable (non-wall) cells from (row, col) using recursive flood fill.
  // Base cases:
  //   - out of bounds -> return 0
  //   - wall ('#') -> return 0
  //   - already revealed -> return 0
  // Recursive step:
  //   - mark (row, col) as revealed
  //   - recurse into all four neighbours (up, down, left, right)
  //   - return 1 + sum of newly revealed neighbours
  if row < 0 || row >= maze.length || col < 0 || col >= maze(row).length then 0
  else if maze(row)(col) == '#' then 0
  else if revealed(row)(col) then 0
  else
    revealed(row)(col) = true
    var count = 1
    count += revealReachable(maze, revealed, row - 1, col) // Up
    count += revealReachable(maze, revealed, row + 1, col) // Down
    count += revealReachable(maze, revealed, row, col - 1) // Left
    count += revealReachable(maze, revealed, row, col + 1) // Right
    count

/** Iterative flood-fill using an explicit mutable stack (stretch goal / extra credit).
  *
  * Functionally equivalent to `revealReachable` but uses heap-allocated stack space instead of the
  * call stack.
  *
  * @return
  *   the number of newly revealed cells
  */
def revealReachableIterative(
    maze: Array[Array[Char]],
    revealed: Array[Array[Boolean]],
    startRow: Int,
    startCol: Int
): Int =
  // TODO (Extra Credit): Implement iterative flood-fill using an explicit mutable stack
  // (scala.collection.mutable.Stack).
  0

/** Help message describing all interactive REPL commands. */
def helpMessage(): String =
  """Commands:
    |  U (up)    : Move up (costs 1 energy)
    |  D (down)  : Move down (costs 1 energy)
    |  L (left)  : Move left (costs 1 energy)
    |  R (right) : Move right (costs 1 energy)
    |  S (scan)  : Reveal reachable floor cells using flood fill
    |  H (help)  : Show this help message
    |  Q (quit)  : Quit the game""".stripMargin

/** Print interactive REPL help message. */
def printHelp(): Unit =
  println(helpMessage())
  println(s"Sample mazes: ${sampleMazeNames.mkString(", ")}")

/** Simple REPL: reads one character at a time from stdin. */
@main def main(args: String*): Unit =
  val mazeName = args.headOption.getOrElse("default.txt")
  val lines = loadMazeFromResource(mazeName)
  val (maze, robot, startEnergy, _) = initGame(lines)
  var energy = startEnergy
  var cellsCollected = 0

  println(s"Maze Rescue ($mazeName) — reach the exit (E) before energy runs out!")
  println(s"Sample mazes: ${sampleMazeNames.mkString(", ")}")
  println("Commands: U (up), D (down), L (left), R (right), S (scan), H (help), Q (quit)\n")
  println(render(maze, robot, energy, cellsCollected))

  var running = true
  while running && energy > 0 do
    print("\n> ")
    scala.io.StdIn.readLine() match
      case line: String if line.nonEmpty =>
        val cmd = line.trim.nn.toUpperCase.nn.head
        cmd match
          case 'H' =>
            printHelp()
          case 'Q' =>
            running = false
          case 'S' =>
            val revealed = makeRevealed(maze)
            val n = revealReachable(maze, revealed, robot(0), robot(1))
            println(s"Scanner revealed $n reachable cells.")
          case dir =>
            if moveRobot(maze, robot, dir) then
              energy -= 1
              val c = collectCell(maze, robot)
              if c > 0 then
                cellsCollected += c
                energy += 3
                println(s"Collected an energy cell! (+3 energy)")
              if isAtExit(maze, robot) then
                println(render(maze, robot, energy, cellsCollected))
                println(s"\n🎉  You escaped with $energy energy and $cellsCollected cell(s)!")
                running = false
            else
              println("Blocked!")
        if running then println(render(maze, robot, energy, cellsCollected))
      case _ =>
        running = false

  if energy <= 0 then
    println("\n💀  Out of energy — stranded on the station!")
