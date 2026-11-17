"""CD-03 / CD-04 / CD-05 (gen_cd.py 에서 불러 쓴다)"""
import os
from uxf import *

OUT = os.path.join(os.path.dirname(__file__), '..')


def legend(d, x, y, w, text):
    lines = text.count('\n') + 1
    d.element('UMLNote', x, y, w, 14 + lines * 16, text + '\nbg=#FFFFE0')


def stub(text, w=None, h=None):
    return Box(text, w=w, h=h, bg='#EEEEEE')


# =========================================================== CD-03 level · config
def cd03():
    d = Diagram()
    lfe = Box("""<<exception>>
*LevelFormatException*
--
+source: String
+line: int
--
+LevelFormatException(source: String, line: int, message: String)""")
    parser = Box("""<<utility>>
*LevelParser*
--
_+parse(source: String, lines: List<String>): Level_
_-intMeta(source, meta, metaLine, key, min): int_
_-ratioMeta(source, meta, metaLine, key): Double_""")
    lvstub = stub("<<entity>>\n*Level*\n(sokoban.core)")
    repo = Box("""<<persistence>>
*LevelRepository*
--
+levels: List<Level>
+difficulty: List<Double>
+errors: List<String>
--
-LevelRepository(levels, difficulty, errors)
_+load(dir: Path, cfg: GameConfig): LevelRepository_""")
    diff = Box("""<<utility>>
*Difficulty*
--
_+score(lv: Level, cfg: GameConfig): double_""")
    cfg = Box("""<<config>>
*GameConfig*
--
+kappa: int
+star3Ratio: double
+star2Ratio: double
+alpha: double
+beta: double
+gamma: double
+delta: double
+maxDeltaD: double
+gimmickWeights: Map<String, Double>
+maxChain: int
+warnings: List<String>
--
-GameConfig(p: Properties, warnings: List<String>)
_+defaults(): GameConfig_
_+load(file: Path): GameConfig_
+weightOf(gimmick: String): double
-intOf(p, key, def, min, max): int
-doubleOf(p, key, def, min, max): double""")

    GAP = 150
    lfe.at(0, 0)
    parser.at(lfe.r + GAP, 0)
    lvstub.at(parser.r + GAP, 0)
    repo.at(parser.x, parser.b + 110)
    diff.at(parser.x, repo.b + 110)
    cfg.at(0, repo.y)
    for b in (lfe, parser, lvstub, repo, diff, cfg):
        b.draw(d)
    rel(d, parser, lfe, 'dep', '<<throws>>', sa=('l', 0.5), sb=('r', 0.5))
    rel(d, parser, lvstub, 'dep', '<<create>>', sa=('r', 0.5), sb=('l', 0.5))
    rel(d, repo, parser, 'dep', '<<use>>', sa=('t', 0.5), sb=('b', 0.5))
    rel(d, repo, diff, 'dep', '<<use>>', sa=('b', 0.5), sb=('t', 0.5))
    rel(d, repo, lvstub, 'aggr', 'levels', m2='0..*', sa=('r', 0.5), sb=('b', 0.3))
    rel(d, diff, cfg, 'dep', '<<use>>', sa=('l', 0.5), sb=('r', 0.85))
    rel(d, diff, lvstub, 'dep', '<<use>>', sa=('r', 0.5), sb=('b', 0.75))
    legend(d, diff.r + 80, diff.b + 40, 420,
           'CD-03 level · config\n회색 상자 = 다른 패키지 클래스\nLevelRepository.load 는 오류 레벨을 목록에서\n제외하고 errors 에 모은다 (UC-01 1a)\nGameConfig 는 값이 틀리면 기본값 + warnings (UC-19)')
    d.save(os.path.join(OUT, 'CD-03_레벨_설정.uxf'))


# =========================================================== CD-04 solver · tool · save
def cd04():
    d = Diagram()
    parser = stub("<<utility>>\n*LevelParser*\n(sokoban.level)")
    diff = stub("<<utility>>\n*Difficulty*\n(sokoban.level)")
    cfg = stub("<<config>>\n*GameConfig*\n(sokoban.config)")
    tool = Box("""<<tool>>
*LevelTool*
--
_+main(args: String[])_
_~rewrite(lines: List<String>, moves: int, path: String): List<String>_""")
    solver = Box("""<<utility>>
*Solver*
--
_+solve(lv: Level, maxStates: int, timeLimitMillis: long): Result_
_-pathOf(n: Node): String_""")
    status = Box("""<<enumeration>>
*Solver.Status*
--
SOLVED
UNSOLVABLE
LIMIT_EXCEEDED""")
    result = Box("""<<record>>
*Solver.Result*
--
+status: Status
+moves: int
+path: String
+statesExplored: int
+millis: long""")
    node = Box("""<<private>>
*Solver.Node*
--
~state: GameState
~parent: Node
~dir: Direction
~depth: int""")
    engine = stub("<<utility>>\n*Engine*\n(sokoban.core)")
    lv = stub("<<entity>>\n*Level*\n(sokoban.core)")
    store = Box("""<<persistence>>
*SaveStore*
--
-file: Path
-records: Map<String, Record>
-loadWarning: String
--
+SaveStore(file: Path)
_+defaultFile(): Path_
+loadWarning(): String
+get(levelId: String): Record
+cleared(levelId: String): boolean
+update(levelId: String, score: int, stars: int, moves: int): boolean
+save() throws IOException
-load()""")
    rec = Box("""<<record>>
*SaveStore.Record*
--
+bestScore: int
+bestStars: int
+bestMoves: int""")

    GAP = 130
    parser.at(0, 0)
    diff.at(parser.r + 60, 0)
    cfg.at(diff.r + 60, 0)
    tool.at(0, parser.b + 120)
    tool.x = diff.x + diff.w / 2 - tool.w / 2
    solver.at(tool.x, tool.b + 110)
    engine.at(solver.x, solver.b + 110)
    node.at(solver.x + engine.w + 120, solver.b + 110)
    result.at(solver.r + GAP, solver.y)
    status.at(result.r + GAP, solver.y)
    store.at(0, node.b + 110)
    rec.at(store.r + GAP, store.y)
    for b in (parser, diff, cfg, tool, solver, status, result, node, engine, store, rec):
        b.draw(d)
    rel(d, tool, parser, 'dep', '<<use>>')
    rel(d, tool, diff, 'dep', '<<use>>')
    rel(d, tool, cfg, 'dep', '<<use>>')
    rel(d, tool, solver, 'dep', '<<use>>', sa=('b', 0.5), sb=('t', 0.5))
    rel(d, solver, result, 'dep', '<<create>>', sa=('r', 0.3), sb=('l', 0.3))
    rel(d, result, status, 'assoc', 'status', m2='1', sa=('r', 0.3), sb=('l', 0.3))
    rel(d, solver, node, 'comp', '탐색 큐', m2='0..*', sa=('b', 0.85), sb=('t', 0.5))
    rel(d, solver, engine, 'dep', '<<use>>', sa=('b', 0.2), sb=('t', 0.2))
    rel(d, store, rec, 'comp', 'records', m2='0..*', sa=('r', 0.3), sb=('l', 0.3))
    legend(d, status.x, store.y, 400,
           'CD-04 solver · tool · save\nSolver 는 Engine 을 그대로 재사용한다\n(BFS, 처음 찾은 해 = 최적해)\nSaveStore 는 ui 패키지(App, PlayScreen)가 사용')
    d.save(os.path.join(OUT, 'CD-04_솔버_도구_저장.uxf'))


# =========================================================== CD-05 ui
def cd05():
    d = Diagram()
    app = Box("""<<controller>>
*App*
--
+config: GameConfig
+repo: LevelRepository
+save: SaveStore
~frame: JFrame
-canvas: Canvas
-screen: Screen
-notice: String
-noticeUntil: long
--
+App(home: Path)
+start()
~showMainMenu()
~showLevelSelect(focus: int)
~startLevel(index: int)
~isUnlocked(index: int): boolean
~firstOpenLevel(): int
~notify(msg: String)""")
    screen = Box("""<<interface>>
*Screen*
--
paint(g: Graphics2D, w: int, h: int)
tick(dtMillis: long)
keyPressed(e: KeyEvent)
mouseMoved(x: int, y: int)
mouseClicked(x: int, y: int)
mousePressed(x: int, y: int)
mouseDragged(x: int, y: int)
mouseReleased(x: int, y: int)""")
    mm = Box("""<<UI>>
*MainMenuScreen*
--
-app: App
-menu: MenuList
--
+paint(g, w, h)
+keyPressed(e: KeyEvent)
+mouseMoved(x, y)
+mouseClicked(x, y)
-activate(i: int)""")
    ls = Box("""<<UI>>
*LevelSelectScreen*
--
-app: App
-levels: List<Level>
-selected: int
-cols: int
-scrollRows: int
-boxes: Rectangle[]
-status: String
--
+paint(g, w, h)
+keyPressed(e: KeyEvent)
+mouseMoved(x, y)
+mouseClicked(x, y)
-card(g, i, x, y)
-open(i: int)""")
    ps = Box("""<<UI>>
*PlayScreen*
--
-app: App
-levelIndex: int
-level: Level
-session: Session
-renderer: IsoRenderer
-cam: Camera
-layerFocus: boolean
-mode: Mode
-animStep: StepResult
-animElapsed: long
-pending: Direction
-pauseMenu: MenuList
-clearMenu: MenuList
-hasNext: boolean
-resScore: int
-resStars: int
-newRecord: boolean
--
+tick(dt: long)
+keyPressed(e: KeyEvent)
+mousePressed(x, y)
+mouseDragged(x, y)
+mouseReleased(x, y)
+mouseMoved(x, y)
+mouseClicked(x, y)
+paint(g, w, h)
-playKey(e: KeyEvent)
-move(d: Direction)
-finishAnim()
-reset()
-onCleared()
-pauseAction(i: int)
-clearAction(i: int)""")
    mode = Box("""<<enumeration>>
*PlayScreen.Mode*
--
PLAYING
PAUSED
CLEARED""")
    menu = Box("""<<UI>>
*MenuList*
--
-items: String[]
-selected: int
-boxes: Rectangle[]
--
+MenuList(items: String...)
~select(i: int)
~key(e: KeyEvent): int
~hover(x: int, y: int)
~click(x: int, y: int): int
~paint(g, cx, top, itemW, itemH, gap)""")
    cam = Box("""<<UI>>
*Camera*
--
+yaw: double
+targetYaw: double
+pitch: double
-dragging: boolean
--
+rotateQuarter(turns: int)
+resetView()
+beginDrag()
+drag(dxPixels: int, dyPixels: int)
+endDrag()
+update(dtMillis: long)
+isRotating(): boolean
+isDragging(): boolean
+mapKey(key: Direction): Direction
_+mapKey(key: Direction, yawDeg: double, pitchDeg: double): Direction_""")
    iso = Box("""<<UI>>
*IsoRenderer*
--
-cosPsi, sinPsi, cosP, sinP: double
-scale, ox, oy: double
-layerZ: double
--
~render(g: Graphics2D, lv: Level, st: GameState, anim: Anim,
area: Rectangle, cam: Camera, layerFocus: boolean)
-setCamera(lv: Level, yawDeg: double, pitchDeg: double)
-pt(x, y, z): double[]
-depth(x, y, z): double
-cube(), tetra(), floorGrid(), backFrame() ...""")
    anim = Box("""<<record>>
*IsoRenderer.Anim*
--
~step: StepResult
~t: double""")
    keyn = Box("""<<utility>>
*KeyNormalizer*
--
_~keyCodeOf(keyCode: int, keyChar: char, extendedKeyCode: int): int_
_~normalize(e: KeyEvent): KeyEvent_""")
    gfx = Box("""<<utility>>
*Gfx*
--
_~centered(g, s: String, cx: int, baselineY: int)_
_~right(g, s: String, rightX: int, baselineY: int)_
_~star(g, cx, cy, r, filled: boolean)_
_~stars(g, cx, cy, r, filled: int)_""")
    theme = Box("""<<utility>>
*Theme*
--
색 · 글꼴 상수 (BG_TOP, PANEL, ACCENT, TEXT ...)
--
_~font(style: int, size: float): Font_
_~shade(c: Color, f: double): Color_""")
    s_repo = stub("<<persistence>>\n*LevelRepository*\n(sokoban.level)")
    s_save = stub("<<persistence>>\n*SaveStore*\n(sokoban.save)")
    s_cfg = stub("<<config>>\n*GameConfig*\n(sokoban.config)")
    s_sess = stub("<<entity>>\n*Session*\n(sokoban.core)")

    GAP = 140
    s_cfg.at(0, 0)
    s_repo.at(0, 110)
    s_save.at(0, 220)
    app.at(s_cfg.r + GAP, 0)
    keyn.at(app.r + GAP, 0)
    gfx.at(keyn.x, keyn.b + 60)
    theme.at(keyn.x, gfx.b + 60)
    screen.at(app.x + 60, app.b + 120)
    mm.at(app.x - 40, screen.b + 130)
    ls.at(mm.r + 70, screen.b + 130)
    ps.at(ls.r + 70, screen.b + 130)
    menu.at(mm.x, mm.b + 110)
    mode.at(ps.x, ps.b + 90)
    s_sess.at(ps.r + GAP, ps.y)
    cam.at(ps.r + GAP, s_sess.b + 60)
    iso.at(ps.r + GAP, cam.b + 90)
    anim.at(iso.x, iso.b + 80)
    for b in (app, s_cfg, s_repo, s_save, keyn, gfx, theme, screen, mm, ls, ps, mode, menu, cam, iso, anim, s_sess):
        b.draw(d)

    rel(d, app, s_cfg, 'assoc', 'config', m2='1', sa=('l', 0.1), sb=('r', 0.5))
    rel(d, app, s_repo, 'assoc', 'repo', m2='1', sa=('l', 0.35), sb=('r', 0.5))
    rel(d, app, s_save, 'assoc', 'save', m2='1', sa=('l', 0.6), sb=('r', 0.5))
    rel(d, app, keyn, 'dep', '<<use>>', sa=('r', 0.1), sb=('l', 0.5))
    rel(d, app, screen, 'assoc', 'screen', m2='0..1', sa=('b', 0.2), sb=('t', 0.2))
    rel(d, mm, screen, 'real', sa=('t', 0.5), sb=('b', 0.15))
    rel(d, ls, screen, 'real', sa=('t', 0.5), sb=('b', 0.5))
    rel(d, ps, screen, 'real', sa=('t', 0.5), sb=('b', 0.85))
    rel(d, mm, menu, 'comp', 'menu', m2='1', sa=('b', 0.3), sb=('t', 0.3))
    fy = (menu.y + menu.h / 2 - ps.y) / ps.h
    rel(d, ps, menu, 'comp', 'pauseMenu, clearMenu', m2='2', sa=('l', fy), sb=('r', 0.5))
    rel(d, ps, mode, 'assoc', 'mode', m2='1', sa=('b', 0.5), sb=('t', 0.5))
    rel(d, ps, s_sess, 'assoc', 'session', m2='1', sa=('r', 0.15), sb=('l', 0.5))
    rel(d, ps, cam, 'comp', 'cam', m2='1', sa=('r', 0.45), sb=('l', 0.5))
    rel(d, ps, iso, 'comp', 'renderer', m2='1', sa=('r', 0.7), sb=('l', 0.5))
    rel(d, iso, cam, 'dep', '<<use>>', sa=('t', 0.5), sb=('b', 0.5))
    rel(d, iso, anim, 'comp', None, m2='0..1', sa=('b', 0.5), sb=('t', 0.5))
    legend(d, keyn.x, theme.b + 60, 420,
           'CD-05 ui (Swing, EDT 에서만 동작)\n회색 = 다른 패키지 클래스\nApp.showMainMenu / showLevelSelect /\nstartLevel 이 화면 객체를 만든다\n세 화면은 모두 App, Gfx, Theme 을 사용한다\nLevelSelectScreen 은 app.repo.levels 를 읽는다')
    d.save(os.path.join(OUT, 'CD-05_UI.uxf'))
