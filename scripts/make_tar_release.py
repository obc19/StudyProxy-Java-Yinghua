#!/usr/bin/env python3
"""组装并打包 Linux 发布包。

源文件全部来自仓库内的固定位置，**永远使用 config.example.yaml 作为配置模板**，
不会把本地已填真实账号的 config.yaml 带进发布包。

用法:
    python scripts/make_tar_release.py              # 输出到 dist/
    python scripts/make_tar_release.py --dest DIR   # 输出到指定目录（自测用）
"""

import argparse
import os
import shutil
import sys
import tarfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PKG = "StudyProxy-Java-linux"

# (源文件, 包内路径, 是否强制 LF)
FILES = [
    ("deploy/linux/start.sh", "start.sh", True),
    ("deploy/linux/README.md", "README.md", True),
    ("ocr/ocr_service.py", "ocr_service.py", True),
    ("ocr/requirements.txt", "requirements.txt", True),
    ("config.example.yaml", "config.yaml", True),
    ("config.example.yaml", "config.example.yaml", True),
]


def locate_jar():
    cands = sorted((ROOT / "target").glob("StudyProxy-Java-*.jar"))
    cands = [c for c in cands if not c.name.endswith(".original")]
    if not cands:
        sys.exit("[错误] 未找到 target/StudyProxy-Java-*.jar，请先执行 mvn package")
    return cands[0]


def read_lf(src: Path) -> bytes:
    return src.read_bytes().replace(b"\r\n", b"\n")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--dest", default=str(ROOT / "dist"))
    dest = Path(ap.parse_args().dest).resolve()

    jar = locate_jar()
    stage = dest / PKG
    if stage.exists():
        shutil.rmtree(stage)
    (stage / "logs").mkdir(parents=True)

    shutil.copy2(jar, stage / jar.name)
    for src_rel, dst_rel, to_lf in FILES:
        src = ROOT / src_rel
        if not src.exists():
            sys.exit(f"[错误] 缺少源文件: {src_rel}")
        out = stage / dst_rel
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_bytes(read_lf(src) if to_lf else src.read_bytes())

    tar_path = dest / f"{PKG}.tar.gz"
    if tar_path.exists():
        tar_path.unlink()
    with tarfile.open(tar_path, "w:gz", format=tarfile.GNU_FORMAT) as tar:
        ti = tar.gettarinfo(str(stage), arcname=PKG)
        ti.mode, ti.uid, ti.gid, ti.uname, ti.gname = 0o755, 0, 0, "root", "root"
        tar.addfile(ti)
        for path in sorted(stage.rglob("*")):
            rel = path.relative_to(stage).as_posix()
            arc = f"{PKG}/{rel}"
            ti = tar.gettarinfo(str(path), arcname=arc)
            ti.uid = ti.gid = 0
            ti.uname = ti.gname = "root"
            if path.is_dir():
                ti.mode = 0o755
                tar.addfile(ti)
            else:
                ti.mode = 0o755 if path.name in ("start.sh", "ocr_service.py") else 0o644
                with open(path, "rb") as fh:
                    tar.addfile(ti, fh)

    print(f"[完成] {tar_path}  ({os.path.getsize(tar_path) / 1048576:.2f} MB)")
    with tarfile.open(tar_path) as tar:
        for m in tar.getmembers():
            print(f"  {m.mode:o}  {m.size:>10}  {m.name}")


if __name__ == "__main__":
    main()
