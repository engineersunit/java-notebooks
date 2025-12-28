#!/usr/bin/env node
/**
 * Refactor Interactive Java Notebook (.ijnb) files to use IO.println
 * instead of System.out.println, per JEP 512.
 *
 * Behavior:
 * - Only modifies code cells (cell_type === 'code').
 * - Replaces occurrences of System.out.println(...) with IO.println(...).
 * - Leaves markdown cells and other content untouched.
 *
 * Caveats:
 * - This is a textual replacement; it may also change occurrences inside string
 *   literals if they exist. Notebooks here typically don't embed such strings.
 * - Does NOT change System.out.printf or System.out.print (only println).
 *
 * Usage:
 *   node scripts/refactor-println.js --check notebooks     (dry run)
 *   node scripts/refactor-println.js notebooks             (apply fixes)
 *   node scripts/refactor-println.js --check .             (dry run repo-wide)
 *   node scripts/refactor-println.js .                     (apply repo-wide)
 */

const fs = require('fs');
const path = require('path');

function statPath(p) {
  try {
    const st = fs.statSync(p);
    return { exists: true, isFile: st.isFile(), isDirectory: st.isDirectory() };
  } catch (_) {
    return { exists: false, isFile: false, isDirectory: false };
  }
}

function walk(dir, acc = []) {
  let entries;
  try {
    entries = fs.readdirSync(dir, { withFileTypes: true });
  } catch (e) {
    acc.push({ __walk_error__: true, dir, error: 'readdir error: ' + e.message });
    return acc;
  }
  for (const e of entries) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) {
      if (['.git', 'node_modules', '.vscode', 'out', 'build', 'target'].includes(e.name)) continue;
      walk(p, acc);
    } else if (e.isFile() && e.name.endsWith('.ijnb')) {
      acc.push(p);
    }
  }
  return acc;
}

const PRINTLN_REGEX = /System\.out\.println\s*\(/g;

function replaceInSource(source) {
  if (typeof source === 'string') {
    const replaced = source.replace(PRINTLN_REGEX, 'IO.println(');
    return { changed: replaced !== source, value: replaced };
  }
  if (Array.isArray(source)) {
    let changed = false;
    const replacedArr = source.map((line) => {
      const newLine = typeof line === 'string' ? line.replace(PRINTLN_REGEX, 'IO.println(') : line;
      if (newLine !== line) changed = true;
      return newLine;
    });
    return { changed, value: replacedArr };
  }
  return { changed: false, value: source };
}

function refactorFile(file) {
  let text;
  try {
    text = fs.readFileSync(file, 'utf8');
  } catch (e) {
    return { file, error: 'read error: ' + e.message };
  }

  let json;
  try {
    json = JSON.parse(text);
  } catch (e) {
    return { file, error: 'JSON parse error: ' + e.message };
  }

  let changed = false;

  if (Array.isArray(json.cells)) {
    for (const cell of json.cells) {
      if (!cell || cell.cell_type !== 'code') continue;
      const res = replaceInSource(cell.source);
      if (res.changed) {
        cell.source = res.value;
        changed = true;
      }
    }
  }

  if (changed) {
    return { file, changed, json };
  }
  return { file, changed };
}

function processTarget(target, checkOnly) {
  const info = statPath(target);
  const results = [];
  const files = [];

  if (!info.exists) {
    return { files, results: [{ file: target, error: 'path does not exist' }] };
  }

  if (info.isFile) {
    files.push(target);
  } else if (info.isDirectory) {
    const walked = walk(target, []);
    for (const w of walked) {
      if (typeof w === 'string') files.push(w);
      else if (w && w.__walk_error__) results.push({ file: w.dir, error: w.error });
    }
  } else {
    return { files, results: [{ file: target, error: 'unsupported path type' }] };
  }

  for (const file of files) {
    const res = refactorFile(file);
    results.push(res);
    if (res.changed && !checkOnly && res.json) {
      try {
        fs.writeFileSync(file, JSON.stringify(res.json, null, 2) + '\n', 'utf8');
      } catch (e) {
        res.error = 'write error: ' + e.message;
      }
    }
  }

  return { files, results };
}

function summarize(target, checkOnly, files, results) {
  const changed = results.filter(r => r.changed);
  const errors = results.filter(r => r.error);

  console.log(`Target: ${target}`);
  console.log(`Scanned ${files.length} .ijnb file(s)`);
  console.log(`${changed.length} file(s) ${checkOnly ? 'would be modified (dry-run)' : 'modified if needed'}.`);

  if (errors.length) {
    console.log(`\nErrors (${errors.length}):`);
    for (const e of errors) console.log(`- ${e.file}: ${e.error}`);
  }

  if (changed.length) {
    console.log(`\n${checkOnly ? 'Would change' : 'Changed'}:`);
    for (const c of changed) console.log(`- ${c.file}`);
  }
}

function main() {
  const args = process.argv.slice(2);
  const checkOnly = args.includes('--check');
  const targetArg = args.find(a => !a.startsWith('--')) || '.';

  const { files, results } = processTarget(targetArg, checkOnly);
  summarize(targetArg, checkOnly, files, results);

  const hadErrors = results.some(r => r.error);
  if (hadErrors) process.exitCode = 1;
}

if (require.main === module) {
  main();
}
