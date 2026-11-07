#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
把 dist/StudyProxy-Java-linux 目录打成 tar.gz，并显式设置 Unix 权限位。
Windows 自带 tar(bsdtar) 不写权限位，因此用 Python 的 tarfile 保证
解压后 start.sh 直接可执行。
"""
import os
import sys
import tarfile

PKG = "StudyProxy-Java-linux"
EXECUTABLES = {"start.sh"}
MTIME = 1735689600  # 2025-01-01，固定时间戳保证可重复构建


def normalize(info):
    info.uid = 0
    info.gid = 0
    info.uname = "root"
    info.gname = "root"
    info.mtime = MTIME
    if info.isdir():
        info.mode = 0o755
    elif os.path.basename(info.name) in EXECUTABLES:
        info.mode = 0o755
    else:
        info.mode = 0o644
    return info


def main():
    root = os.path.abspath("dist")
    src = os.path.join(root, PKG)
    if not os.path.isdir(src):
        sys.stderr.write("未找到目录: %s\n" % src)
        return 1

    out = os.path.join(root, PKG + ".tar.gz")
    if os.path.exists(out):
        os.remove(out)

    with tarfile.open(out, "w:gz") as tf:
        tf.add(src, arcname=PKG, recursive=True, filter=normalize)

    print("已生成: %s (%d 字节)" % (out, os.path.getsize(out)))
    with tarfile.open(out, "r:gz") as tf:
        for m in tf.getmembers():
            print("  %s %s" % (oct(m.mode), m.name))
    return 0


if __name__ == "__main__":
    sys.exit(main())
