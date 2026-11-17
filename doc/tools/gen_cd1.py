"""CD-01 전체 구조 (패키지 단위 개요)"""
import os
from uxf import *

OUT = os.path.join(os.path.dirname(__file__), '..')
BLUE, GREEN, ORANGE, PURPLE, YELLOW, GRAY = '#DCE8F7', '#E3F2DF', '#FCE9D6', '#F3E5F5', '#FFF8D6', '#EEEEEE'


def cd01():
    d = Diagram()

    def block(title, names, color, x, y, cols):
        boxes = []
        for i, n in enumerate(names):
            b = Box('*%s*' % n, w=150, h=30)
            b.at(x + 18 + (i % cols) * 164, y + 34 + (i // cols) * 42)
            b.draw(d)
            boxes.append(b)
        return Rect(*package(d, title, boxes, color))

    ui_names = ['App', 'Screen', 'MainMenuScreen', 'LevelSelectScreen', 'PlayScreen', 'MenuList',
                'Camera', 'IsoRenderer', 'KeyNormalizer', 'Gfx', 'Theme']
    ui = block('sokoban.ui  (Swing)', ui_names, BLUE, 0, 0, 6)
    main = block('sokoban', ['Main'], GRAY, ui.r + 90, 0, 1)

    rowb = ui.b + 130
    core = block('sokoban.core', ['Direction', 'TileType', 'Level', 'GameState', 'Engine', 'StepResult', 'Session', 'Scoring'],
                 GREEN, 0, rowb, 2)
    level = block('sokoban.level', ['LevelParser', 'LevelRepository', 'Difficulty', 'LevelFormatException'],
                  ORANGE, core.r + 110, rowb, 2)
    config = block('sokoban.config', ['GameConfig'], YELLOW, level.r + 110, rowb, 1)
    save = block('sokoban.save', ['SaveStore'], GRAY, config.r + 80, rowb, 1)

    rowc = core.b + 150
    solver = block('sokoban.solver', ['Solver'], PURPLE, 0, rowc, 1)
    tool = block('sokoban.tool', ['LevelTool'], PURPLE, level.x, rowc, 1)

    d.element('UMLNote', save.x, rowc, 300, 14 + 5 * 16,
              'CD-01 전체 구조 (패키지 단위)\n점선 화살표 = 의존(import)\ncore · level · solver 는 Swing 에\n의존하지 않는다 (NFR-1)\n상세: CD-02 ~ CD-05\nbg=#FFFFE0'.replace('\nbg=#FFFFE0', '') + '\nbg=#FFFFE0')

    # ui → 하위 패키지
    rel(d, ui, core, 'dep', '<<use>>', sa=('b', 0.06), sb=('t', 0.3))
    rel(d, ui, level, 'dep', '<<use>>', sa=('b', 0.4), sb=('t', 0.5))
    rel(d, ui, config, 'dep', '<<use>>', sa=('b', 0.75), sb=('t', 0.5))
    rel(d, ui, save, 'dep', '<<use>>', sa=('b', 0.95), sb=('t', 0.5))
    rel(d, main, ui, 'dep', '<<use>>', sa=('l', 0.5), sb=('r', 0.5))
    # level / solver / tool
    rel(d, level, core, 'dep', '<<use>>', sa=('l', 0.3), sb=('r', 0.3))
    rel(d, level, config, 'dep', '<<use>>', sa=('r', 0.3), sb=('l', 0.3))
    rel(d, solver, core, 'dep', '<<use>>', sa=('t', 0.3), sb=('b', 0.3))
    rel(d, tool, solver, 'dep', '<<use>>', sa=('l', 0.5), sb=('r', 0.5))
    rel(d, tool, level, 'dep', '<<use>>', sa=('t', 0.5), sb=('b', 0.5))
    pa, pb = anchor(core, 'b', 0.9), anchor(config, 'b', 0.3)
    lane = core.b + 60
    rel(d, core, config, 'dep', '<<use>>', sa=('b', 0.9), sb=('b', 0.3), via=[(pa[0], lane), (pb[0], lane)])
    pa2 = anchor(tool, 'r', 0.5)
    rel(d, tool, config, 'dep', '<<use>>', sa=('r', 0.5), sb=('b', 0.75))
    d.save(os.path.join(OUT, 'CD-01_전체구조.uxf'))
