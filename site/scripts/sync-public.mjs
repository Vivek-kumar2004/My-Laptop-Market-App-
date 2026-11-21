import { cpSync, existsSync, mkdirSync, rmSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const siteRoot = join(dirname(fileURLToPath(import.meta.url)), "..");
const websiteRoot = join(siteRoot, "..", "website");
const publicDir = join(siteRoot, "public");

const copies = [
  ["images", "images"],
  ["favicon.ico", "favicon.ico"],
  ["apple-touch-icon.png", "apple-touch-icon.png"],
];

mkdirSync(publicDir, { recursive: true });

for (const [fromRel, toRel] of copies) {
  const from = join(websiteRoot, fromRel);
  const to = join(publicDir, toRel);
  if (!existsSync(from)) {
    console.warn(`skip missing ${fromRel}`);
    continue;
  }
  rmSync(to, { recursive: true, force: true });
  mkdirSync(dirname(to), { recursive: true });
  cpSync(from, to, { recursive: true });
  console.log(`synced ${fromRel} -> public/${toRel}`);
}
