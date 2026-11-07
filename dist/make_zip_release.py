import os
import zipfile

BASE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(BASE, "StudyProxy-Java-windows")
OUT = os.path.join(BASE, "StudyProxy-Java-windows.zip")

SKIP = {"desktop.ini", ".DS_Store"}

with zipfile.ZipFile(OUT, "w", zipfile.ZIP_DEFLATED, compresslevel=9) as z:
    root = "StudyProxy-Java-windows"
    z.writestr(zipfile.ZipInfo(root + "/"), "")
    for cur, dirs, files in os.walk(SRC):
        dirs.sort()
        rel = os.path.relpath(cur, SRC)
        prefix = root + "/" + ("" if rel == "." else rel.replace(os.sep, "/") + "/")
        if rel != ".":
            z.writestr(zipfile.ZipInfo(prefix), "")
        for f in sorted(files):
            if f in SKIP:
                continue
            z.write(os.path.join(cur, f), prefix + f)

print("written:", OUT, os.path.getsize(OUT))
with zipfile.ZipFile(OUT) as z:
    for i in z.infolist():
        print(f"{i.file_size:>10}  {i.filename}")
