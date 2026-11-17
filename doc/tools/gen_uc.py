"""Use Case Diagram .uxf 생성 (구현된 UC 만)"""
import math, os, sys
sys.path.insert(0, os.path.dirname(__file__))
from uxf import *

OUT = os.path.join(os.path.dirname(__file__), '..')

LEFT = ['UC-01', 'UC-02', 'UC-13', 'UC-16', 'UC-10', 'UC-11', 'UC-12']
RIGHT = ['UC-03', 'UC-04', 'UC-14', 'UC-15', 'UC-17', 'UC-18', 'UC-19']
NAMES = {
    'UC-01': '게임 시작 및 기록 불러오기', 'UC-02': '레벨 선택', 'UC-03': '평면 이동', 'UC-04': '블록 밀기',
    'UC-10': '되돌리기', 'UC-11': '다시 하기', 'UC-12': '레벨 리셋', 'UC-13': '일시정지 및 메뉴 조작',
    'UC-14': '레벨 클리어 및 결과 확인', 'UC-15': '기록 저장', 'UC-16': '층 보기 전환',
    'UC-17': '레벨 검증 및 최적해 산출', 'UC-18': '난이도 점수 산출 및 곡선 검증', 'UC-19': '설정 값 조정',
}
PLAYER = ['UC-01', 'UC-02', 'UC-03', 'UC-04', 'UC-10', 'UC-11', 'UC-12', 'UC-13', 'UC-14', 'UC-16']
SYSTEM = ['UC-01', 'UC-03', 'UC-04', 'UC-10', 'UC-11', 'UC-12', 'UC-14', 'UC-15', 'UC-17', 'UC-18']
DESIGNER = ['UC-17', 'UC-18', 'UC-19']

UW, UH, PITCH = 320, 56, 70
BX = 230                       # 시스템 경계 x
LX = BX + 40                   # 왼쪽 열 x
RX = LX + UW + 130             # 오른쪽 열 x
TOP = 50
RTOP = TOP + 7 * PITCH + 20    # 오른쪽 열은 왼쪽 열 아래에서 시작


def main():
    d = Diagram()
    pos = {}
    for i, no in enumerate(LEFT):
        pos[no] = (LX, TOP + i * PITCH)
    for i, no in enumerate(RIGHT):
        pos[no] = (RX, RTOP + i * PITCH)
    bh = RTOP + 7 * PITCH + 30
    bw = RX + UW + 60 - BX
    d.element('UMLPackage', BX, 0, bw, bh, '2.5D 소코반 (기본 게임)\nbg=white\nlayer=0')
    for no in LEFT + RIGHT:
        x, y = pos[no]
        d.element('UMLUseCase', x, y, UW, UH, '%s\n%s\nlayer=1' % (no, NAMES[no]))

    def center(no):
        x, y = pos[no]
        return (x + UW / 2, y + UH / 2)

    def edge(no, toward):
        cx, cy = center(no)
        dx, dy = toward[0] - cx, toward[1] - cy
        a, b = UW / 2, UH / 2
        t = 1 / math.sqrt((dx / a) ** 2 + (dy / b) ** 2)
        return (cx + dx * t, cy + dy * t)

    AW, AH = 70, 110
    cy0 = TOP + 3 * PITCH + UH / 2          # 왼쪽 열 가운데 높이
    player = (30, cy0 - 38)
    system = (BX + bw + 90, cy0 - 38 + PITCH // 2)
    designer = (30, RTOP + 5 * PITCH - 38)
    for name, (x, y) in (('플레이어', player), ('시스템', system), ('레벨 디자이너', designer)):
        d.element('UMLActor', x, y, AW, AH, name)

    laneL = BX + 15                         # 플레이어 버스 (경계 안쪽 왼쪽)
    laneD = BX + 15                         # 디자이너 버스 (UC-17~19 높이만 사용)
    laneG = LX + UW + 65                    # 시스템 -> 왼쪽 열 버스
    laneS = RX + UW + 55                    # 시스템 -> 오른쪽 열 버스

    def yc(no):
        return center(no)[1]

    px = (player[0] + AW, player[1] + 38)
    for no in PLAYER:
        tx = LX if no in LEFT else RX
        d.relation([px, (laneL, px[1]), (laneL, yc(no)), (tx, yc(no))], '-')
    sx = (system[0], system[1] + 38)
    for no in SYSTEM:
        if no in LEFT:
            d.relation([sx, (laneG, sx[1]), (laneG, yc(no)), (LX + UW, yc(no))], '-')
        else:
            d.relation([sx, (laneS, sx[1]), (laneS, yc(no)), (RX + UW, yc(no))], '-')
    gx = (designer[0] + AW, designer[1] + 38)
    for no in DESIGNER:
        d.relation([gx, (laneD, gx[1]), (laneD, yc(no)), (RX, yc(no))], '-')

    def vline(a, b, label, lt='.>'):
        (ax, ay), (bx, by) = center(a), center(b)
        s = 1 if by > ay else -1
        d.relation([(ax, ay + s * UH / 2), (bx, by - s * UH / 2)], lt, [label])

    vline('UC-04', 'UC-03', '<<extend>>')
    vline('UC-04', 'UC-14', '<<include>>')
    vline('UC-14', 'UC-15', '<<include>>')
    vline('UC-17', 'UC-18', '<<include>>')
    lane = RX + UW + 26
    a = center('UC-03')
    b = center('UC-14')
    d.relation([(RX + UW - 14, a[1] - 14), (lane, a[1] - 14), (lane, b[1] - 14), (RX + UW - 14, b[1] - 14)],
               '.>', ['<<include>>'])

    d.element('UMLNote', 20, bh + 20, 1000, 78,
              'Use Case Diagram (구현된 범위)\n'
              '구현: UC-01 ~ UC-04, UC-10 ~ UC-19   미구현(기믹): UC-05 연쇄 밀기, UC-06 계단, UC-07 리프트, UC-08 포탈, UC-09 중력 낙하\n'
              '시스템 = 규칙 엔진, 솔버, 저장소.  UC-03 / UC-04 는 턴 끝에서 클리어 판정(UC-14)을 포함한다.\n'
              'bg=#FFFFE0')
    d.save(os.path.join(OUT, 'UC_유스케이스_다이어그램.uxf'))


main()
