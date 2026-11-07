#!/usr/bin/env python3
"""组装并打包 Windows 发布包。

源文件全部来自仓库内的固定位置，**永远使用 config.example.yaml 作为配置模板**，
不会把本地已填真实账号的 config.yaml 带进发布包。

用法:
    python scripts/make_zip_release.py              # 输出到 dist/
    python scripts/make_zip_release.py --dest DIR   # 输出到指定目录（自测用）
"""

import argparse
import os
import shutil
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PKG = "StudyProxy-Java-windows"

# (源文件, 包内路径)
FILES = [
    ("deploy/windows/start-all.bat", "start-all.bat"),
    ("deploy/windows/README.md", "README.md"),
    ("ocr/ocr_service.py", "ocr_service.py"),
    ("ocr/requirements.txt", "requirements.txt"),
    ("config.example.yaml", "config.yaml"),
    ("config.example.yaml", "config.example.yaml"),
]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--dest", default=str(ROOT / "dist"))
    dest = Path(ap.parse_args().dest).resolve()
    dest.mkdir(parents=True, exist_ok=True)

    exe = ROOT / "target" / "StudyProxy.exe"
    if not exe.exists():
        sys.exit("[错误] 未找到 target/StudyProxy.exe，请先执行 scripts/build-exe.bat 或 mvn package")

    stage = dest / PKG
    if stage.exists():
        shutil.rmtree(stage)
    (stage / "logs").mkdir(parents=True)
    shutil.copy2(exe, stage / "StudyProxy.exe")

    for src_rel, dst_rel in FILES:
        src = ROOT / src_rel
        if not src.exists():
            sys.exit(f"[错误] 缺少源文件: {src_rel}")
        out = stage / dst_rel
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_bytes(src.read_bytes())

    zip_path = dest / f"{PKG}.zip"
    if zip_path.exists():
        zip_path.unlink()
    with zipfile.ZipFile(zip_path, "w", zipfile.ZIP_DEFLATED, compresslevel=9) as z:
        z.writestr(zipfile.ZipInfo(PKG + "/"), "")
        z.writestr(zipfile.ZipInfo(PKG + "/logs/"), "")
        for path in sorted(stage.rglob("*")):
            if path.is_dir():
                continue
            z.write(path, f"{PKG}/{path.relative_to(stage).as_posix()}")

    print(f"[完成] {zip_path}  ({os.path.getsize(zip_path) / 1048576:.2f} MB)")
    with zipfile.ZipFile(zip_path) as z:
        for i in z.infolist():
            print(f"  {i.file_size:>10}  {i.filename}")


if __name__ == "__main__":
    main()
