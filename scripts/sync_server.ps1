<#
.SYNOPSIS
    Syncs the repo's managed configs, datapacks, server.properties and JVM flags
    into the live server/ directory.

.DESCRIPTION
    Source of truth lives at the repo root:
        mod_configs_server/            -> server/config/                 (additive overwrite)
        datapacks/<pack>/              -> server/<level-name>/datapacks/ (additive overwrite)
        server_configuration/server.properties -> server/server.properties
        flags/server.txt               -> server/user_jvm_args.txt       (flag lines only)

    The copy is ADDITIVE: existing files are overwritten, but nothing in the
    destination is ever deleted. server/config/ holds ~100 mod-generated files
    that are not tracked in the repo, so a mirror-delete would be destructive.

    <level-name> is read from server_configuration/server.properties (falls back
    to "world").

.PARAMETER DryRun
    Print the copy plan without touching any files.

.EXAMPLE
    ./scripts/sync_server.ps1 -DryRun
    ./scripts/sync_server.ps1
#>
[CmdletBinding()]
param(
    [switch]$DryRun
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

# Repo root = parent of the scripts/ directory this file lives in.
$RepoRoot = Split-Path -Parent $PSScriptRoot
$ServerDir = Join-Path $RepoRoot 'server'

$script:copyCount = 0
$script:skipCount = 0

function Get-LevelName {
    $props = Join-Path $RepoRoot 'server_configuration/server.properties'
    if (Test-Path $props) {
        $line = Get-Content $props | Where-Object { $_ -match '^\s*level-name\s*=' } | Select-Object -First 1
        if ($line) {
            $name = ($line -split '=', 2)[1].Trim()
            if ($name) { return $name }
        }
    }
    return 'world'
}

# Recursively copy every file under $SourceDir into $DestDir, preserving relative
# structure. Overwrites existing files; never deletes.
function Sync-Tree {
    param(
        [Parameter(Mandatory)] [string]$SourceDir,
        [Parameter(Mandatory)] [string]$DestDir,
        [Parameter(Mandatory)] [string]$Label
    )
    if (-not (Test-Path $SourceDir)) {
        Write-Host "  [skip] source missing: $SourceDir" -ForegroundColor Yellow
        return
    }
    Write-Host "== $Label ==" -ForegroundColor Cyan
    Write-Host "   $SourceDir  ->  $DestDir"

    $files = Get-ChildItem -Path $SourceDir -Recurse -File
    foreach ($f in $files) {
        $rel = $f.FullName.Substring($SourceDir.Length).TrimStart('\', '/')
        $target = Join-Path $DestDir $rel
        $exists = Test-Path $target
        $verb = if ($exists) { 'overwrite' } else { 'create   ' }

        if ($DryRun) {
            Write-Host "   [$verb] $rel"
        } else {
            $targetParent = Split-Path -Parent $target
            if (-not (Test-Path $targetParent)) {
                New-Item -ItemType Directory -Path $targetParent -Force | Out-Null
            }
            Copy-Item -Path $f.FullName -Destination $target -Force
        }
        $script:copyCount++
    }
}

# Copy a single file, overwriting. Optionally transform the content.
function Sync-File {
    param(
        [Parameter(Mandatory)] [string]$Source,
        [Parameter(Mandatory)] [string]$Dest,
        [Parameter(Mandatory)] [string]$Label,
        [scriptblock]$Transform
    )
    if (-not (Test-Path $Source)) {
        Write-Host "  [skip] source missing: $Source" -ForegroundColor Yellow
        return
    }
    Write-Host "== $Label ==" -ForegroundColor Cyan
    $exists = Test-Path $Dest
    $verb = if ($exists) { 'overwrite' } else { 'create   ' }
    Write-Host "   [$verb] $Source  ->  $Dest"

    if (-not $DryRun) {
        $destParent = Split-Path -Parent $Dest
        if (-not (Test-Path $destParent)) {
            New-Item -ItemType Directory -Path $destParent -Force | Out-Null
        }
        if ($Transform) {
            $content = Get-Content $Source
            & $Transform $content | Set-Content -Path $Dest -Encoding UTF8
        } else {
            Copy-Item -Path $Source -Destination $Dest -Force
        }
    }
    $script:copyCount++
}

# ---- Run --------------------------------------------------------------------

$levelName = Get-LevelName
Write-Host ""
Write-Host "sync_server.ps1  (level-name = '$levelName')$(if ($DryRun) { '   [DRY RUN]' })" -ForegroundColor Green
Write-Host "Repo:   $RepoRoot"
Write-Host "Server: $ServerDir"
Write-Host ""

# 1. Server mod configs -> server/config/
Sync-Tree -Label 'mod configs' `
    -SourceDir (Join-Path $RepoRoot 'mod_configs_server') `
    -DestDir   (Join-Path $ServerDir 'config')

# 2. Datapacks -> server/<level-name>/datapacks/
Sync-Tree -Label 'datapacks' `
    -SourceDir (Join-Path $RepoRoot 'datapacks') `
    -DestDir   (Join-Path $ServerDir "$levelName/datapacks")

# 3. server.properties
Sync-File -Label 'server.properties' `
    -Source (Join-Path $RepoRoot 'server_configuration/server.properties') `
    -Dest   (Join-Path $ServerDir 'server.properties')

# 4. JVM flags -> user_jvm_args.txt (strip comment/blank lines)
Sync-File -Label 'JVM flags' `
    -Source (Join-Path $RepoRoot 'flags/server.txt') `
    -Dest   (Join-Path $ServerDir 'user_jvm_args.txt') `
    -Transform { param($lines) $lines | Where-Object { $_ -notmatch '^\s*#' -and $_.Trim() -ne '' } }

Write-Host ""
if ($DryRun) {
    Write-Host "DRY RUN complete: $($script:copyCount) file(s) would be copied. No changes made." -ForegroundColor Green
} else {
    Write-Host "Sync complete: $($script:copyCount) file(s) copied." -ForegroundColor Green
}
