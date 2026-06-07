param(
    [string]$Root = '.',
    [switch]$Dry
)

$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$backupRoot = Join-Path -Path $Root -ChildPath ".comment_backups\$timestamp"
Write-Host "Backups will be stored in $backupRoot"

$targets = @(
    'src\main\java\com\superball\controller',
    'src\main\java\com\superball\entity',
    'src\main\java\com\superball\repository',
    'src\main\java\com\superball\service'
)

foreach ($t in $targets) {
    $full = Join-Path -Path $Root -ChildPath $t
    if (-Not (Test-Path $full)) { continue }
    Get-ChildItem -Path $full -Filter *.java -Recurse | ForEach-Object {
        $file = $_.FullName
        $rel = $file.Substring((Get-Location).Path.Length).TrimStart('\', '/')
        $backupPath = Join-Path -Path $backupRoot -ChildPath $rel
        $bkDir = Split-Path $backupPath -Parent
        if (-Not (Test-Path $bkDir)) { New-Item -ItemType Directory -Path $bkDir -Force | Out-Null }
        if ($Dry) {
            Write-Host "[DRY] Would backup and process: $file"
            return
        }
        $orig = Get-Content -Raw -LiteralPath $file
        Set-Content -LiteralPath $backupPath -Value $orig -Encoding utf8

        # remove block comments /* ... */
        $noBlock = [regex]::Replace($orig, '/\*[\s\S]*?\*/', '')
        # remove line comments //...
        $noLine = [regex]::Replace($noBlock, '//.*', '')

        Set-Content -LiteralPath $file -Value $noLine -Encoding utf8
        Write-Host "Processed: $file -> backup at $backupPath"
    }
}

Write-Host 'Done.'
