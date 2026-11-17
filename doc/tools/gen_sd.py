"""Sequence Diagram .uxf 생성 (구현된 UC 의 기본 코스). 메서드명은 실제 소스 코드 기준."""
import os, sys
sys.path.insert(0, os.path.dirname(__file__))
from uxf import *

OUT = os.path.join(os.path.dirname(__file__), '..')

# 라이프라인 정의 도우미: (id, 표시 텍스트, 플래그)
ACTOR = lambda id_, name: (id_, name, 'ACTOR EXECUTION')


def obj(id_, st, name, inst=True, later=False):
    """st = 스테레오타입, inst = True 면 인스턴스(밑줄 + ':')."""
    label = '%s\\n%s' % (('<<%s>>' % st), ('_:%s_' % name if inst else name))
    return (id_, label, 'CREATED_LATER' if later else '')


def seq(fname, title, desc, lifelines, msgs, note=None):
    """msgs: (src, dst, 텍스트) 또는 (src, dst, 텍스트, 번호문자열)."""
    n = len(lifelines)
    idx = {l[0]: i for i, l in enumerate(lifelines)}
    # 간격: 가장 긴 라벨이 들어가도록
    spacing = 200
    for m in msgs:
        s, t, txt = m[0], m[1], m[2]
        span = abs(idx[s] - idx[t])
        if span:
            spacing = max(spacing, (tw('00: ' + txt) * 0.62 + 20) / span)
    selfs = sum(1 for m in msgs if m[0] == m[1])
    w = int(spacing * n + 60)
    h = int(250 + 46 * len(msgs) + 22 * selfs + 14 * desc.count('\\n'))
    lines = ['title=' + title, 'desc=' + desc]
    for id_, label, flags in lifelines:
        lines.append(('obj=%s~%s %s' % (label, id_, flags)).rstrip())
    lines.append('')
    k = 0
    for m in msgs:
        k += 1
        s, t, txt = m[0], m[1], m[2]
        no = m[3] if len(m) > 3 else str(k)
        if s == t:
            lines.append('%s->>>%s + : %s: %s' % (s, s, no, txt))
            lines.append('tick=')
        else:
            lines.append('%s->>>%s : %s: %s' % (s, t, no, txt))
    d = Diagram()
    d.element('UMLSequenceAllInOne', 0, 0, w, h, '\n'.join(lines))
    if note:
        d.element('UMLNote', 0, h + 10, max(w, 700), 20 + 18 * (note.count('\n') + 1), note + '\nbg=#FFFFE0')
    d.save(os.path.join(OUT, fname))
    return fname


def main():
    U = ACTOR('u', '사용자')
    # ------------------------------------------------------------ SD-01 UC-01
    seq('SD-01_UC-01_기본흐름.uxf',
        'sd UC-01 Basic Course - 게임 시작 및 기록 불러오기',
        '시스템이 설정, 레벨 목록, 저장 기록을 읽고 메인 메뉴를 표시한다.\\nAlternate: 1a 레벨 파일 오류 - repo.errors 에 모으고 해당 레벨은 목록에서 제외 / 2a 저장 파일 없음·손상 - 빈 기록으로 시작',
        [U,
         obj('mn', 'controller', 'Main', inst=False),
         obj('app', 'controller', 'App', later=True),
         obj('gc', 'config', 'GameConfig', inst=False),
         obj('lr', 'persistence', 'LevelRepository', inst=False),
         obj('lp', 'utility', 'LevelParser', inst=False),
         obj('df', 'utility', 'Difficulty', inst=False),
         obj('ss', 'persistence', 'SaveStore', later=True),
         obj('mm', 'UI', 'MainMenuScreen', later=True)],
        [('u', 'mn', '1: 앱 실행'.split(': ', 1)[1] + ' - main(args)'),
         ('mn', 'app', '<<create>> App(home)'),
         ('app', 'gc', 'config = load(home/config/game.properties)'),
         ('app', 'lr', 'repo = load(home/levels, config)'),
         ('lr', 'lp', '*[레벨 파일마다] lv = parse(name, lines)'),
         ('lr', 'df', '*[레벨마다] score(lv, cfg)'),
         ('app', 'ss', '<<create>> SaveStore(defaultFile())'),
         ('mn', 'app', 'start()'),
         ('app', 'app', 'showMainMenu()'),
         ('app', 'mm', '<<create>> MainMenuScreen(app)')])

    # ------------------------------------------------------------ SD-02 UC-02
    seq('SD-02_UC-02_기본흐름.uxf',
        'sd UC-02 Basic Course - 레벨 선택',
        '메인 메뉴에서 "시작"을 고르면 레벨 목록이 뜨고, 해금된 레벨을 고르면 플레이 화면이 열린다.\\nAlternate: 2a 잠긴 레벨 선택 - isUnlocked(i) == false 이면 안내 문구(status)만 표시하고 진입하지 않는다',
        [U,
         obj('mm', 'UI', 'MainMenuScreen'),
         obj('app', 'controller', 'App'),
         obj('ss', 'persistence', 'SaveStore'),
         obj('ls', 'UI', 'LevelSelectScreen', later=True),
         obj('ps', 'UI', 'PlayScreen', later=True),
         obj('se', 'entity', 'Session', later=True)],
        [('u', 'mm', '"시작" 선택 (Enter / 클릭)'),
         ('mm', 'app', 'focus = firstOpenLevel()'),
         ('app', 'ss', 'cleared(levelId)'),
         ('mm', 'app', 'showLevelSelect(focus)'),
         ('app', 'ls', '<<create>> LevelSelectScreen(app, focus)'),
         ('u', 'ls', '해금된 레벨 선택 (Enter / 클릭)'),
         ('ls', 'app', 'ok = isUnlocked(i)'),
         ('app', 'ss', 'cleared(prevLevelId)'),
         ('ls', 'app', '[ok] startLevel(i)'),
         ('app', 'ps', '<<create>> PlayScreen(app, i)'),
         ('ps', 'se', '<<create>> Session(level, config)')])

    mv_lifelines = [U,
                    obj('ps', 'UI', 'PlayScreen'),
                    obj('cam', 'UI', 'Camera'),
                    obj('se', 'entity', 'Session'),
                    obj('en', 'utility', 'Engine', inst=False),
                    obj('lv', 'entity', 'Level'),
                    obj('gs', 'entity', 'GameState')]
    # ------------------------------------------------------------ SD-03 UC-03
    seq('SD-03_UC-03_기본흐름.uxf',
        'sd UC-03 Basic Course - 평면 이동 (3D 6방향 이동)',
        '플레이어가 방향키로 빈 칸으로 움직인다.\\nAlternate: 3a 목적지에 블록 - SD-04 / 3d 벽·구멍·격자 밖 - step() 이 null, 상태와 이동 횟수 불변',
        mv_lifelines,
        [('u', 'ps', '방향키 입력: keyPressed(e)'),
         ('ps', 'cam', 'd = mapKey(key)'),
         ('ps', 'se', 'r = move(d)'),
         ('se', 'en', 'r = step(level, state, d)'),
         ('en', 'lv', 'to = neighbor(from, dir)'),
         ('en', 'gs', '[목적지가 빈 칸] next = withPlayer(to)'),
         ('en', 'en', 'isCleared(lv, next)'),
         ('se', 'se', 'undoStack.push(state), state = r.state()'),
         ('ps', 'ps', 'animStep = r (이동 애니메이션)')],
        note='UC-03 의 "낙하(UC-09)" 단계는 중력이 없는 3D 규칙이라 구현하지 않았다.')

    # ------------------------------------------------------------ SD-04 UC-04
    seq('SD-04_UC-04_기본흐름.uxf',
        'sd UC-04 Basic Course - 블록 밀기',
        '플레이어가 블록 쪽으로 입력하면 블록과 플레이어가 한 칸씩 움직인다. 구멍이면 블록과 구멍이 함께 사라진다.\\nAlternate: 2a 뒤 칸이 벽·블록·격자 밖 - step() 이 null, 상태와 이동 횟수 불변',
        mv_lifelines,
        [('u', 'ps', '블록 방향 입력: keyPressed(e)'),
         ('ps', 'cam', 'd = mapKey(key)'),
         ('ps', 'se', 'r = move(d)'),
         ('se', 'en', 'r = step(level, state, d)'),
         ('en', 'lv', 'to = neighbor(from, dir)'),
         ('en', 'gs', 'hasBlock(to)'),
         ('en', 'lv', 'behind = neighbor(to, dir)'),
         ('en', 'gs', 'hasBlock(behind)'),
         ('en', 'lv', 't = tile(behind)'),
         ('en', 'gs', '[t == HOLE] next = withFill(to, to, behind)'),
         ('en', 'gs', '[그 외] next = withPush(to, to, behind)'),
         ('en', 'en', 'isCleared(lv, next)'),
         ('se', 'se', 'undoStack.push(state), state = r.state()'),
         ('ps', 'ps', 'animStep = r (블록 이동 / 소멸 애니메이션)')])

    # ------------------------------------------------------------ SD-05 UC-10
    seq('SD-05_UC-10_기본흐름.uxf',
        'sd UC-10 Basic Course - 되돌리기',
        '직전 입력 이전 상태로 돌아가고, 이동 횟수 -1, 되돌리기 횟수 +1.\\nAlternate: 1a 기록이 없거나 클리어 후 - undo() 가 false, 변화 없음',
        [U, obj('ps', 'UI', 'PlayScreen'), obj('se', 'entity', 'Session')],
        [('u', 'ps', 'Z 입력: keyPressed(e)'),
         ('ps', 'ps', 'finishAnim()'),
         ('ps', 'se', 'ok = undo()'),
         ('se', 'se', 'redoStack.push(state), state = undoStack.pop(), undoCount++'),
         ('ps', 'se', 'HUD 갱신: moves(), undoCount()')])

    # ------------------------------------------------------------ SD-06 UC-11
    seq('SD-06_UC-11_기본흐름.uxf',
        'sd UC-11 Basic Course - 다시 하기',
        '되돌렸던 입력을 다시 적용한다.\\nAlternate: 1a redo 기록이 없음 - redo() 가 false / 새 유효 이동 move() 가 일어나면 redoStack.clear()',
        [U, obj('ps', 'UI', 'PlayScreen'), obj('se', 'entity', 'Session')],
        [('u', 'ps', 'Y 입력: keyPressed(e)'),
         ('ps', 'ps', 'finishAnim()'),
         ('ps', 'se', 'ok = redo()'),
         ('se', 'se', 'undoStack.push(state), state = redoStack.pop()'),
         ('ps', 'se', 'HUD 갱신: moves()')])

    # ------------------------------------------------------------ SD-07 UC-12
    seq('SD-07_UC-12_기본흐름.uxf',
        'sd UC-12 Basic Course - 레벨 리셋',
        '이동 횟수가 0이 아니면 확인 대화상자를 띄우고, 확인하면 초기 상태로 되돌린다.\\nAlternate: 2a 취소 - 상태 유지 / 일시정지 메뉴의 "다시 시작"도 같은 reset() 을 쓴다',
        [U, obj('ps', 'UI', 'PlayScreen'), obj('se', 'entity', 'Session'),
         obj('jo', 'UI', 'JOptionPane', inst=False)],
        [('u', 'ps', 'R 입력: keyPressed(e)'),
         ('ps', 'ps', 'finishAnim()'),
         ('ps', 'se', 'n = moves()'),
         ('ps', 'jo', '[n != 0] r = showConfirmDialog(frame, 메시지, "리셋", ...)'),
         ('u', 'jo', '확인 선택'),
         ('ps', 'se', '[확인 또는 n == 0] reset()')])

    # ------------------------------------------------------------ SD-08 UC-13
    seq('SD-08_UC-13_기본흐름.uxf',
        'sd UC-13 Basic Course - 일시정지 및 메뉴 조작',
        'Esc 로 일시정지 메뉴(계속, 다시 시작, 레벨 선택, 메인 메뉴)를 열고 항목을 고른다.\\nAlternate: i == 0 계속 - mode = PLAYING / i == 1 다시 시작 - SD-07',
        [U, obj('ps', 'UI', 'PlayScreen'), obj('ml', 'UI', 'MenuList (pauseMenu)'),
         obj('app', 'controller', 'App')],
        [('u', 'ps', 'Esc 입력: keyPressed(e)'),
         ('ps', 'ps', 'finishAnim(), mode = PAUSED'),
         ('u', 'ps', '방향키 / Enter 입력: keyPressed(e)'),
         ('ps', 'ml', 'i = key(e)'),
         ('ps', 'ps', 'pauseAction(i)'),
         ('ps', 'app', '[i == 2] showLevelSelect(levelIndex)'),
         ('ps', 'app', '[i == 3] showMainMenu()')])

    # ------------------------------------------------------------ SD-09 UC-14 (+UC-15)
    seq('SD-09_UC-14_기본흐름.uxf',
        'sd UC-14 Basic Course - 레벨 클리어 및 결과 확인 (UC-15 기록 저장 포함)',
        '마지막 이동 애니메이션이 끝나면 점수·별을 계산하고 기록을 갱신·저장한 뒤 결과 메뉴를 표시한다.\\nAlternate: 2a 신기록이 아니면 기존 기록 유지 / 5a 마지막 레벨은 "다음 레벨" 없음 / UC-15 2a 저장 실패 - notify() 로 알림만',
        [U, obj('ps', 'UI', 'PlayScreen'), obj('se', 'entity', 'Session'),
         obj('sc', 'utility', 'Scoring', inst=False),
         obj('ss', 'persistence', 'SaveStore'), obj('app', 'controller', 'App'),
         obj('ml', 'UI', 'MenuList (clearMenu)', later=True)],
        [('ps', 'se', 'tick(dt): 애니메이션 종료 후 isCleared()'),
         ('ps', 'ps', 'onCleared()'),
         ('ps', 'se', 'resScore = score()'),
         ('se', 'sc', 'score(optimal, moves, undoCount, kappa)'),
         ('ps', 'se', 'resStars = stars()'),
         ('se', 'sc', 'stars(optimal, moves, r3, r2)'),
         ('ps', 'ss', 'newRecord = update(level.id, resScore, resStars, moves)'),
         ('ps', 'ss', '[newRecord] save()  (UC-15)'),
         ('ps', 'app', '[IOException] notify(msg)'),
         ('ps', 'ml', '<<create>> MenuList("다음 레벨", "다시 도전", "레벨 선택")'),
         ('u', 'ps', '항목 선택: keyPressed(e)'),
         ('ps', 'app', 'clearAction(i): startLevel(levelIndex + 1)')])

    # ------------------------------------------------------------ SD-10 UC-16
    seq('SD-10_UC-16_기본흐름.uxf',
        'sd UC-16 Basic Course - 층 보기 전환',
        'Tab 으로 현재 층을 강조(위층을 흐리게)하거나 일반 보기로 복귀한다.',
        [U, obj('ps', 'UI', 'PlayScreen'), obj('ir', 'UI', 'IsoRenderer')],
        [('u', 'ps', 'Tab 입력: keyPressed(e)'),
         ('ps', 'ps', 'layerFocus = !layerFocus'),
         ('ps', 'ir', 'render(g, level, state, anim, area, cam, layerFocus)'),
         ('ir', 'ir', '[layerFocus] layerZ = z(player) + 0.5 (위층을 흐리게)')])

    # ------------------------------------------------------------ SD-11 UC-17
    D = ACTOR('d', '레벨 디자이너')
    seq('SD-11_UC-17_기본흐름.uxf',
        'sd UC-17 Basic Course - 레벨 검증 및 최적해 산출',
        'LevelTool 이 레벨 파일을 검사하고 BFS 솔버로 풀어 optimalMoves 와 풀이 경로를 파일에 기록한다.\\nAlternate: 2a 형식 오류 - "[형식 오류]" 출력 / 3a UNSOLVABLE - "[풀이 불가]" / 3b LIMIT_EXCEEDED - "[검증 불가]" (모두 출고 불가)',
        [D,
         obj('lt', 'tool', 'LevelTool', inst=False),
         obj('gc', 'config', 'GameConfig', inst=False),
         obj('lp', 'utility', 'LevelParser', inst=False),
         obj('sv', 'utility', 'Solver', inst=False),
         obj('en', 'utility', 'Engine', inst=False),
         obj('lv', 'entity', 'Level'),
         obj('df', 'utility', 'Difficulty', inst=False)],
        [('d', 'lt', '검증 실행: main(args)'),
         ('lt', 'gc', 'cfg = load(config/game.properties)'),
         ('lt', 'lp', 'lv = parse(name, lines)'),
         ('lt', 'sv', 'r = solve(lv, maxStates, timeLimit)'),
         ('sv', 'en', '*[BFS 탐색] r = step(lv, state, dir)'),
         ('lt', 'lv', 'solved = withSolution(r.moves(), r.path())'),
         ('lt', 'df', 'dscore = score(solved, cfg)  (UC-18)'),
         ('lt', 'lt', 'rewrite(lines, moves, path)')])

    # ------------------------------------------------------------ SD-12 UC-18
    seq('SD-12_UC-18_기본흐름.uxf',
        'sd UC-18 Basic Course - 난이도 점수 산출 및 곡선 검증',
        'LevelTool 이 레벨마다 난이도 D 를 계산하고, 파일 순서대로 비감소 조건과 급등 한계(maxDeltaD)를 검사한다.\\n게임 쪽에서는 LevelRepository.load 가 같은 Difficulty.score 로 레벨 목록을 D 오름차순 정렬한다.',
        [D, obj('lt', 'tool', 'LevelTool', inst=False), obj('df', 'utility', 'Difficulty', inst=False)],
        [('d', 'lt', '검증 실행: main(args)'),
         ('lt', 'df', '*[레벨마다] d = score(solved, cfg)'),
         ('lt', 'lt', '*[i = 1 .. n-1] delta = ds[i] - ds[i-1]'),
         ('lt', 'd', '[delta < 0 또는 delta > maxDeltaD] "[곡선 경고]" 출력')])

    # ------------------------------------------------------------ SD-13 UC-19
    seq('SD-13_UC-19_기본흐름.uxf',
        'sd UC-19 Basic Course - 설정 값 조정',
        '디자이너가 config/game.properties 를 고치면 다음 실행 때 읽어 적용한다.\\nAlternate: 2a 값이 범위 밖이거나 형식 오류 - 기본값으로 대체하고 warnings 에 경고를 남긴다',
        [D, obj('app', 'controller', 'App'), obj('gc', 'config', 'GameConfig', inst=False)],
        [('d', 'app', '설정 파일 수정 후 게임 또는 LevelTool 실행'),
         ('app', 'gc', 'config = load(file)'),
         ('gc', 'gc', 'intOf(p, key, def, min, max) / doubleOf(p, key, def, min, max)'),
         ('gc', 'gc', '[값 오류] warnings.add(...), 기본값 사용'),
         ('app', 'app', 'config.warnings 를 "[설정 경고]" 로 출력')])


main()
