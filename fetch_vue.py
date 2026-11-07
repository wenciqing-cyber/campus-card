# -*- coding: utf-8 -*-
"""从 npm 缓存中取出指定包的 tarball（网络不通时的离线方案）。"""
import base64
import glob
import json
import os
import sys
import tarfile

CACHE = os.path.expanduser(r"C:\Users\WCQ\AppData\Local\npm-cache\_cacache")


def find_tarball(key_part):
    for f in glob.glob(os.path.join(CACHE, "index-v5", "**"), recursive=True):
        if not os.path.isfile(f):
            continue
        with open(f, encoding="utf-8", errors="ignore") as fh:
            for line in fh:
                if key_part not in line:
                    continue
                tab = line.find("\t")
                if tab < 0:
                    continue
                try:
                    meta = json.loads(line[tab + 1:])
                except Exception:
                    continue
                integ = meta.get("integrity", "")
                if not integ.startswith("sha512-"):
                    continue
                h = base64.b64decode(integ[7:]).hex()
                p = os.path.join(CACHE, "content-v2", "sha512", h[:2], h[2:4], h[4:])
                if os.path.isfile(p):
                    return p
    return None


def extract(key_part, wanted, outdir):
    tb = find_tarball(key_part)
    if not tb:
        print("MISS", key_part)
        return False
    os.makedirs(outdir, exist_ok=True)
    with tarfile.open(tb, "r:gz") as tar:
        for m in tar.getmembers():
            name = m.name.replace("package/", "", 1)
            if name in wanted:
                data = tar.extractfile(m).read()
                dst = os.path.join(outdir, os.path.basename(name))
                with open(dst, "wb") as o:
                    o.write(data)
                print("OK", name, len(data), "->", dst)
    return True


if __name__ == "__main__":
    extract(
        "vue/-/vue-3.5.44.tgz",
        {"dist/vue.esm-browser.js", "dist/vue.global.js", "dist/vue.runtime.esm-browser.js"},
        r"D:\Study\课程资料\软件测试\资料\期末\campus-card\frontend\vendor",
    )
