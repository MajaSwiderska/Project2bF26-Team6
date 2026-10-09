package mazesolver

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName

// ═══════════════════════════════════════════════════════════════════════════════
// Part 1 — State and procedural abstraction
// ═══════════════════════════════════════════════════════════════════════════════

class MazeSolverTest:

  // Small 5×5 maze used by most unit tests
  //
  //   #####
  //   #R.C#
  //   #.#.#
  //   #..E#
  //   #####
  //
  val smallLines: Array[String] = Array(
    "#####",
    "#R.C#",
    "#.#.#",
    "#..E#",
    "#####"
  )

  // ─── parseMaze / findChar ────────────────────────────────────────────────

  @Test @DisplayName("parseMaze produces correct dimensions")
  def testParseMazeDimensions(): Unit =
    val maze = parseMaze(smallLines)
    assertEquals(5, maze.length)
    assertEquals(5, maze(0).length)

  @Test @DisplayName("findChar locates the robot start")
  def testFindCharRobot(): Unit =
    val maze = parseMaze(smallLines)
    assertEquals(Some((1, 1)), findChar(maze, 'R'))

  @Test @DisplayName("findChar locates the exit")
  def testFindCharExit(): Unit =
    val maze = parseMaze(smallLines)
    assertEquals(Some((3, 3)), findChar(maze, 'E'))

  @Test @DisplayName("findChar returns None for absent character")
  def testFindCharMissing(): Unit =
    val maze = parseMaze(smallLines)
    assertEquals(None, findChar(maze, 'Z'))

  // ─── initGame ────────────────────────────────────────────────────────────

  @Test @DisplayName("initGame clears the robot marker and sets position")
  def testInitGame(): Unit =
    val (maze, robot, energy, cells) = initGame(smallLines, 10)
    assertEquals('.', maze(1)(1), "Robot marker should be replaced with '.'")
    assertEquals(1, robot(0))
    assertEquals(1, robot(1))
    assertEquals(10, energy)
    assertEquals(0, cells)

  // ─── isWalkable ──────────────────────────────────────────────────────────

  @Test @DisplayName("isWalkable: floor cell is walkable")
  def testIsWalkableFloor(): Unit =
    val maze = parseMaze(smallLines)
    assertTrue(isWalkable(maze, 1, 2))

  @Test @DisplayName("isWalkable: wall cell is not walkable")
  def testIsWalkableWall(): Unit =
    val maze = parseMaze(smallLines)
    assertFalse(isWalkable(maze, 0, 0))

  @Test @DisplayName("isWalkable: out of bounds is not walkable")
  def testIsWalkableOutOfBounds(): Unit =
    val maze = parseMaze(smallLines)
    assertFalse(isWalkable(maze, -1, 0))
    assertFalse(isWalkable(maze, 5, 0))
    assertFalse(isWalkable(maze, 0, -1))
    assertFalse(isWalkable(maze, 0, 5))

  @Test @DisplayName("isWalkable: energy cell is walkable")
  def testIsWalkableEnergyCell(): Unit =
    val maze = parseMaze(smallLines)
    assertTrue(isWalkable(maze, 1, 3))

  @Test @DisplayName("isWalkable: exit is walkable")
  def testIsWalkableExit(): Unit =
    val maze = parseMaze(smallLines)
    assertTrue(isWalkable(maze, 3, 3))

  // ─── directionDelta ──────────────────────────────────────────────────────

  @Test @DisplayName("directionDelta returns correct deltas")
  def testDirectionDelta(): Unit =
    assertEquals(Some((-1, 0)), directionDelta('U'))
    assertEquals(Some((1, 0)), directionDelta('D'))
    assertEquals(Some((0, -1)), directionDelta('L'))
    assertEquals(Some((0, 1)), directionDelta('R'))

  @Test @DisplayName("directionDelta returns None for invalid direction")
  def testDirectionDeltaInvalid(): Unit =
    assertEquals(None, directionDelta('X'))

  // ─── moveRobot ───────────────────────────────────────────────────────────

  @Test @DisplayName("moveRobot moves right on open floor")
  def testMoveRobotRight(): Unit =
    val (maze, robot, _, _) = initGame(smallLines)
    assertTrue(moveRobot(maze, robot, 'R'))
    assertEquals(1, robot(0))
    assertEquals(2, robot(1))

  @Test @DisplayName("moveRobot blocked by wall returns false")
  def testMoveRobotBlocked(): Unit =
    val (maze, robot, _, _) = initGame(smallLines)
    assertFalse(moveRobot(maze, robot, 'U'))
    // position unchanged
    assertEquals(1, robot(0))
    assertEquals(1, robot(1))

  @Test @DisplayName("moveRobot with invalid direction returns false")
  def testMoveRobotInvalidDir(): Unit =
    val (maze, robot, _, _) = initGame(smallLines)
    assertFalse(moveRobot(maze, robot, 'Z'))

  @Test @DisplayName("moveRobot: sequence of moves updates position correctly")
  def testMoveRobotSequence(): Unit =
    val (maze, robot, _, _) = initGame(smallLines)
    // R(1,1) → R → (1,2) → D → (2,2) blocked by #, stays (1,2)
    assertTrue(moveRobot(maze, robot, 'R'))
    assertEquals(1, robot(0))
    assertEquals(2, robot(1))
    // (1,2) → D → (2,2) which is '#' in smallLines
    assertFalse(moveRobot(maze, robot, 'D'))
    assertEquals(1, robot(0))
    assertEquals(2, robot(1))

  // ─── collectCell ─────────────────────────────────────────────────────────

  @Test @DisplayName("collectCell picks up an energy cell")
  def testCollectCell(): Unit =
    val (maze, robot, _, _) = initGame(smallLines)
    // move to (1,3) where 'C' lives: R R
    moveRobot(maze, robot, 'R')
    moveRobot(maze, robot, 'R')
    assertEquals(1, collectCell(maze, robot))
    assertEquals('.', maze(1)(3), "Cell should become floor after collection")

  @Test @DisplayName("collectCell on empty floor returns 0")
  def testCollectCellEmpty(): Unit =
    val (maze, robot, _, _) = initGame(smallLines)
    assertEquals(0, collectCell(maze, robot))

  @Test @DisplayName("collectCell cannot collect same cell twice")
  def testCollectCellTwice(): Unit =
    val (maze, robot, _, _) = initGame(smallLines)
    moveRobot(maze, robot, 'R')
    moveRobot(maze, robot, 'R')
    assertEquals(1, collectCell(maze, robot))
    assertEquals(0, collectCell(maze, robot))

  // ─── isAtExit ────────────────────────────────────────────────────────────

  @Test @DisplayName("isAtExit is false when not at exit")
  def testIsAtExitFalse(): Unit =
    val (maze, robot, _, _) = initGame(smallLines)
    assertFalse(isAtExit(maze, robot))

  @Test @DisplayName("isAtExit is true when robot reaches E")
  def testIsAtExitTrue(): Unit =
    val (maze, robot, _, _) = initGame(smallLines)
    // Navigate to (3,3): D D R R
    moveRobot(maze, robot, 'D')
    moveRobot(maze, robot, 'D')
    moveRobot(maze, robot, 'R')
    moveRobot(maze, robot, 'R')
    assertTrue(isAtExit(maze, robot))

  // ─── render ──────────────────────────────────────────────────────────────

  @Test @DisplayName("render shows robot as '@' at current position")
  def testRender(): Unit =
    val (maze, robot, energy, cells) = initGame(smallLines, 10)
    val output = render(maze, robot, energy, cells)
    assertTrue(output.contains("@"), "Render should show '@' for the robot")
    assertTrue(output.contains("Energy: 10"), "Render should show energy")

  @Test @DisplayName("render does not mutate maze")
  def testRenderNoMutation(): Unit =
    val (maze, robot, energy, cells) = initGame(smallLines, 10)
    render(maze, robot, energy, cells)
    assertEquals('.', maze(robot(0))(robot(1)), "Render should not change maze cells")

  // ─── playGame (integration) ──────────────────────────────────────────────

  @Test @DisplayName("playGame: reach exit via D D R R")
  def testPlayGameReachExit(): Unit =
    val (maze, robot, _, _) = initGame(smallLines, 10)
    val (energy, cells, escaped) = playGame(maze, robot, "DDRR", 10)
    assertTrue(escaped)
    assertEquals(6, energy, "4 moves cost 4 energy → 10 - 4 = 6")
    assertEquals(0, cells)

  @Test @DisplayName("playGame: collect cell then exit via R R D D R")
  def testPlayGameCollectAndExit(): Unit =
    val (maze, robot, _, _) = initGame(smallLines, 10)
    // path: R(1,1)→(1,2)→(1,3)collect C→D blocked by #→ we need another path
    // R R picks up C at (1,3), then D blocked by wall at (2,3)='.'? Let's check
    // smallLines row 2 = "#.#.#" so (2,3)='.' — walkable
    // (1,3)→D→(2,3)→D→(3,3) exit
    val (energy, cells, escaped) = playGame(maze, robot, "RRDDE", 10)
    // 4 moves executed (R,R,D,D) before exit; exit detected after 4th move
    assertTrue(escaped)
    assertEquals(1, cells, "Should collect 1 energy cell")
    // 10 - 4 moves + 3 bonus = 9
    assertEquals(9, energy)

  @Test @DisplayName("playGame: energy runs out")
  def testPlayGameEnergyOut(): Unit =
    // Use a maze with no energy cells so the robot truly runs out
    val noCellLines = Array(
      "#####",
      "#...#",
      "#...#",
      "#..E#",
      "#####"
    )
    val maze = parseMaze(noCellLines)
    val robot = Array(1, 1)
    val (energy, cells, escaped) = playGame(maze, robot, "RRRRRR", 2)
    assertFalse(escaped)
    assertEquals(0, energy)

  @Test @DisplayName("playGame: blocked moves do not cost energy")
  def testPlayGameBlockedFree(): Unit =
    val (maze, robot, _, _) = initGame(smallLines, 10)
    // U is blocked (wall), robot stays put, no energy cost
    val (energy, _, _) = playGame(maze, robot, "U", 10)
    assertEquals(10, energy)

  // ─── State-transition table for three moves (Part 4 requirement) ─────────

  @Test @DisplayName("state transitions: three-move trace R, D, D")
  def testStateTransitionTable(): Unit =
    val (maze, robot, _, _) = initGame(smallLines, 10)
    // Move 1: R → (1,1)→(1,2), energy 10→9
    assertTrue(moveRobot(maze, robot, 'R'))
    assertArrayEquals(Array(1, 2), robot)

    // Move 2: D → (1,2)→(2,2) which is '#' → blocked
    assertFalse(moveRobot(maze, robot, 'D'))
    assertArrayEquals(Array(1, 2), robot)

    // Move 3: D again — still blocked
    assertFalse(moveRobot(maze, robot, 'D'))
    assertArrayEquals(Array(1, 2), robot)

// ═══════════════════════════════════════════════════════════════════════════════
// Part 2 — Parameter passing and aliasing
// ═══════════════════════════════════════════════════════════════════════════════

class ParameterPassingTest:

  @Test @DisplayName("moveNorth mutates the shared array")
  def testMoveNorthMutatesArray(): Unit =
    val pos = Array(3, 4)
    moveNorth(pos)
    assertEquals(2, pos(0), "Row should decrease by 1")
    assertEquals(4, pos(1), "Column should be unchanged")

  @Test @DisplayName("localReset does NOT change caller's array")
  def testLocalResetDoesNotAffectCaller(): Unit =
    val pos = Array(3, 4)
    localReset(pos)
    assertEquals(3, pos(0), "Row should remain 3")
    assertEquals(4, pos(1), "Column should remain 4")

  @Test @DisplayName("aliasing: two references to same array see mutations")
  def testAliasing(): Unit =
    val a = Array(5, 5)
    val b = a // alias — same underlying array
    moveNorth(a)
    assertEquals(4, b(0), "b sees a's mutation because they alias the same array")

  @Test @DisplayName("Int parameter is copied — caller's value unchanged")
  def testIntCopySemantics(): Unit =
    var energy = 10
    def consumeEnergy(e: Int): Int = e - 1
    val result = consumeEnergy(energy)
    assertEquals(10, energy, "Original energy unchanged")
    assertEquals(9, result)

  // ─── Call by name ────────────────────────────────────────────────────────

  @Test @DisplayName("repeatUntilStopped re-evaluates by-name argument")
  def testRepeatUntilStopped(): Unit =
    var counter = 5
    val attempts = repeatUntilStopped {
      counter -= 1
      counter > 0
    }
    // counter goes 4,3,2,1,0 — action returns true 4 times, then false
    assertEquals(4, attempts)
    assertEquals(0, counter)

  @Test @DisplayName("repeatUntilStopped with always-false returns 0")
  def testRepeatUntilStoppedImmediate(): Unit =
    val attempts = repeatUntilStopped(false)
    assertEquals(0, attempts)

  @Test @DisplayName("repeatUntilStopped with energy-consuming action")
  def testRepeatUntilStoppedEnergy(): Unit =
    var energy = 3
    val steps = repeatUntilStopped {
      energy -= 1
      energy > 0
    }
    assertEquals(2, steps)
    assertEquals(0, energy)

// ═══════════════════════════════════════════════════════════════════════════════
// Part 3 — Recursive flood-fill scanner
// ═══════════════════════════════════════════════════════════════════════════════

class FloodFillTest:

  // 4×4 maze for focused scanner tests
  //
  //   ####
  //   #..#
  //   #.##
  //   ####
  //
  val tinyLines: Array[String] = Array(
    "####",
    "#..#",
    "#.##",
    "####"
  )

  @Test @DisplayName("revealReachable counts all reachable floor cells")
  def testRevealReachableCount(): Unit =
    val maze = parseMaze(tinyLines)
    val revealed = makeRevealed(maze)
    val count = revealReachable(maze, revealed, 1, 1)
    assertEquals(3, count, "3 floor cells: (1,1),(1,2),(2,1)")

  @Test @DisplayName("revealReachable marks cells as revealed")
  def testRevealReachableMarks(): Unit =
    val maze = parseMaze(tinyLines)
    val revealed = makeRevealed(maze)
    revealReachable(maze, revealed, 1, 1)
    assertTrue(revealed(1)(1))
    assertTrue(revealed(1)(2))
    assertTrue(revealed(2)(1))
    assertFalse(revealed(0)(0), "Walls should not be revealed")
    assertFalse(revealed(2)(2), "Walls should not be revealed")

  @Test @DisplayName("revealReachable on wall returns 0")
  def testRevealReachableOnWall(): Unit =
    val maze = parseMaze(tinyLines)
    val revealed = makeRevealed(maze)
    assertEquals(0, revealReachable(maze, revealed, 0, 0))

  @Test @DisplayName("revealReachable out of bounds returns 0")
  def testRevealReachableOutOfBounds(): Unit =
    val maze = parseMaze(tinyLines)
    val revealed = makeRevealed(maze)
    assertEquals(0, revealReachable(maze, revealed, -1, 0))
    assertEquals(0, revealReachable(maze, revealed, 0, -1))

  @Test @DisplayName("revealReachable already-revealed cell returns 0")
  def testRevealReachableAlreadyRevealed(): Unit =
    val maze = parseMaze(tinyLines)
    val revealed = makeRevealed(maze)
    revealReachable(maze, revealed, 1, 1) // first pass
    val secondPass = revealReachable(maze, revealed, 1, 1)
    assertEquals(0, secondPass, "No new cells on second pass")

  @Test @DisplayName("revealReachable on default maze from robot start")
  def testRevealReachableDefaultMaze(): Unit =
    val (maze, robot, _, _) = initGame()
    val revealed = makeRevealed(maze)
    val count = revealReachable(maze, revealed, robot(0), robot(1))
    // Count all non-wall cells in the default maze (connected region from R)
    // Row 1: R . . | . . . . E → 8 non-wall (but R replaced with .)
    // Row 2: . # . | . # # . . → 5 non-wall
    // Row 3: . # . . . C # . . → 6 non-wall
    // Row 4: . # # # # # . # . → 3 non-wall
    // Row 5: . . . C . . . # . → 6 non-wall
    // Total reachable from (1,1): all 28 non-wall cells are connected
    assertTrue(count > 0)
    // Verify the exit is reachable
    assertTrue(revealed(1)(9), "Exit at (1,9) should be reachable")

  // ─── Iterative flood fill (stretch goal) ─────────────────────────────────

  @Test @DisplayName("iterative flood fill matches recursive result")
  def testIterativeMatchesRecursive(): Unit =
    val maze1 = parseMaze(tinyLines)
    val revealed1 = makeRevealed(maze1)
    val countRec = revealReachable(maze1, revealed1, 1, 1)

    val maze2 = parseMaze(tinyLines)
    val revealed2 = makeRevealed(maze2)
    val countIter = revealReachableIterative(maze2, revealed2, 1, 1)

    assertEquals(countRec, countIter, "Iterative and recursive should reveal same count")

    // Same cells should be revealed
    for
      r <- maze1.indices
      c <- maze1(r).indices
    do assertEquals(revealed1(r)(c), revealed2(r)(c), s"Mismatch at ($r,$c)")

  @Test @DisplayName("iterative flood fill on default maze")
  def testIterativeDefaultMaze(): Unit =
    val (maze, robot, _, _) = initGame()
    val revealed = makeRevealed(maze)
    val count = revealReachableIterative(maze, revealed, robot(0), robot(1))
    assertTrue(count > 0)
    assertTrue(revealed(1)(9), "Exit should be reachable")

  // ─── Disconnected region ─────────────────────────────────────────────────

  @Test @DisplayName("revealReachable does not cross walls to disconnected region")
  def testDisconnectedRegion(): Unit =
    // Two rooms separated by a wall column
    val lines = Array(
      "#####",
      "#.#.#",
      "#.#.#",
      "#####"
    )
    val maze = parseMaze(lines)
    val revealed = makeRevealed(maze)
    val count = revealReachable(maze, revealed, 1, 1)
    assertEquals(2, count, "Only left room: (1,1) and (2,1)")
    assertFalse(revealed(1)(3), "Right room should not be revealed")
    assertFalse(revealed(2)(3), "Right room should not be revealed")

// ═══════════════════════════════════════════════════════════════════════════════
// Integration — full game scenario on default maze
// ═══════════════════════════════════════════════════════════════════════════════

class IntegrationTest:

  @Test @DisplayName("full game: solve default maze collecting one cell")
  def testFullGameSolve(): Unit =
    val (maze, robot, _, _) = initGame()
    // Winning path on the default maze (robot starts at (1,1), exit at (1,9)):
    //
    //   ###########
    //   #@..#....E#    (1,1) → R → R → D → D → R → R → R(collect C at 3,6)
    //   #.#.#.##..#          → L → U → U → R → R → R → R(exit at 1,9)
    //   #.#...C#..#
    //   #.#####.#.#
    //   #...C...#.#
    //   ###########
    //
    // 14 moves total, 1 cell collected (+3 energy), energy = 12 - 14 + 3 = 1
    val winningPath = "RRDDRRRLUURRRR"

    val (energy, cells, escaped) = playGame(maze, robot, winningPath, 12)
    assertTrue(escaped, "Robot should reach the exit")
    assertEquals(1, cells, "Should collect 1 energy cell")
    assertEquals(1, energy)

  @Test @DisplayName("render of initial default maze contains robot and exit")
  def testRenderDefaultMaze(): Unit =
    val (maze, robot, energy, cells) = initGame()
    val output = render(maze, robot, energy, cells)
    assertTrue(output.contains("@"))
    assertTrue(output.contains("E"))
    assertTrue(output.contains("C"))
    assertFalse(output.contains("R"), "Robot marker should be replaced with '@'")

// ═══════════════════════════════════════════════════════════════════════════════
// External sample mazes verification
// ═══════════════════════════════════════════════════════════════════════════════

class SampleMazesTest:

  @Test @DisplayName("sampleMazeNames contains 6 mazes")
  def testSampleMazeCount(): Unit =
    assertEquals(6, sampleMazeNames.length)

  @Test @DisplayName("load default.txt resource (11x7)")
  def testLoadDefaultMaze(): Unit =
    val lines = loadMazeFromResource("default.txt")
    assertEquals(7, lines.length, "Height should be 7")
    assertEquals(11, lines(0).length, "Width should be 11")

  @Test @DisplayName("load tiny.txt resource (7x5)")
  def testLoadTinyMaze(): Unit =
    val lines = loadMazeFromResource("tiny.txt")
    assertEquals(5, lines.length, "Height should be 5")
    assertEquals(7, lines(0).length, "Width should be 7")

  @Test @DisplayName("load corridor.txt resource (15x5 non-square)")
  def testLoadCorridorMaze(): Unit =
    val lines = loadMazeFromResource("corridor.txt")
    assertEquals(5, lines.length, "Height should be 5")
    assertEquals(15, lines(0).length, "Width should be 15")

  @Test @DisplayName("load chambers.txt resource (13x7 non-square)")
  def testLoadChambersMaze(): Unit =
    val lines = loadMazeFromResource("chambers.txt")
    assertEquals(7, lines.length, "Height should be 7")
    assertEquals(13, lines(0).length, "Width should be 13")

  @Test @DisplayName("load arena.txt resource (16x5 non-square)")
  def testLoadArenaMaze(): Unit =
    val lines = loadMazeFromResource("arena.txt")
    assertEquals(5, lines.length, "Height should be 5")
    assertEquals(16, lines(0).length, "Width should be 16")

  @Test @DisplayName("load labyrinth.txt resource (17x8 non-square)")
  def testLoadLabyrinthMaze(): Unit =
    val lines = loadMazeFromResource("labyrinth.txt")
    assertEquals(8, lines.length, "Height should be 8")
    assertEquals(17, lines(0).length, "Width should be 17")

  @Test @DisplayName("all sample mazes contain R and E markers")
  def testAllSampleMazesContainMarkers(): Unit =
    for name <- sampleMazeNames do
      val lines = loadMazeFromResource(name)
      val maze = parseMaze(lines)
      assertTrue(findChar(maze, 'R').isDefined, s"$name missing 'R'")
      assertTrue(findChar(maze, 'E').isDefined, s"$name missing 'E'")

  @Test @DisplayName("all sample mazes can be initialized and scanned")
  def testAllSampleMazesInitAndScan(): Unit =
    for name <- sampleMazeNames do
      val lines = loadMazeFromResource(name)
      val (maze, robot, energy, _) = initGame(lines)
      val revealed = makeRevealed(maze)
      val count = revealReachable(maze, revealed, robot(0), robot(1))
      assertTrue(count > 0, s"$name scanner revealed 0 cells")

  @Test @DisplayName("playMoves executes sequence and returns remaining energy")
  def testPlayMoves(): Unit =
    val lines = loadMazeFromResource("tiny.txt")
    val (maze, robot, _, _) = initGame(lines, 10)
    val remaining = playMoves(maze, robot, "DDRR", 10)
    assertTrue(remaining >= 0)

  @Test @DisplayName("loadMazeFromFile reads lines from local file")
  def testLoadMazeFromFile(): Unit =
    val tempFile = java.io.File.createTempFile("test_maze", ".txt")
    tempFile.deleteOnExit()
    val writer = java.io.PrintWriter(tempFile)
    writer.println("#####")
    writer.println("#R.E#")
    writer.println("#####")
    writer.close()
    val lines = loadMazeFromFile(tempFile.getAbsolutePath.nn)
    assertEquals(3, lines.length)
    assertEquals(5, lines(0).length)

  @Test @DisplayName("helpMessage contains descriptions of all commands including H")
  def testHelpMessage(): Unit =
    val help = helpMessage()
    assertTrue(help.contains("H (help)"))
    assertTrue(help.contains("U (up)"))
    assertTrue(help.contains("D (down)"))
    assertTrue(help.contains("L (left)"))
    assertTrue(help.contains("R (right)"))
    assertTrue(help.contains("S (scan)"))
    assertTrue(help.contains("Q (quit)"))
    // verify printHelp executes without error
    printHelp()

// ═══════════════════════════════════════════════════════════════════════════════
// Student Tests — Test-Driven Development & Edge Cases
// ═══════════════════════════════════════════════════════════════════════════════

class StudentMazeSolverTest:

  @Test @DisplayName("student test for completely trapped robot")
  def testTrappedRobot(): Unit =
    val lines = Array(
      "###",
      "#R#",
      "###"
    )
    val (maze, robot, energy, cells) = initGame(lines, 10)
    assertFalse(moveRobot(maze, robot, 'U'))
    assertFalse(moveRobot(maze, robot, 'D'))
    assertFalse(moveRobot(maze, robot, 'L'))
    assertFalse(moveRobot(maze, robot, 'R'))
    assertEquals(1, robot(0))
    assertEquals(1, robot(1))

    val (remEnergy, collected, escaped) = playGame(maze, robot, "UDLR", 10)
    assertEquals(10, remEnergy)
    assertEquals(0, collected)
    assertFalse(escaped)

  @Test @DisplayName("student test for energy exhaustion mid-journey")
  def testEnergyExhaustion(): Unit =
    val lines = Array(
      "#######",
      "#R....E#",
      "#######"
    )
    val (maze, robot, _, _) = initGame(lines, 2)
    val (remainingEnergy, cellsCollected, reachedExit) = playGame(maze, robot, "RRRRR", 2)
    assertEquals(0, remainingEnergy)
    assertEquals(0, cellsCollected)
    assertFalse(reachedExit)
    assertEquals(1, robot(0))
    assertEquals(3, robot(1))

  @Test @DisplayName("student test for scanner on cyclic corridors")
  def testScannerCyclicCorridor(): Unit =
    val lines = Array(
      "#####",
      "#...#",
      "#.#.#",
      "#...#",
      "#####"
    )
    val maze = parseMaze(lines)
    val revealed = makeRevealed(maze)
    val count = revealReachable(maze, revealed, 1, 1)
    assertEquals(8, count)
    for r <- 1 to 3; c <- 1 to 3 do
      if r != 2 || c != 2 then
        assertTrue(revealed(r)(c))
      else
        assertFalse(revealed(r)(c))

  @Test @DisplayName("student test for multiple energy cells on route")
  def testMultipleEnergyCells(): Unit =
    val lines = Array(
      "#######",
      "#R.C.CE#",
      "#######"
    )
    val (maze, robot, _, _) = initGame(lines, 5)
    val (remainingEnergy, cellsCollected, reachedExit) = playGame(maze, robot, "RRRRRR", 5)
    assertTrue(reachedExit)
    assertEquals(2, cellsCollected)
    assertEquals(5 - 5 + (2 * 3), remainingEnergy)
