# UMLet 다이어그램 작업 가이드 (Claude 작업 지시용)

별잇기(Asteriddle) 프로젝트에서 UMLet으로 Use Case / Sequence / Class 다이어그램을 만든 방식을 정리한 문서.
다른 프로젝트에서 Claude에게 "이 문서를 참고해서 다이어그램을 만들어줘"라고 시킬 때 쓴다.
아래 내용은 이 저장소의 실제 `.uxf` 파일(`doc/SD-*.uxf`, `doc/CD-*.uxf`, `doc/usecase_diagram.uxf`)과
`doc/sequence_diagram_umlet.txt`에서 확인한 것이고, 내보내기 명령은 작성 시점에 직접 실행해 검증했다.

---

## 0. 한눈에 보기

| 항목 | 방식 |
|---|---|
| 도구 | UMLet 15.1 (`C:\Users\gksmf\Desktop\대학\Umlet\umlet.jar`, `Umlet.exe`) |
| 원본 | `.uxf` (XML). Claude가 **XML을 직접 작성**한다. UMLet GUI로 그리지 않는다 |
| 파일 단위 | 다이어그램 1개 = `.uxf` 1개 (한 파일에 여러 개 넣지 않는다) |
| 파일명 | `SD-01_UC-01_기본코스.uxf`, `CD-02_도메인모델.uxf` 처럼 `<종류>-<번호>_<내용>` |
| 위치 | `.uxf` → `doc/`, 렌더링한 `.png` → `doc/diagram/` |
| 그림 출력 | UMLet CLI로 SVG 변환 → headless Chrome으로 2배율 PNG |
| 검증 | PNG를 Claude가 직접 열어(Read) 잘림·겹침 확인 후 수정 |
| 보고서 삽입 | PNG를 Word(.doc)에 삽입 (Word COM, 별도 작업) |

작업 순서: **(1) 소스 코드/유스케이스 문서 읽기 → (2) .uxf 작성 → (3) PNG 내보내기 → (4) PNG 확인·수정 → (5) 보고서에 삽입**

---

## 1. 작업 전 준비 (Claude가 먼저 할 일)

다이어그램은 **실제 코드와 일치**해야 한다. 추측으로 그리지 않는다.

1. 유스케이스 문서에서 UC 번호, 기본 흐름 단계, 대안 흐름 번호(7a, 3b 등)를 읽는다.
2. 소스 코드에서 클래스 이름, 메서드 시그니처, 패키지 구조를 읽는다. 메서드명·인자는 코드 그대로 쓴다.
3. 코드가 바뀌면 (예: 이 프로젝트의 "정답 보기" 수정) 해당 SD/CD를 다시 확인하고 갱신한다.
4. 다이어그램 목록(번호·이름·대응 UC)을 먼저 표로 합의한 뒤 그린다.

---

## 2. .uxf 기본 구조

모든 `.uxf`는 이 틀을 따른다.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<diagram program="umlet" version="15.1">
<zoom_level>10</zoom_level>
<element>
<id>요소종류</id>
<coordinates>
<x>0</x>
<y>0</y>
<w>400</w>
<h>200</h>
</coordinates>
<panel_attributes>본문(UMLet 텍스트 문법)</panel_attributes>
<additional_attributes/>
</element>
</diagram>
```

주의:
- 파일은 **UTF-8**로 저장 (한글 사용).
- `panel_attributes` 안의 `<` `>` `&` 는 **반드시 XML 이스케이프**: `&lt;` `&gt;` `&amp;`.
  예) `<<entity>>` → `&lt;&lt;entity&gt;&gt;`, 화살표 `->>>` → `-&gt;&gt;&gt;`, `List<String>` → `List&lt;String&gt;`
- 이스케이프를 빠뜨리면 UMLet이 파일을 못 열거나 내용이 깨진다. **Python으로 생성할 때는 `xml.sax.saxutils.escape()` 사용 권장.**
- 요소 종류(`<id>`): `UMLSequenceAllInOne`, `UMLClass`, `UMLPackage`, `UMLNote`, `UMLActor`, `UMLUseCase`, `Relation`
- 개행은 `panel_attributes` 안에서 그냥 줄바꿈. 시퀀스의 `desc=` 같은 한 줄 안 개행은 `\n`.
- `coordinates`의 x, y, w, h 는 px. 요소 위치·크기를 직접 정한다.

---

## 3. Sequence Diagram

### 3.1 요소
`UMLSequenceAllInOne` **한 요소 안에** 라이프라인과 메시지를 전부 텍스트로 적는다.
(GUI 팔레트에서 "UML Sequence - All in one"과 같은 요소. "UML Activity - All in one"에 넣으면 순서도로 그려지니 주의.)

### 3.2 문법 (실제 사용 예: `SD-01_UC-01_기본코스.uxf`)

```
title=sd UC-01 Basic Course - 기본 문제 모드로 새 게임 시작
desc=사용자가 체계, 난이도를 골라 새 게임을 시작한다.\nAlternate: 5a 밤하늘은 난이도 생략 / 6a 누락 항목 안내
obj=사용자~u ACTOR EXECUTION
obj=<<controller>>\n_:MainFrame_~mf
obj=<<UI>>\n_:StartPanel_~sp
obj=<<entity>>\nBasicGame~bg
obj=<<persistence>>\n_:SaveManager_~sm

u->>>sp : 1: 새 게임 클릭
sp->>>mf : 2: showSetup()
mf->>>bg : 3: game = start(catalog, system, difficulty, random)
mf->>>mf + : 4: autoSave()
tick=
mf->>>sm : 5: save(session)
```

| 문법 | 의미 |
|---|---|
| `title=` | 프레임 이름. `sd <UC번호> <Basic/Alternate> Course [번호] - <시나리오>` |
| `desc=` | 프레임 안 설명문. 별도 다이어그램이 없는 짧은 대안 흐름은 여기에 번호와 함께 적는다 |
| `obj=<<스테레오타입>>\n이름~id` | 라이프라인. `~id`는 메시지에서 쓰는 짧은 식별자 |
| `_:이름_` | 밑줄 = **객체(인스턴스)** (`:GamePanel`). 밑줄 없음 = **클래스**(static 호출, 예: `AnswerJudge`) |
| `obj=이름~id ACTOR EXECUTION` | 액터(사람 모양). 실행 막대가 길게 표시됨 |
| `CREATED_LATER` | 객체가 중간에 생성될 때. 박스가 생성 시점 높이에 그려짐 (`obj=... ~ng CREATED_LATER`) |
| `a->>>b : 번호: 메시지` | 동기 메시지(실선 화살표). **번호는 시간 순서** |
| `a->>>a + : 번호: op()` + 다음 줄 `tick=` | 자기 호출. `+` 뒤에 `tick=`을 두면 실행 막대가 닫힌다. 자기 호출 뒤엔 항상 `tick=` |
| `x = op(args)` | 반환값을 쓰는 호출. 점선 반환 화살표를 따로 그리지 않고 메시지에 `결과 = ` 로 표기 |
| `*[조건] op()` | 반복 호출 (예: `*[후보 별자리] hintsUsed(c)`) |
| `[조건] op()` | 조건부 호출 (예: `[ok == false] 시작 불가 안내`) |
| `<<create>> (인자)` | 생성 메시지 |

### 3.3 관례 (이 프로젝트에서 고정해서 쓴 규칙)

- **한 다이어그램 = 한 시나리오.** Basic Course와 Alternate Course를 분리. 번호는 유스케이스 문서의 단계 번호를 그대로 쓴다.
- **라이프라인 순서(왼쪽 → 오른쪽):** 액터 → `<<controller>>` → `<<UI>>` → `<<entity>>`/`<<utility>>` → `<<persistence>>`
- 스테레오타입: `controller`, `UI`, `entity`, `utility`, `persistence` (프로젝트의 계층에 맞게 바꾼다).
- 메시지 번호는 `1:`, `2:` … 형식으로 **라벨 앞**에 붙인다. 반환 화살표는 쓰지 않는다 (모든 메시지는 실선 `->>>`).
- 메시지명은 코드의 실제 메서드 시그니처 (`startBasic(system, difficulty)`).
- 사용자 행위/화면 안내처럼 코드에 없는 단계는 한글 문장으로 쓴다 (`새 게임 클릭`, `시작 불가 안내`).
- 다이어그램마다 **보충 노트**(`UMLNote`)를 오른쪽에 둘 수 있다 (예: "`start()`는 해당 난이도 별자리가 없으면 null을 반환한다").
- 다이어그램 번호 ↔ UC 단계 대응표를 문서(`sequence_diagram_umlet.txt` 같은 파일 또는 보고서)에 남긴다:
  `SD-01  UC-01 기본 흐름 1~7`, `SD-02  UC-01 7a` …

### 3.4 요소 크기 잡는 법

크기는 자동 조절되지 않는다. 처음엔 넉넉히 잡고 PNG를 보고 줄인다. 실제 값 참고:

| 다이어그램 | 라이프라인(액터 포함) | 메시지 | 크기 (w x h) |
|---|---|---|---|
| SD-02 (대체 7a) | 4 | 5 | 890 x 460 |
| SD-11 (대체 5b) | 5 | 8 | 1340 x 520 |
| SD-07 (힌트) | 7 | 10 | 2260 x 640 |
| SD-01 (기본) | 8 | 13 | 1970 x 750 |

- 높이 ≈ 230 + 메시지당 40~50 (자기 호출·`tick=`은 더 먹는다).
- 너비는 라이프라인 개수 x 약 220 + **가장 긴 메시지 라벨 길이**에 좌우된다. 라벨이 길면 겹치므로 라벨을 줄이거나 너비를 늘린다.
- 노트(`UMLNote`)는 시퀀스 요소 오른쪽 바깥에 배치 (예: x = 시퀀스 너비 + 10).

---

## 4. Class Diagram

### 4.1 구성 요소
- `UMLClass` : 클래스 박스
- `UMLPackage` : 패키지 틀 (배경색으로 계층 구분)
- `UMLNote` : 범례/설명
- `Relation` : 관계선

### 4.2 클래스 박스 문법 (`CD-03_게임로직.uxf`)

```
<<entity>>
*GameSession*
--
-mode: GameMode
-basic: BasicGame
--
_+of(basic: BasicGame): GameSession_
+system(): StarSystem
+summary(): String
```

- 첫 줄 `<<스테레오타입>>` (entity / utility / persistence / controller / enumeration / record / interface)
- `*이름*` = 굵게 (대표 클래스 강조), `_텍스트_` = 밑줄 → **static 멤버**
- `--` 는 구획선: **이름 / 필드 / 메서드** 3구획
- 가시성 `+` `-`, 타입은 `이름: 타입` 형식. 제네릭의 `<`,`>`는 이스케이프(`List&lt;String&gt;`)
- 긴 시그니처는 줄을 나눠 `_` 로 계속 표시해도 된다 (실제로 `restore(...)`는 두 줄로 씀)
- **높이는 줄 수에 맞춰 직접 계산**: 대략 `h = 30 + 줄 수 x 14~15`. 부족하면 마지막 줄이 잘린다 → PNG에서 확인.
- 메서드가 많으면 **공개 API 위주로만** 싣는다 (private 도우미는 필요한 것만).

### 4.3 패키지와 계층

- `UMLPackage`의 `panel_attributes`: 첫 줄에 이름, `bg=#DCE8F7` 로 배경색.
- 패키지 안에 클래스를 넣을 땐 클래스 `panel_attributes`에 `layer=1` + `bg=white` (패키지 위에 그려지도록). 패키지 요소는 `layer=0`(기본).
- 이 프로젝트에서 쓴 파스텔 색: 패키지 `#DCE8F7`(파랑) `#E3F2DF`(초록) `#FCE9D6`(주황) `#F3E5F5`(보라) `#FFF8D6`(노랑) `#EEEEEE` `#DDDDDD`(회색)
- 범례 노트는 `bg=#FFFFE0`.

### 4.4 관계선 (`Relation`)

```
<element>
<id>Relation</id>
<coordinates><x>807</x><y>250</y><w>40</w><h>130</h></coordinates>
<panel_attributes>lt=-&gt;
m2=1..*
lines</panel_attributes>
<additional_attributes>20;20;20;110</additional_attributes>
</element>
```

| `lt=` | 의미 | 이 프로젝트의 사용 |
|---|---|---|
| `lt=->` | 연관 (화살표 방향으로 참조) | `m2=1..*` 같은 다중도, 3번째 줄에 역할명 |
| `lt=<<<<<-` | 합성 (채워진 마름모가 소유자 쪽) | 소유 관계 (GameSession → BasicGame) |
| `lt=<<<<-` | 집약 (빈 마름모) | 컬렉션 보유 (`m2=0..*`) |
| `lt=.>` | 점선 의존 | 3번째 줄에 `<<use>>` / `<<create>>` / `<<id 참조>>` |

- `m1=`, `m2=` = 선 양 끝의 다중도 (`m2=0..*`, `m2=1`, `m2=0..1`). 줄 3번째부터는 선 가운데 라벨.
- `->>`, `.>>` 변형도 CD-04에서 쓰였는데, 새 프로젝트에서 쓸 때는 UMLet 팔레트에서 의미를 확인한다.
- **좌표 규칙(중요):** `additional_attributes`의 점들은 **관계 요소의 좌상단 기준 상대좌표**다.
  선의 시작점과 끝점이 각 클래스 박스의 가장자리에 닿도록 `coordinates`(x, y, w, h)와 점 좌표를 맞춘다.
  꺾이는 선은 점을 3개 이상 적는다 (`315;20;315;65;20;65;20;110`).
- 선이 박스와 안 닿거나 허공에 뜨면 → 클래스의 x, y, w, h 로 가장자리 좌표를 다시 계산한다.
  (실수 방지를 위해 Python으로 **클래스 박스 좌표에서 관계선 좌표를 계산하는 헬퍼**를 만들어 쓰는 것을 권장.)

### 4.5 이 프로젝트의 CD 분할 방식
한 장에 다 넣지 않고 관점별로 4장으로 나눴다.

| 번호 | 내용 |
|---|---|
| CD-01 | 전체 구조 (패키지 단위 개요, 클래스 이름만 + 의존 화살표) |
| CD-02 | 도메인 모델 (entity/record/enum, 다중도 중심) |
| CD-03 | 게임 로직 (필드·메서드 상세) |
| CD-04 | UI / 저장 계층 |

개요 장에는 클래스 이름만(높이 30), 상세 장에는 필드·메서드까지 적는 식으로 정보 밀도를 나눈다.

---

## 5. Use Case Diagram (`usecase_diagram.uxf`)

- 시스템 경계: `UMLPackage` (이름 한 줄 + `bg=white` + `layer=0`)
- 액터: `UMLActor` (패널 텍스트 = 액터 이름). 주 액터는 왼쪽, 외부 시스템/보조 액터는 오른쪽
- 유스케이스: `UMLUseCase`, 텍스트 `UC-01\n게임 설정` (번호 + 이름)
- 연결선: `Relation`. 액터–유스케이스는 `lt=-` (실선).
  `<<include>>` = `lt=.>` 다음 줄에 `<<include>>` (포함하는 쪽 → 포함되는 쪽),
  `<<extend>>` = `lt=<.` 다음 줄에 `<<extend>>` (화살표가 확장 대상 쪽을 향하도록 방향 확인)
- 번호는 보고서의 Usecase 표와 **반드시 일치**시킨다.

---

## 6. PNG 내보내기 (검증 완료된 방법)

UMLet CLI는 **SVG만** 안정적으로 내보낸다. SVG를 headless Chrome으로 2배율 PNG로 변환하면 글자가 선명하다.

### 6.1 수동 명령

```bash
# 1) uxf -> svg  (같은 폴더에 .svg 생성)
java -jar "C:\Users\gksmf\Desktop\대학\Umlet\umlet.jar" -action=convert -format=svg -filename=a.uxf

# 2) svg 의 width/height 를 읽어 HTML 래퍼 작성 (margin 0, 흰 배경)
#    <html><body style="margin:0;background:#fff"><img src="a.svg" width="W" height="H" style="display:block"></body></html>

# 3) headless Chrome 으로 캡처 (2배율)
"C:\Program Files\Google\Chrome\Application\chrome.exe" --headless=new --disable-gpu --hide-scrollbars \
  --force-device-scale-factor=2 --window-size=W,H --screenshot=a.png file:///.../a.html
```

- 결과 PNG 크기 = (W x 2) x (H x 2). 예: SVG 1226x530 → PNG 2452x1060 (검증됨)
- `log4j:WARN ...` 경고는 무시해도 된다.
- 경로에 한글이 있으면 CLI가 문제를 일으킬 수 있으니 **임시 폴더에 ASCII 이름으로 복사해서 변환**하는 게 안전하다 (아래 스크립트가 그렇게 한다).

### 6.2 스크립트 (그대로 복사해 사용)

```python
#!/usr/bin/env python3
"""UMLet .uxf -> crisp 2x PNG.  usage: python umlet_export.py OUT_DIR a.uxf [b.uxf ...]
UMLet CLI exports SVG only, so rasterize it with headless Chrome."""
import os, re, shutil, subprocess, sys, tempfile, pathlib

UMLET_JAR = os.environ.get("UMLET_JAR", r"C:\Users\gksmf\Desktop\대학\Umlet\umlet.jar")
CHROME = os.environ.get("CHROME", r"C:\Program Files\Google\Chrome\Application\chrome.exe")
SCALE = 2

def export(uxf: pathlib.Path, out_dir: pathlib.Path):
    with tempfile.TemporaryDirectory() as tmp:
        tmp = pathlib.Path(tmp)
        src = tmp / "d.uxf"                      # ASCII name: avoids Korean-path issues in the CLI
        shutil.copy(uxf, src)
        subprocess.run(["java", "-jar", UMLET_JAR, "-action=convert", "-format=svg",
                        f"-filename={src}"], check=True, capture_output=True)
        svg = (tmp / "d.svg").read_text(encoding="utf-8")
        w = int(re.search(r'\swidth="(\d+)"', svg).group(1))
        h = int(re.search(r'\sheight="(\d+)"', svg).group(1))
        (tmp / "d.html").write_text(
            f'<html><body style="margin:0;background:#fff"><img src="d.svg" width="{w}" height="{h}" '
            f'style="display:block"></body></html>', encoding="utf-8")
        png = tmp / "d.png"
        subprocess.run([CHROME, "--headless=new", "--disable-gpu", "--hide-scrollbars",
                        f"--force-device-scale-factor={SCALE}", f"--window-size={w},{h}",
                        f"--screenshot={png}", (tmp / "d.html").as_uri()],
                       check=True, capture_output=True)
        out_dir.mkdir(parents=True, exist_ok=True)
        dst = out_dir / (uxf.stem + ".png")
        shutil.copy(png, dst)
        print(f"{uxf.name} -> {dst} ({w*SCALE}x{h*SCALE})")

if __name__ == "__main__":
    out = pathlib.Path(sys.argv[1])
    for f in sys.argv[2:]:
        export(pathlib.Path(f), out)
```

사용: `python umlet_export.py doc/diagram doc/SD-*.uxf doc/CD-*.uxf`
(Java 21, Chrome 설치 필요. 경로가 다르면 환경변수 `UMLET_JAR`, `CHROME` 으로 지정)

### 6.3 확인 절차
1. PNG를 Claude가 직접 열어본다 (Read로 이미지 열람).
2. 점검 항목: 라벨 겹침/잘림, 클래스 박스 하단 잘림, 관계선이 박스에 닿는지, 노트가 프레임 밖에 제대로 놓였는지, 한글 깨짐 여부.
3. 문제가 있으면 `.uxf`의 크기·좌표를 고쳐서 6.2를 다시 실행.

---

## 7. 사용자가 직접 UMLet에서 여는 방법 (수정 필요 시)

- `Umlet.exe` 실행 → File > Open → `.uxf` 선택. 요소를 클릭하면 아래 Properties 창에 `panel_attributes` 텍스트가 그대로 보이고, 거기서 바로 편집할 수 있다.
- 시퀀스를 처음부터 GUI로 만들 땐: 팔레트 드롭다운 "UML Sequence - All in one" → 요소 추가 → Properties 내용 전체 교체. (**"Activity - All in one"이나 "Deprecated" 팔레트는 쓰지 않는다**) 노트는 "UML Common Elements"의 Note.
- 과제 제출 시 UMLet 실행 파일 일체가 필요하면 (Umlet.exe, umlet.jar, custom_elements, img, lib, palettes 등) 폴더째 zip에 포함한다.

---

## 8. Claude에게 작업 시킬 때 프롬프트 예시

> `UMLet_다이어그램_작업_가이드.md` 를 읽고, 이 프로젝트의 유스케이스 문서(`doc/...`)와 소스 코드를 기준으로
> UC-01~UC-05 시퀀스 다이어그램을 만들어줘.
> - `.uxf` 는 `doc/`, PNG 는 `doc/diagram/` 에 저장
> - 파일명은 `SD-NN_UC-NN_<시나리오>.uxf`
> - 기본 흐름 / 대안 흐름을 별도 다이어그램으로 분리
> - 메서드명은 실제 코드에서 가져올 것
> - 그린 뒤 PNG를 직접 열어서 겹침·잘림 확인하고 고칠 것

> 같은 가이드를 따라 패키지 `xxx.yyy` 의 클래스 다이어그램을 개요 1장 + 상세 N장으로 만들어줘.

바뀌는 값(다른 프로젝트에서 달라질 것): UMLet/Chrome 경로, 스테레오타입 종류(계층 구조), 파일명 접두사, 출력 폴더, 대응하는 유스케이스 번호 체계.

---

## 9. 자주 하는 실수 체크리스트

- [ ] `<` `>` `&` 이스케이프 누락 → UMLet에서 열리지 않음
- [ ] 자기 호출(`a->>>a +`) 뒤에 `tick=` 누락 → 실행 막대가 안 닫힘
- [ ] 객체/클래스 구분 (`_:Name_` 밑줄 여부)이 코드의 인스턴스/static 호출과 불일치
- [ ] 메시지 번호가 시간 순서와 다름 / 대안 흐름 번호가 UC 문서와 다름
- [ ] 시퀀스 요소 크기 부족 → 라벨 겹침, 프레임 잘림
- [ ] 클래스 박스 높이 부족 → 마지막 메서드 잘림
- [ ] 관계선 좌표가 박스 가장자리와 안 맞음
- [ ] 패키지 안 클래스에 `layer=1` 없음 → 패키지 뒤로 숨음
- [ ] 코드 수정 후 다이어그램 미갱신 (이 프로젝트에서 "정답 보기" 수정 후 SD/CD를 다시 갱신함)
- [ ] 한글 경로 그대로 CLI에 넘겨 변환 실패
