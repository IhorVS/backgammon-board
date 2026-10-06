# Backgammon Board

Java 21 accessories for backgammon games: immutable checker definitions, stack cells,
mutable boards, immutable state snapshots, a reusable board builder, and a plain-text renderer.

The library stores and moves checkers. A game engine defines legal moves, initial arrangements,
movement directions, dice handling, and victory conditions.

## Requirements and build

- JDK 21.
- Maven.
- Apache Commons Lang for validation and Lombok for generated members.
- JUnit 5 and Mockito for tests.

Build and install the library locally:

```shell
mvn clean install
```

The Maven configuration also attaches a source JAR.
After installation, another project can use:

```xml
<dependency>
    <groupId>ivs.games.accessories</groupId>
    <artifactId>backgammon-board</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Packages

| Package under `ivs.games.accessories.backgammonboard` | Contents                                                                    |
|-------------------------------------------------------|-----------------------------------------------------------------------------|
| Root                                                  | Checker, color, and type contracts; cell and board interfaces; `BoardState` |
| `standard`                                            | Standard enums, cells, `StandardBoard`, and `StandardBoardBuilder`          |
| `render`                                              | `StandardBoardRenderer` for console and log output                          |

## Standard checkers

| Constant     | Code | Type         | Color         |
|--------------|------|--------------|---------------|
| `WHITE_MAN`  | `WM` | `MAN` (`M`)  | `WHITE` (`W`) |
| `WHITE_EMIR` | `WE` | `EMIR` (`E`) | `WHITE` (`W`) |
| `BLACK_MAN`  | `BM` | `MAN` (`M`)  | `BLACK` (`B`) |
| `BLACK_EMIR` | `BE` | `EMIR` (`E`) | `BLACK` (`B`) |

Each constant identifies a type and color, rather than one physical checker.
The same constant can occur repeatedly in a stack or across the board.
Checker codes are computed when enum constants are created.

`StandardChecker`, `StandardCheckerType`, and `StandardCheckerColor` provide case-sensitive
`fromCode()` lookup. Unknown codes cause `IllegalArgumentException`; null causes
`NullPointerException`. `StandardCheckerColor.getOpposite(color)` returns the other standard color.

The white and black names are server-side identifiers. A user interface may display them using
other colors. Emir movement and interaction rules belong to the game engine.
Custom immutable checkers can implement `Checker<T, C>` with their own type and color definitions.

## Board and cell access

A `StandardBoard` contains 24 playing cells (`0–23`), two bars, and two borne-off cells.
Each special cell belongs to one standard color.
Indices are stable technical addresses. The game engine maps game-specific routes to these indices.

| Interface                | Access                                                                   |
|--------------------------|--------------------------------------------------------------------------|
| `BoardCellView<P>`       | Size, emptiness, top checker and color, snapshot, color count, traversal |
| `BoardCell<P>`           | View operations plus `push`, `pop`, `setState`, and `clear`              |
| `BackgammonBoardView<P>` | Cell views, board emptiness, playing cell count, traversal, snapshot     |
| `BackgammonBoard<P>`     | Mutable cells, checker transfers, and clearing                           |

Cell contents are ordered **from bottom to top**. The last list element is the top checker.
`push()` adds to the top; `pop()` removes the top. Empty `peek()`, `pop()`, and
`getTopCheckerColor()` calls throw `IllegalArgumentException` in the standard implementation.
Cell `getState()` returns an unmodifiable snapshot. `setState()` copies supplied contents.
Cell `forEach()` visits checkers from bottom to top.

Playing cells accept any checker color or type. `ColoredCell` accepts only its assigned color.
The standard board supports `StandardCheckerColor.WHITE` and `BLACK` for its special cells;
a custom color with the same text code is not automatically equivalent to a standard enum value.

Board views and mutable boards return the **same underlying cell objects**.
A view exposes only read methods at compile time; it is not an immutable wrapper.
Changes made through mutable references remain visible through previously obtained views.

Board `forEachCell()` visits only playing cells, including empty ones, in ascending index order.
Do not modify cells during traversal callbacks. `isEmpty()` checks all board areas.
`clear()` empties every area while preserving cell references.

## Build a board

```java
import ivs.games.accessories.backgammonboard.standard.StandardBoard;
import ivs.games.accessories.backgammonboard.standard.StandardBoardBuilder;
import ivs.games.accessories.backgammonboard.standard.StandardChecker;

import java.util.List;

import static ivs.games.accessories.backgammonboard.standard.StandardChecker.*;
import static ivs.games.accessories.backgammonboard.standard.StandardCheckerColor.*;

StandardBoard<StandardChecker> board = new StandardBoardBuilder<StandardChecker>()
        .cell(0, List.of(WHITE_MAN, BLACK_EMIR))
        .cell(23, List.of(BLACK_MAN))
        .bar(WHITE, List.of(WHITE_MAN))
        .borneOff(BLACK, List.of(BLACK_MAN, BLACK_MAN))
        .build();
```

This is an arbitrary arrangement demonstrating the API, not a rules-validated game position.
Unconfigured cells remain empty. Reconfiguring a cell replaces its previous checker list.
The builder copies lists when they are supplied and can be reused: each `build()` produces
an independent board without clearing its configuration.

Null lists, elements, and special cell colors are rejected during configuration.
Playing indices, supported special cell colors, and checker color matching are validated
when `build()` initializes the new board. No fixed checker inventory limit is enforced.

## Move checkers

Using the board from the previous example:

```java
board.move(0, 23);             // Move the top BLACK_EMIR between playing cells.
board.moveToBar(23);           // Move it to the black bar, selected by its color.
board.moveFromBar(BLACK, 5);   // Move the black bar's top checker to playing cell 5.
board.moveToBorneOff(5);       // Move it to the black borne-off cell.
```

Each operation transfers one top checker. Transfers between playing cells can place a checker
on opposing checkers without applying capture or blocking rules. The game engine must validate
such moves. Transfers to special cells select the destination using the moved checker's color.

## Capture and restore state

```java
import ivs.games.accessories.backgammonboard.BackgammonBoardView;
import ivs.games.accessories.backgammonboard.BoardState;

BackgammonBoardView<StandardChecker> view = board;
BoardState<StandardChecker> state = view.getState();

StandardBoard<StandardChecker> restored = new StandardBoard<>(state);
StandardBoard<StandardChecker> copied = new StandardBoard<>(view);
```

`BoardState` copies the playing cell list, special cell maps, and all nested checker lists.
Its collections are unmodifiable; immutable checker and color objects are shared.
Subsequent board changes do not affect a previously captured state.

A state can be extended with additional immutable properties. `StandardBoard` restores only
its base cell contents and does not process subclass properties. `BoardState` itself does not
validate board dimensions or special cell restrictions. Restoration into `StandardBoard` requires
24 playing cells, exactly the two standard colors in each special cell map, and matching checker
colors in special cells.

Copied and restored boards own independent mutable cells and preserve checker order.

## Render a board

```java
import ivs.games.accessories.backgammonboard.render.StandardBoardRenderer;

String text = new StandardBoardRenderer().render(board);
System.out.println(text);
```

The renderer obtains a state snapshot and displays technical indices:

- Upper row: `12–23`, from left to right.
- Lower row: `11–0`, from left to right.
- Stack bottoms at the outer edges, stack tops towards the center.
- At least five checker rows per half; each half expands independently for larger stacks.
- One blank board row between halves; `..` marks empty positions.
- Bars and borne-off areas below the board, with complete bottom-to-top lists and checker counts.
- Counts right-aligned in a field of at least two characters; black sections aligned across both lines.

Codes must be non-empty and contain no whitespace. Column width accommodates longer playing
checker codes; alignment assumes one console column per character.
The returned text uses `\n` separators and has no trailing newline.

A runnable example is provided in
`src/test/java/ivs/games/accessories/backgammonboard/render/BoardRendererExample.java`.
Run its `main()` method from the IDE to inspect mixed stacks, special cells, and expanded board height.

## Threading

Standard boards, cells, and builders are mutable and not thread-safe.
Do not modify a board while copying it, capturing its state, or rendering it.
Immutable states can be shared when checker and color implementations, including subclass properties,
respect their immutability contracts. The renderer has no mutable instance state.

## License

See [LICENSE](LICENSE).
