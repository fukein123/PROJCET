#!/usr/bin/env pwsh
[CmdletBinding(PositionalBinding = $false)]
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateSet('request', 'status', 'list', 'approve', 'needs-revision', 'delete')]
    [string]$Action,

    [string]$Id,
    [string]$Title,
    [string]$FilePath,
    [string]$Category = 'spec',
    [string]$CategoryName = 'general',
    [string]$Comment = '',
    [switch]$Json
)

$ErrorActionPreference = 'Stop'

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$workflowRoot = Join-Path $projectRoot '.approval-workflow'
$requestsRoot = Join-Path $workflowRoot 'requests'
$archiveRoot = Join-Path $workflowRoot 'archive'

function Ensure-Directory {
    param([string]$Path)

    if (-not (Test-Path $Path)) {
        New-Item -ItemType Directory -Path $Path | Out-Null
    }
}

function Get-RelativeProjectPath {
    param([string]$Path)

    $fullPath = [System.IO.Path]::GetFullPath($Path)
    if (-not $fullPath.StartsWith($projectRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Path '$Path' is outside project root '$projectRoot'."
    }

    return $fullPath.Substring($projectRoot.Length).TrimStart('\', '/').Replace('\', '/')
}

function New-ApprovalId {
    $timestamp = Get-Date -Format 'yyyyMMddHHmmssfff'
    $alphabet = @()
    $alphabet += 48..57
    $alphabet += 97..122
    $suffix = -join ($alphabet | Get-Random -Count 8 | ForEach-Object { [char]$_ })
    return "local_approval_${timestamp}_${suffix}"
}

function Write-Result {
    param([object]$Value)

    if ($Json) {
        $Value | ConvertTo-Json -Depth 10
        return
    }

    $Value
}

function Save-Approval {
    param(
        [object]$Approval,
        [string]$Path
    )

    $Approval | ConvertTo-Json -Depth 10 | Set-Content -Path $Path -Encoding UTF8
}

function Load-Approval {
    param([string]$Path)

    return Get-Content -Path $Path -Encoding UTF8 | ConvertFrom-Json
}

function Find-ApprovalPath {
    param([string]$ApprovalId)

    $match = Get-ChildItem -Path $requestsRoot -Recurse -Filter "${ApprovalId}.json" -File -ErrorAction SilentlyContinue |
        Select-Object -First 1

    if ($null -ne $match) {
        return $match.FullName
    }

    $match = Get-ChildItem -Path $archiveRoot -Recurse -Filter "${ApprovalId}.json" -File -ErrorAction SilentlyContinue |
        Select-Object -First 1

    if ($null -ne $match) {
        return $match.FullName
    }

    throw "Approval '$ApprovalId' was not found."
}

Ensure-Directory $workflowRoot
Ensure-Directory $requestsRoot
Ensure-Directory $archiveRoot

switch ($Action) {
    'request' {
        if ([string]::IsNullOrWhiteSpace($Title)) {
            throw "Title is required for 'request'."
        }

        if ([string]::IsNullOrWhiteSpace($FilePath)) {
            throw "FilePath is required for 'request'."
        }

        $documentPath = Join-Path $projectRoot $FilePath
        if (-not (Test-Path $documentPath)) {
            throw "Document '$FilePath' does not exist."
        }

        $approvalId = New-ApprovalId
        $requestDir = Join-Path $requestsRoot $CategoryName
        $snapshotDir = Join-Path $requestDir '.snapshots'
        $snapshotRequestDir = Join-Path $snapshotDir $approvalId
        Ensure-Directory $requestDir
        Ensure-Directory $snapshotRequestDir

        $snapshotTarget = Join-Path $snapshotRequestDir ([System.IO.Path]::GetFileName($documentPath))
        Copy-Item -Path $documentPath -Destination $snapshotTarget -Force

        $approval = [ordered]@{
            id = $approvalId
            title = $Title
            filePath = Get-RelativeProjectPath $documentPath
            snapshotPath = Get-RelativeProjectPath $snapshotTarget
            type = 'document'
            status = 'pending'
            createdAt = (Get-Date).ToString('o')
            respondedAt = $null
            response = $null
            comments = @()
            category = $Category
            categoryName = $CategoryName
            approvalMode = 'chat'
            canProceed = $false
            mustWait = $true
        }

        $approvalPath = Join-Path $requestDir "${approvalId}.json"
        Save-Approval -Approval $approval -Path $approvalPath

        Write-Result ([ordered]@{
            success = $true
            action = 'request'
            approvalId = $approvalId
            status = 'pending'
            filePath = $approval.filePath
            title = $Title
            note = "Reply with '通过' to approve, or '需修改：<comment>' to request changes."
        })
    }

    'status' {
        if ([string]::IsNullOrWhiteSpace($Id)) {
            throw "Id is required for 'status'."
        }

        $approval = Load-Approval (Find-ApprovalPath $Id)
        $status = [string]$approval.status
        $approval.canProceed = $status -eq 'approved'
        $approval.mustWait = $status -eq 'pending'
        Write-Result $approval
    }

    'list' {
        $items = Get-ChildItem -Path $requestsRoot -Recurse -Filter '*.json' -File -ErrorAction SilentlyContinue |
            Sort-Object LastWriteTime -Descending |
            ForEach-Object {
                $approval = Load-Approval $_.FullName
                [ordered]@{
                    id = $approval.id
                    title = $approval.title
                    status = $approval.status
                    filePath = $approval.filePath
                    updatedAt = if ($null -ne $approval.respondedAt) { $approval.respondedAt } else { $approval.createdAt }
                }
            }

        Write-Result $items
    }

    'approve' {
        if ([string]::IsNullOrWhiteSpace($Id)) {
            throw "Id is required for 'approve'."
        }

        $approvalPath = Find-ApprovalPath $Id
        $approval = Load-Approval $approvalPath
        $approval.status = 'approved'
        $approval.respondedAt = (Get-Date).ToString('o')
        $approval.response = 'approved'
        $approval.canProceed = $true
        $approval.mustWait = $false

        if (-not [string]::IsNullOrWhiteSpace($Comment)) {
            $entry = [ordered]@{
                at = (Get-Date).ToString('o')
                type = 'approval'
                text = $Comment
            }
            $approval.comments = @($approval.comments) + @($entry)
        }

        Save-Approval -Approval $approval -Path $approvalPath
        Write-Result $approval
    }

    'needs-revision' {
        if ([string]::IsNullOrWhiteSpace($Id)) {
            throw "Id is required for 'needs-revision'."
        }

        $approvalPath = Find-ApprovalPath $Id
        $approval = Load-Approval $approvalPath
        $approval.status = 'needs-revision'
        $approval.respondedAt = (Get-Date).ToString('o')
        $approval.response = 'needs-revision'
        $approval.canProceed = $false
        $approval.mustWait = $false

        $entry = [ordered]@{
            at = (Get-Date).ToString('o')
            type = 'revision'
            text = $Comment
        }
        $approval.comments = @($approval.comments) + @($entry)

        Save-Approval -Approval $approval -Path $approvalPath
        Write-Result $approval
    }

    'delete' {
        if ([string]::IsNullOrWhiteSpace($Id)) {
            throw "Id is required for 'delete'."
        }

        $approvalPath = Find-ApprovalPath $Id
        if ($approvalPath.StartsWith($archiveRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
            Write-Result ([ordered]@{
                success = $true
                action = 'delete'
                approvalId = $Id
                note = 'Approval already archived.'
            })
            return
        }

        $approval = Load-Approval $approvalPath
        $archiveDir = Join-Path $archiveRoot $approval.categoryName
        Ensure-Directory $archiveDir
        $archivePath = Join-Path $archiveDir ([System.IO.Path]::GetFileName($approvalPath))
        Move-Item -Path $approvalPath -Destination $archivePath -Force

        Write-Result ([ordered]@{
            success = $true
            action = 'delete'
            approvalId = $Id
            archivedPath = Get-RelativeProjectPath $archivePath
        })
    }
}
