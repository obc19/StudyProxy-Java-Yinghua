import os
import tarfile

BASE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(BASE, "StudyProxy-Java-linux")
OUT = os.path.join(BASE, "StudyProxy-Java-linux.tar.gz")

EXEC = {"start.sh"}

with tarfile.open(OUT, "w:gz", format=tarfile.GNU_FORMAT) as tar:
    root_ti = tar.gettarinfo(SRC, arcname="StudyProxy-Java-linux")
    root_ti.mode = 0o755
    root_ti.uid = root_ti.gid = 0
    root_ti.uname = root_ti.gname = "root"
    tar.addfile(root_ti)
    for root, dirs, files in os.walk(SRC):
        dirs.sort()
        rel_root = os.path.relpath(root, SRC)
        arc_root = "StudyProxy-Java-linux" if rel_root == "." else "StudyProxy-Java-linux/" + rel_root.replace(os.sep, "/")
        for d in sorted(dirs):
            ti = tar.gettarinfo(os.path.join(root, d), arcname=arc_root + "/" + d)
            ti.mode = 0o755
            tar.addfile(ti)
        for f in sorted(files):
            path = os.path.join(root, f)
            ti = tar.gettarinfo(path, arcname=arc_root + "/" + f)
            ti.uid = ti.gid = 0
            ti.uname = ti.gname = "root"
            ti.mode = 0o755 if f in EXEC else (0o755 if f.endswith(".py") else 0o644)
            with open(path, "rb") as fh:
                tar.addfile(ti, fh)

print("written:", OUT, os.path.getsize(OUT))
with tarfile.open(OUT, "r:gz") as tar:
    for m in tar.getmembers():
        print(f"{m.mode:o}  {m.size:>10}  {m.name}")
