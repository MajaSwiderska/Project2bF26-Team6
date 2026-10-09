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
   // Checking if that row is not negative and not past the bottom of the grid
  row >= 0 && row < maze.length &&
    // Checking if that col is not negative and not past the right edge of this row
    col >= 0 && col < maze(row).length &&
    // Checking if that cell is not a wall character
    maze(row)(col) != '#'

/** Translate a direction character into a (dRow, dCol) delta.
  *
  * Supported directions: U (up), D (down), L (left), R (right).
  *
  * @return
  *   Some((dr, dc)) for a recognised direction, None otherwise
  */
def directionDelta(direction: Char): Option[(Int, Int)] =
  // Matching on the direction character
  direction match 
    // up: decrease row by 1, col stays the same
    case 'U' => Some((-1, 0))
    // down: increase row by 1, col stays the same
    case 'D' => Some((1, 0))
    // left: row stays the same, decrease col by 1
    case 'L' => Some((0, -1))
    // right: row stays the same, increase col by 1
    case 'R' => Some((0, 1))
    // Any other character is not a valid direction
    case _   => None

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
  // look up the delta for this direction
  directionDelta(direction) match
    // valid direction: computing the target cell
    case Some((dr, dc)) =>
      val newRow = robot(0) + dr
      val newCol = robot(1) + dc
      // only move if the target cell is walkable
      if isWalkable(maze, newRow, newCol) then
        // mutate the robot array in place so the caller sees the new position
        robot(0) = newRow
        robot(1) = newCol
        true
      else
        // blocked: leaves the robot where it is
        false
    // invalid direction: no move, no mutation
    case None =>
        false

/** If the robot is standing on an energy cell ('C'), collect it.
  *
  * The cell is replaced with '.' and 1 is returned; otherwise 0.
  */
def collectCell(maze: Array[Array[Char]], robot: Array[Int]): Int =
  // reads the robots current position
  val row = robot(0)
  val col = robot(1)
  // checks if we're inside the grid and standing on an energy cell
  if isWalkable(maze, row, col) && maze(row)(col) == 'C' then
    // overwrite the cell with floor so it cannot be collected twice
    maze(row)(col) = '.'
    // return 1 to signal that a cell was collected
    1
  else
    // nothing to collect
    0

/** True when the robot is standing on the exit ('E'). */
def isAtExit(maze: Array[Array[Char]], robot: Array[Int]): Boolean =
  // reads the robots current position
  val row = robot(0)
  val col = robot(1)
  // gaurd with isWalkable to avoid out-of-bounds access, then it checks for 'E'
  isWalkable(maze, row, col) && maze(row)(col) == 'E'

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
  // StringBuilder accumulates the output line by line
  val sb = new StringBuilder
  // remembers where the robot is so we can overlay '@' there
  val rr = robot(0)
  val rc = robot(1)
  // walks through every row of the maze
  var row = 0
  while row < maze.length do
    // walks through every column of this row
    var col = 0
    while col < maze(row).length do
      // if this cell is the robots position, it draws '@'
      if row == rr && col == rc then sb.append('@')
      // otherwise draw whatever is in the maze (floor, wall, exit, so on)
      else sb.append(maze(row)(col))
      col += 1
      // end of the row, add a newline
    sb.append('\n')
    row += 1
  // append the status line after the grid
  sb.append(s"Energy: $energy  Cells: $cellsCollected")
  // converts the StringBuilder to a String and return it
  sb.toString
  
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
  // tracks the remaining energy
  var energy = startEnergy
  // index into the moves string
  var i = 0
  // loop control flag, sets to false when we reach the exit
  var running = true
  // loop while: the game is running, more moves to process and we have energy left
  while running && i < moves.length && energy > 0 do
    // trying to move in the direction of the current character
    if moveRobot(maze, robot, moves.charAt(i)) then
      // successful move costs 1 energy
      energy -= 1
      // if we landed on an energy cell, it collects it and add +3 energy
      if collectCell(maze, robot) > 0 then energy += 3
      // if we're now on the exit, stop the loop
      if isAtExit(maze, robot) then running = false
    // blocked moves does cost 0 energy, so nothing to do in the else case
    i += 1
  // return however much energy is left
  energy

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
  // tracks energy, cells collected, and wheather we escaped
  var energy = startEnergy
  var cells = 0
  var reachedExit = false
  // index and loop control
  var i = 0
  var running = true
  // same loop structure as playMoves
  while running && i < moves.length && energy > 0 do
    if moveRobot(maze, robot, moves.charAt(i)) then
      energy -= 1
      // collectCell returns 0 or 1, so we add it directly to cells
      val collected = collectCell(maze, robot)
      if collected > 0 then
        energy += 3
        cells += collected
      // if we reached the exit, remember it and stop the loop
      if isAtExit(maze, robot) then
        reachedExit = true
        running = false
    i += 1
  // return all three pieces of info as a tuple
  (energy, cells, reachedExit)

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
  if startRow < 0 || startRow >= maze.length || startCol < 0 || startCol >= maze(startRow).length then 0
  else if maze(startRow)(startCol) == '#' || revealed(startRow)(startCol) then 0
  else
    val stack = scala.collection.mutable.Stack[(Int, Int)]()
    stack.push((startRow, startCol))
    var count = 0

    while stack.nonEmpty do
      val (r, c) = stack.pop()
      if r >= 0 && r < maze.length && c >= 0 && c < maze(r).length then
        if maze(r)(c) != '#' && !revealed(r)(c) then
          revealed(r)(c) = true
          count += 1
          stack.push((r - 1, c)) // Up
          stack.push((r + 1, c)) // Down
          stack.push((r, c - 1)) // Left
          stack.push((r, c + 1)) // Right

    count

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
