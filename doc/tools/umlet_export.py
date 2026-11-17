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
        src = tmp / "d.uxf"
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
