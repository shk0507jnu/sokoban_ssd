"""UMLet .uxf 생성 도우미 (UMLet_다이어그램_작업_가이드.md 기준)."""
from xml.sax.saxutils import escape

HEAD = '<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n<diagram program="umlet" version="15.1">\n<zoom_level>10</zoom_level>\n'
TAIL = '</diagram>\n'


def tw(s):
    """텍스트 폭 추정(px). 한글 등 넓은 글자는 13, 그 외 7."""
    return sum(13 if ord(c) > 0x2E80 else 7 for c in s)


class Diagram:
    def __init__(self):
        self.parts = []

    def element(self, eid, x, y, w, h, text, add='<additional_attributes/>'):
        self.parts.append(
            '<element>\n<id>%s</id>\n<coordinates>\n<x>%d</x>\n<y>%d</y>\n<w>%d</w>\n<h>%d</h>\n</coordinates>\n'
            '<panel_attributes>%s</panel_attributes>\n%s\n</element>\n'
            % (eid, round(x), round(y), round(w), round(h), escape(text), add))

    def relation(self, pts, lt, labels=(), m1=None, m2=None, extra=()):
        """pts: 절대좌표 점 목록. 요소 bbox 는 점 범위에서 20px 여유."""
        xs = [p[0] for p in pts]
        ys = [p[1] for p in pts]
        x0, y0 = min(xs) - 20, min(ys) - 20
        w = max(xs) - min(xs) + 40
        h = max(ys) - min(ys) + 40
        lines = ['lt=' + lt]
        if m1:
            lines.append('m1=' + m1)
        if m2:
            lines.append('m2=' + m2)
        lines += list(extra)
        lines += list(labels)
        add = '<additional_attributes>%s</additional_attributes>' % ';'.join(
            '%d;%d' % (round(px - x0), round(py - y0)) for px, py in pts)
        self.element('Relation', x0, y0, w, h, '\n'.join(lines), add)

    def save(self, path):
        with open(path, 'w', encoding='utf-8', newline='\n') as f:
            f.write(HEAD + ''.join(self.parts) + TAIL)


# ---------------------------------------------------------------- 클래스 박스
class Box:
    def __init__(self, text, x=0, y=0, w=None, h=None, layer=1, bg='white', minw=0):
        self.lines = text.strip('\n').split('\n')
        self.x, self.y = x, y
        self.layer, self.bg = layer, bg
        inner = [l.strip('_*') for l in self.lines]
        self.w = w if w else max(max(tw(l) for l in inner) + 28, minw)
        seps = sum(1 for l in self.lines if l.strip() == '--')
        self.h = h if h else 16 * len(self.lines) - 6 * seps + 6

    def at(self, x, y):
        self.x, self.y = x, y
        return self

    @property
    def r(self):
        return self.x + self.w

    @property
    def b(self):
        return self.y + self.h

    def draw(self, d):
        text = '\n'.join(self.lines)
        if self.layer:
            text += '\nlayer=%d' % self.layer
        text += '\nbg=' + self.bg
        d.element('UMLClass', self.x, self.y, self.w, self.h, text)


def package(d, title, boxes, color, pad=18, top=34, minw=0):
    x0 = min(b.x for b in boxes) - pad
    y0 = min(b.y for b in boxes) - top
    x1 = max(b.r for b in boxes) + pad
    y1 = max(b.b for b in boxes) + pad
    w = max(x1 - x0, minw)
    d.element('UMLPackage', x0, y0, w, y1 - y0, title + '\nbg=' + color + '\nlayer=0')
    return (x0, y0, w, y1 - y0)


class Rect:
    """패키지 같은 임의 사각형 (관계선 연결용)."""
    def __init__(self, x, y, w, h):
        self.x, self.y, self.w, self.h = x, y, w, h

    @property
    def r(self):
        return self.x + self.w

    @property
    def b(self):
        return self.y + self.h


def anchor(bx, side, f=0.5):
    if side == 'l':
        return (bx.x, bx.y + bx.h * f)
    if side == 'r':
        return (bx.x + bx.w, bx.y + bx.h * f)
    if side == 't':
        return (bx.x + bx.w * f, bx.y)
    return (bx.x + bx.w * f, bx.y + bx.h)


def auto_sides(a, b):
    """두 상자 위치로 연결 변과 좌표를 자동 결정(겹치는 구간의 중앙에 직선)."""
    ox0, ox1 = max(a.x, b.x), min(a.r, b.r)
    oy0, oy1 = max(a.y, b.y), min(a.b, b.b)
    if ox1 - ox0 > 20 and (a.b <= b.y or b.b <= a.y):
        mx = (ox0 + ox1) / 2
        if a.b <= b.y:
            return (mx, a.b), (mx, b.y)
        return (mx, a.y), (mx, b.b)
    if oy1 - oy0 > 20 and (a.r <= b.x or b.r <= a.x):
        my = (oy0 + oy1) / 2
        if a.r <= b.x:
            return (a.r, my), (b.x, my)
        return (a.x, my), (b.r, my)
    return None


def route(a, b, sa=None, sb=None, via=None):
    """a, b 박스 → 점 목록. sa/sb = (변, 비율) 지정 가능, via = 중간 경유점들."""
    if sa is None and sb is None and via is None:
        r = auto_sides(a, b)
        if r:
            return [r[0], r[1]]
        # 모서리 방향: a 의 좌/우 변 → b 의 상/하 변 (L 자)
        ax = a.r if b.x > a.r else a.x
        ay = a.y + a.h / 2
        bx = b.x + b.w / 2
        by = b.y if b.y > ay else b.b
        return [(ax, ay), (bx, ay), (bx, by)]
    pa = anchor(a, *(sa if isinstance(sa, tuple) else (sa,)))
    pb = anchor(b, *(sb if isinstance(sb, tuple) else (sb,)))
    pts = [pa] + list(via or []) + [pb]
    if via is None:
        ha = (sa[0] if isinstance(sa, tuple) else sa) in ('l', 'r')
        hb = (sb[0] if isinstance(sb, tuple) else sb) in ('l', 'r')
        if ha and hb and abs(pa[1] - pb[1]) > 1:
            mx = (pa[0] + pb[0]) / 2
            pts = [pa, (mx, pa[1]), (mx, pb[1]), pb]
        elif (not ha) and (not hb) and abs(pa[0] - pb[0]) > 1:
            my = (pa[1] + pb[1]) / 2
            pts = [pa, (pa[0], my), (pb[0], my), pb]
        elif ha and not hb:
            pts = [pa, (pb[0], pa[1]), pb]
        elif hb and not ha:
            pts = [pa, (pa[0], pb[1]), pb]
    return pts


KIND = {
    'assoc': '->',
    'plain': '-',
    'comp': '<<<<<-',      # 채운 마름모 (시작=소유자)
    'aggr': '<<<<-',       # 빈 마름모
    'dep': '.>',
    'real': '.>>',         # 구현 (점선 + 속 빈 삼각형, 끝=인터페이스)
    'gen': '->>',          # 상속
}


def rel(d, a, b, kind='assoc', label=None, m1=None, m2=None, sa=None, sb=None, via=None, extra=()):
    pts = route(a, b, sa, sb, via)
    labels = [label] if label else []
    d.relation(pts, KIND[kind], labels, m1, m2, extra)
