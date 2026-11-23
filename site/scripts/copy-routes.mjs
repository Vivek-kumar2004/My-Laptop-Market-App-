import { cpSync, mkdirSync, existsSync } from "node:fs";
import { join, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const root = join(dirname(fileURLToPath(import.meta.url)), "..");
const outDir = join(root, "out");
const indexHtml = join(outDir, "index.html");

if (!existsSync(indexHtml)) {
  console.error("out/index.html missing — run vite build first");
  process.exit(1);
}

const routes = ["why", "how", "vendors", "insights"];

for (const route of routes) {
  const dir = join(outDir, route);
  mkdirSync(dir, { recursive: true });
  cpSync(indexHtml, join(dir, "index.html"));
  console.log(`copied out/${route}/index.html`);
}
