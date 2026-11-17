"""Class Diagram .uxf 생성: CD-01 전체 구조 / CD-02 core / CD-03 level·solver·config·save·tool / CD-04 ui"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from uxf import *

OUT = os.path.join(os.path.dirname(__file__), '..')

BLUE, GREEN, ORANGE, PURPLE, YELLOW, GRAY = '#DCE8F7', '#E3F2DF', '#FCE9D6', '#F3E5F5', '#FFF8D6', '#EEEEEE'


def legend(d, x, y, w, text):
    lines = text.count('\n') + 1
    d.element('UMLNote', x, y, w, 14 + lines * 16, text + '\nbg=#FFFFE0')


# =========================================================== CD-01 전체 구조
def cd01():
    d = Diagram()

    def names(*ns):
        return [Box('*%s*' % n if not n.startswith('<<') else n, w=150, h=30) for n in ns]

    pk = {}

    def block(title, ns, color, x, y, cols):
        bs = names(*ns)
        for i, b in enumerate(bs):
            b.at(x + (i % cols) * 170, y + (i // cols) * 44)
            b.draw(d)
        pk[title] = Rect(*package(d, title, bs, color))
        return pk[title]

    # 위: ui
    ui = block('sokoban.ui', ['App', 'Screen «interface»', 'MainMenuScreen', 'LevelSelectScreen', 'PlayScreen', 'MenuList',
                              'Camera', 'IsoRenderer', 'KeyNormalizer', 'Gfx', 'Theme'], BLUE, 40, 40, 4)
    # 중간: core / level / solver
    core = block('sokoban.core', ['Direction', 'TileType', 'Level', 'GameState', 'Engine', 'StepResult', 'Session', 'Scoring'],
                 GREEN, 40, 330, 2)
    level = block('sokoban.level', ['LevelParser', 'LevelRepository', 'Difficulty', 'LevelFormatException'], ORANGE, 470, 330, 2)
    solver = block('sokoban.solver', ['Solver'], PURPLE, 880, 330, 1)
    # 아래: config / save / tool / Main
    config = block('sokoban.config', ['GameConfig'], YELLOW, 470, 560, 1)
    save = block('sokoban.save', ['SaveStore'], GRAY, 700, 560, 1)
    tool = block('sokoban.tool', ['LevelTool'], PURPLE, 880, 560, 1)
    main = block('sokoban (기본 패키지)', ['Main'], GRAY, 700, 330, 1)
    # 위치 보정: Main 은 ui 위로
    legend(d, 1100, 40, 290, '패키지 단위 개요 (실선 아님: 점선 = 의존)\n화살표 = "사용한다"(import)\ncore · level · solver 는\nSwing 에 의존하지 않는다 (NFR-1)')

    def dep(a, b, label=None, sa=None, sb=None, via=None):
        rel(d, pk[a], pk[b], 'dep', label, sa=sa, sb=sb, via=via)

    dep('sokoban.ui', 'sokoban.core', '<<use>>', ('b', 0.15), ('t', 0.2))
    dep('sokoban.ui', 'sokoban.level', '<<use>>', ('b', 0.55), ('t', 0.3))
    dep('sokoban.ui', 'sokoban.config', '<<use>>', ('b', 0.8), ('t', 0.3), via=None)
    d.save(os.path.join(OUT, 'CD-01_전체구조.uxf'))


# =========================================================== CD-02 core
def cd02():
    d = Diagram()
    tile = Box("""<<enumeration>>
*TileType*
--
EMPTY
WALL
HOLE
GOAL""")
    direction = Box("""<<enumeration>>
*Direction*
--
UP, RIGHT, DOWN, LEFT, ABOVE, BELOW
--
+symbol: char
+dx: int
+dy: int
+dz: int
--
_+fromSymbol(c: char): Direction_
_+symbols(): String_""")
    level = Box("""<<entity>>
*Level*
--
+id: String
+name: String
+w: int
+d: int
+h: int
-tiles: TileType[]
+initial: GameState
+goals: int[]
+optimalMoves: int
+solution: String
+star3Ratio: Double
+star2Ratio: Double
+gimmicks: Map<String, Integer>
+source: String
--
+idx(x: int, y: int, z: int): int
+x(idx: int): int
+y(idx: int): int
+z(idx: int): int
+cellCount(): int
+tile(idx: int): TileType
+neighbor(idx: int, dir: Direction): int
+blockCount(): int
+holeCount(): int
+usedLayers(): int
+withSolution(optimalMoves: int, solution: String): Level""")
    gs = Box("""<<entity>>
*GameState*
--
+player: int
-blocks: int[]
-holes: int[]
-hash: int
--
_+of(player: int, blocks: int[], holes: int[]): GameState_
+blockCount(): int
+holeCount(): int
+block(i: int): int
+hole(i: int): int
+hasBlock(idx: int): boolean
+isHole(idx: int): boolean
+withPlayer(newPlayer: int): GameState
+withPush(newPlayer: int, from: int, to: int): GameState
+withFill(newPlayer: int, from: int, hole: int): GameState""")
    engine = Box("""<<utility>>
*Engine*
--
_+step(lv: Level, s: GameState, dir: Direction): StepResult_
_+isCleared(lv: Level, s: GameState): boolean_
_-walkable(lv: Level, s: GameState, idx: int): boolean_""")
    step = Box("""<<record>>
*StepResult*
--
+state: GameState
+dir: Direction
+playerFrom: int
+playerTo: int
+blockFrom: int
+blockTo: int
+blockVanished: boolean
+cleared: boolean
--
+pushed(): boolean""")
    session = Box("""<<entity>>
*Session*
--
-level: Level
-config: GameConfig
-state: GameState
-undoStack: Deque<GameState>
-redoStack: Deque<GameState>
-undoCount: int
-cleared: boolean
--
+Session(level: Level, config: GameConfig)
+level(): Level
+state(): GameState
+moves(): int
+undoCount(): int
+isCleared(): boolean
+canUndo(): boolean
+canRedo(): boolean
+move(dir: Direction): StepResult
+undo(): boolean
+redo(): boolean
+reset()
+score(): int
+stars(): int""")
    scoring = Box("""<<utility>>
*Scoring*
--
_+score(optimal: int, moves: int, undoCount: int, kappa: int): int_
_+stars(optimal: int, moves: int, star3Ratio: double, star2Ratio: double): int_
_+limit(optimal: int, ratio: double): int_""")
    cfg = Box("""<<config>>
*GameConfig*
--
(sokoban.config, CD-03 참조)""")

    # ----- 배치 -----
    GAP = 150
    tile.at(0, 0)
    level.at(0, tile.b + 90)
    gs.at(level.r + GAP, level.y)
    step.at(gs.r + GAP, level.y)
    row3 = level.b + 130
    session.at(0, row3)
    engine.at(step.x, row3)
    direction.at(engine.r + GAP, row3)
    scoring.at(0, session.b + 110)
    cfg.at(scoring.r + 80, scoring.y)
    for b in (tile, direction, level, gs, engine, step, session, scoring, cfg):
        b.draw(d)

    # ----- 관계 -----
    rel(d, level, tile, 'assoc', 'tiles', m2='1..*', sa=('t', 0.15), sb=('b', 0.5))
    rel(d, level, gs, 'comp', 'initial', m2='1', sa=('r', 0.2), sb=('l', 0.2))
    rel(d, step, gs, 'assoc', 'state', m2='1', sa=('l', 0.2), sb=('r', 0.2))
    rel(d, engine, step, 'dep', '<<create>>', sa=('t', 0.5), sb=('b', 0.5))
    rel(d, engine, gs, 'dep', '<<use>>', sa=('t', 0.1), sb=('b', 0.9))
    rel(d, engine, direction, 'dep', '<<use>>', sa=('r', 0.3), sb=('l', 0.3))
    rel(d, session, level, 'assoc', 'level', m2='1', sa=('t', 0.2), sb=('b', 0.2))
    lane = level.b + 60
    pa_, pb_ = anchor(session, 't', 0.9), anchor(gs, 'b', 0.1)
    rel(d, session, gs, 'aggr', 'state, undoStack, redoStack', m2='0..*', sa=('t', 0.9), sb=('b', 0.1),
        via=[(pa_[0], lane), (pb_[0], lane)])
    rel(d, session, engine, 'dep', '<<use>>', sa=('r', 0.3), sb=('l', 0.3))
    rel(d, session, scoring, 'dep', '<<use>>', sa=('b', 0.2), sb=('t', 0.2))
    rel(d, session, cfg, 'assoc', 'config', m2='1', sa=('b', 0.9), sb=('t', 0.5))
    legend(d, step.x, 0, 300, 'CD-02 core (게임 규칙)\n밑줄 = static 멤버\nGameState 는 불변 객체: 되돌리기 스냅샷과\n솔버가 공유한다 (NFR-3)\nEngine 은 Level, GameState, Direction 을\n인자로 받는다')
    d.save(os.path.join(OUT, 'CD-02_core.uxf'))


def stub(text, w=None, h=None):
    """다른 패키지의 클래스를 이름만 보여 주는 회색 상자."""
    return Box(text, w=w, h=h, bg='#EEEEEE')


from gen_cd1 import cd01  # noqa: E402,F811
from gen_cd2 import cd03, cd04, cd05  # noqa: E402

if __name__ == '__main__':
    which = sys.argv[1:] or ['01', '02', '03', '04', '05']
    for w in which:
        globals()['cd' + w]()
