#!/usr/bin/env node
/**
 * Refactor Java (.java) files to use IO.println(...) instead of System.out.println(...).
 *
 * Key points:
 * - Only replaces real code occurrences, NOT inside string literals, text blocks ("""),
 *   character literals, or comments. This preserves demo snippets shown inside strings.
 * - Does NOT change System.out.print or System.out.printf (only println).
 * - Skips IO.java files to avoid rewriting the IO helper itself.
 * - For each directory where at least one file was modified, creates an IO.java with:
 *     public final class IO {
 *       private IO() {}
 *       public static void println() { System.out.println(); }
 *       public static void println(Object o) { System.out.println(String.valueOf(o)); }
 *     }
 *
 * Usage:
 *   node scripts/refactor-java-println.js --check .   (dry run)
 *   node scripts/refactor-java-println.js .           (apply changes)
 */

const fs = require('fs');
const path = require('path');

const IGNORED_DIRS = new Set(['.git', 'node_modules', '.vscode', 'out', 'build', 'target']);

function statPath(p) {
  try {
    const st = fs.statSync(p);
    return { exists: true, isFile: st.isFile(), isDirectory: st.isDirectory() };
  } catch (_) {
    return { exists: false, isFile: false, isDirectory: false };
  }
}

function walkForJava(dir, acc = []) {
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
      if (IGNORED_DIRS.has(e.name)) continue;
      walkForJava(p, acc);
    } else if (e.isFile() && e.name.endsWith('.java')) {
      acc.push(p);
    }
  }
  return acc;
}

/**
 * Replace System.out.println( ... ) with IO.println( ... ) OUTSIDE of:
 * - string literals "..."
 * - text blocks """ ... """
 * - char literals 'c'
 * - line comments // ...
 * - block comments /* ... *\/
 *
 * Also tolerates optional whitespace between "println" and "(".
 */
function replaceOutsideStringsAndComments(src) {
  const needle = 'System.out.println';
  let i = 0;
  let out = '';
  const n = src.length;

  let inString = false;       // "..."
  let inChar = false;         // 'c'
  let inTextBlock = false;    // """ ... """
  let inLineComment = false;  // // ...
  let inBlockComment = false; // /* ... */
  let pendingSlash = false;

  function peek(k = 0) {
    return src[i + k];
  }

  function startsWithAt(s, idx) {
    return src.startsWith(s, idx);
  }

  while (i < n) {
    let ch = src[i];
    let next = i + 1 < n ? src[i + 1] : '';

    // Handle end of line comment
    if (inLineComment) {
      out += ch;
      if (ch === '\n') {
        inLineComment = false;
      }
      i++;
      continue;
    }

    // Handle end of block comment
    if (inBlockComment) {
      out += ch;
      if (ch === '*' && next === '/') {
        out += next;
        i += 2;
        inBlockComment = false;
      } else {
        i++;
      }
      continue;
    }

    // Handle text block (""" ... """)
    if (inTextBlock) {
      out += ch;
      // Look for terminating """
      if (ch === '"' && startsWithAt('"""', i)) {
        // We already added one '"', add the next two and close
        out += src[i + 1] || '';
        out += src[i + 2] || '';
        i += 3;
        inTextBlock = false;
      } else {
        i++;
      }
      continue;
    }

    // Handle regular string literal
    if (inString) {
      out += ch;
      if (ch === '\\\\') {
        // escape next char
        if (i + 1 < n) {
          out += src[i + 1];
          i += 2;
        } else {
          i++;
        }
      } else if (ch === '"') {
        inString = false;
        i++;
      } else {
        i++;
      }
      continue;
    }

    // Handle char literal
    if (inChar) {
      out += ch;
      if (ch === '\\\\') {
        if (i + 1 < n) {
          out += src[i + 1];
          i += 2;
        } else {
          i++;
        }
      } else if (ch === '\'') {
        inChar = false;
        i++;
      } else {
        i++;
      }
      continue;
    }

    // Outside of strings/comments: detect comment starts
    if (ch === '/' && next === '/') {
      inLineComment = true;
      out += ch;
      out += next;
      i += 2;
      continue;
    }
    if (ch === '/' && next === '*') {
      inBlockComment = true;
      out += ch;
      out += next;
      i += 2;
      continue;
    }

    // Detect start of text block: """
    if (ch === '"' && startsWithAt('"""', i)) {
      inTextBlock = true;
      // copy the opening """
      out += '"';
      out += '"';
      out += '"';
      i += 3;
      continue;
    }

    // Detect start of string literal: "
    if (ch === '"') {
      inString = true;
      out += ch;
      i++;
      continue;
    }

    // Detect start of char literal: '
    if (ch === '\'') {
      inChar = true;
      out += ch;
      i++;
      continue;
    }

    // Attempt targeted replacement when outside strings/comments
    if (startsWithAt(needle, i)) {
      // position after "System.out.println"
      let j = i + needle.length;

      // Skip optional whitespace between println and '('
      while (j < n && (src[j] === ' ' || src[j] === '\\t')) j++;

      if (j < n && src[j] === '(') {
        // Perform replacement
        out += 'IO.println(';
        i = j + 1; // skip '(' as we already wrote it
        continue;
      }
      // If no '(', fall through without replacing
    }

    // Default: copy char
    out += ch;
    i++;
  }

  return out;
}

function refactorJavaFile(filePath) {
  let original;
  try {
    original = fs.readFileSync(filePath, 'utf8');
  } catch (e) {
    return { file: filePath, error: 'read error: ' + e.message };
  }

  const transformed = replaceOutsideStringsAndComments(original);
  const changed = transformed !== original;

  return { file: filePath, changed, content: transformed };
}

const IO_JAVA_CONTENT = `public final class IO {
    private IO() {}
    public static void println() { System.out.println(); }
    public static void println(Object o) { System.out.println(String.valueOf(o)); }
}
`;

function ensureIOForDirs(dirs, checkOnly) {
  const created = [];
  const errors = [];
  for (const dir of dirs) {
    const ioPath = path.join(dir, 'IO.java');
    try {
      if (fs.existsSync(ioPath)) continue;
      if (checkOnly) {
        created.push(ioPath + ' (would create)');
      } else {
        fs.writeFileSync(ioPath, IO_JAVA_CONTENT, 'utf8');
        created.push(ioPath);
      }
    } catch (e) {
      errors.push({ file: ioPath, error: 'write error: ' + e.message });
    }
  }
  return { created, errors };
}

function processTarget(target, checkOnly) {
  const info = statPath(target);
  const results = [];
  const files = [];

  if (!info.exists) {
    return { files, results: [{ file: target, error: 'path does not exist' }], createdIO: [], ioErrors: [] };
  }

  if (info.isFile) {
    if (target.endsWith('.java')) {
      files.push(target);
    } else {
      return { files, results: [{ file: target, error: 'not a .java file' }], createdIO: [], ioErrors: [] };
    }
  } else if (info.isDirectory) {
    const walked = walkForJava(target, []);
    for (const w of walked) {
      if (typeof w === 'string') files.push(w);
      else if (w && w.__walk_error__) results.push({ file: w.dir, error: w.error });
    }
  } else {
    return { files, results: [{ file: target, error: 'unsupported path type' }], createdIO: [], ioErrors: [] };
  }

  const changedDirs = new Set();

  for (const file of files) {
    // Avoid rewriting the IO helper itself to prevent recursion.
    if (path.basename(file) === 'IO.java') {
      results.push({ file, changed: false });
      continue;
    }
    const res = refactorJavaFile(file);
    results.push(res);
    if (res.changed) {
      changedDirs.add(path.dirname(file));
      if (!checkOnly) {
        try {
          fs.writeFileSync(file, res.content, 'utf8');
        } catch (e) {
          res.error = 'write error: ' + e.message;
        }
      }
    }
  }

  // Ensure IO.java in each directory that had modifications
  const { created, errors } = ensureIOForDirs(changedDirs, checkOnly);

  return { files, results, createdIO: created, ioErrors: errors };
}

function summarize(target, checkOnly, files, results, createdIO, ioErrors) {
  const changed = results.filter(r => r.changed);
  const errors = results.filter(r => r.error);

  console.log(`Target: ${target}`);
  console.log(`Scanned ${files.length} .java file(s)`);
  console.log(`${changed.length} file(s) ${checkOnly ? 'would be modified (dry-run)' : 'modified if needed'}.`);

  if (createdIO.length) {
    console.log(`\n${checkOnly ? 'Would create' : 'Created'} IO.java in ${createdIO.length} director${createdIO.length === 1 ? 'y' : 'ies'}:`);
    for (const c of createdIO) console.log(`- ${c}`);
  }

  if (errors.length || ioErrors.length) {
    console.log(`\nErrors (${errors.length + ioErrors.length}):`);
    for (const e of errors) console.log(`- ${e.file}: ${e.error}`);
    for (const e of ioErrors) console.log(`- ${e.file}: ${e.error}`);
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

  const { files, results, createdIO, ioErrors } = processTarget(targetArg, checkOnly);
  summarize(targetArg, checkOnly, files, results, createdIO, ioErrors);

  const hadErrors = results.some(r => r.error) || ioErrors.length > 0;
  if (hadErrors) process.exitCode = 1;
}

if (require.main === module) {
  main();
}
