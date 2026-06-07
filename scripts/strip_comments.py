import os
import argparse
import shutil
import time


def strip_comments(code: str) -> str:
    out = []
    i = 0
    n = len(code)
    state = 'normal'
    while i < n:
        c = code[i]
        if state == 'normal':
            if c == '"':
                out.append(c)
                state = 'dquote'
                i += 1
            elif c == "'":
                out.append(c)
                state = 'squote'
                i += 1
            elif c == '/' and i + 1 < n and code[i+1] == '/':
                i += 2
                while i < n and code[i] != '\n':
                    i += 1
            elif c == '/' and i + 1 < n and code[i+1] == '*':
                i += 2
                while i + 1 < n and not (code[i] == '*' and code[i+1] == '/'):
                    if code[i] == '\n':
                        out.append('\n')
                    i += 1
                i += 2
            else:
                out.append(c)
                i += 1
        elif state == 'dquote':
            if c == '\\' and i + 1 < n:
                out.append(c)
                out.append(code[i+1])
                i += 2
            elif c == '"':
                out.append(c)
                state = 'normal'
                i += 1
            else:
                out.append(c)
                i += 1
        elif state == 'squote':
            if c == '\\' and i + 1 < n:
                out.append(c)
                out.append(code[i+1])
                i += 2
            elif c == "'":
                out.append(c)
                state = 'normal'
                i += 1
            else:
                out.append(c)
                i += 1
    return ''.join(out)


def process_file(path: str, backup_root: str, dry_run: bool = False) -> bool:
    rel = os.path.relpath(path)
    bk_path = os.path.join(backup_root, rel)
    os.makedirs(os.path.dirname(bk_path), exist_ok=True)
    with open(path, 'r', encoding='utf-8') as f:
        orig = f.read()
    if dry_run:
        print(f"[DRY] Would process: {path}")
        return True
    with open(bk_path, 'w', encoding='utf-8') as f:
        f.write(orig)
    new = strip_comments(orig)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(new)
    print(f"Processed: {path} -> backup at {bk_path}")
    return True


def find_target_files(root: str):
    targets = [
        os.path.join('src', 'main', 'java', 'com', 'superball', 'controller'),
        os.path.join('src', 'main', 'java', 'com', 'superball', 'entity'),
        os.path.join('src', 'main', 'java', 'com', 'superball', 'repository'),
        os.path.join('src', 'main', 'java', 'com', 'superball', 'service'),
    ]
    files = []
    for t in targets:
        full = os.path.join(root, t)
        if not os.path.isdir(full):
            continue
        for dirpath, _, filenames in os.walk(full):
            for fn in filenames:
                if fn.endswith('.java'):
                    files.append(os.path.join(dirpath, fn))
    return files


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--root', default='.', help='workspace root')
    parser.add_argument('--dry', action='store_true', help='dry run')
    args = parser.parse_args()
    root = args.root
    ts = time.strftime('%Y%m%d_%H%M%S')
    backup_root = os.path.join('.comment_backups', ts)
    os.makedirs(backup_root, exist_ok=True)
    files = find_target_files(root)
    if not files:
        print('No target files found.')
        return
    print(f'Found {len(files)} files. Backups in {backup_root}')
    for p in files:
        try:
            process_file(p, backup_root, dry_run=args.dry)
        except Exception as e:
            print(f'Error processing {p}: {e}')
    print('Done.')


if __name__ == '__main__':
    main()
