$ErrorActionPreference = 'Stop'

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$legacyReference = 'origin/1.12'
$legacyMainPath = 'src/main/java/zmaster587/advancedRocketry/AdvancedRocketry.java'
$entitiesPath = Join-Path $repositoryRoot 'src/main/java/zmaster587/advancedRocketry/api/AdvancedRocketryEntities.java'
$tilesPath = Join-Path $repositoryRoot 'src/main/java/zmaster587/advancedRocketry/api/AdvancedRocketryTileEntityType.java'
$mappingsPath = Join-Path $repositoryRoot 'src/main/java/zmaster587/advancedRocketry/util/LegacyRegistryMappings.java'

function Get-Ids {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Text,

        [Parameter(Mandatory = $true)]
        [string]$Pattern
    )

    return @(
        [regex]::Matches(
            $Text,
            $Pattern,
            [System.Text.RegularExpressions.RegexOptions]::Singleline
        ) |
            ForEach-Object { $_.Groups[1].Value.ToLowerInvariant() } |
            Sort-Object -Unique
    )
}

function Assert-Covered {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Description,

        [Parameter(Mandatory = $true)]
        [string[]]$Expected,

        [Parameter(Mandatory = $true)]
        [string[]]$Covered
    )

    $missing = @(
        Compare-Object ($Covered | Sort-Object -Unique) ($Expected | Sort-Object -Unique) |
            Where-Object SideIndicator -eq '=>' |
            ForEach-Object InputObject
    )
    if ($missing.Count) {
        throw "Uncovered $Description IDs: $($missing -join ', ')"
    }
}

$legacyText = (& git -C $repositoryRoot show "${legacyReference}:$legacyMainPath") -join "`n"
if ($LASTEXITCODE -ne 0) {
    throw "Unable to read $legacyMainPath from $legacyReference."
}

$legacyWithoutLineComments = [regex]::Replace(
    $legacyText,
    '(?m)^\s*//.*(?:\r?\n|$)',
    ''
)
$legacyEntityIds = Get-Ids `
    -Text $legacyWithoutLineComments `
    -Pattern 'registerModEntity\(\s*new ResourceLocation\(\s*Constants\.modId,\s*"([^"]+)"'
$legacyTileIds = Get-Ids `
    -Text $legacyWithoutLineComments `
    -Pattern 'GameRegistry\.registerTileEntity\([^;]+?(?:new ResourceLocation\(\s*Constants\.modId,\s*)?"([^"]+)"\)\s*;'

$currentEntityIds = Get-Ids `
    -Text (Get-Content $entitiesPath -Raw) `
    -Pattern '\.setRegistryName\(\s*Constants\.modId,\s*"([^"]+)"\)'
$currentTileIds = Get-Ids `
    -Text (Get-Content $tilesPath -Raw) `
    -Pattern '\.setRegistryName\("([^"]+)"\)'

$mappingsText = Get-Content $mappingsPath -Raw
$entityStart = $mappingsText.IndexOf('public static void remapEntities')
$tileRemapStart = $mappingsText.IndexOf('public static void remapTileEntities')
$tileTargetStart = $mappingsText.IndexOf('private static TileEntityType')
$blockTargetStart = $mappingsText.IndexOf('private static Block blockTarget')
if ($entityStart -lt 0 -or $tileRemapStart -lt 0 -or
    $tileTargetStart -lt 0 -or $blockTargetStart -lt 0) {
    throw 'Unable to locate entity/tile mapping sections.'
}

$entityAliases = Get-Ids `
    -Text $mappingsText.Substring($entityStart, $tileRemapStart - $entityStart) `
    -Pattern 'case "([^"]+)"'
$tileAliases = Get-Ids `
    -Text $mappingsText.Substring($tileTargetStart, $blockTargetStart - $tileTargetStart) `
    -Pattern 'case "([^"]+)"'

# The 1.12 branch registered these TileEntity classes but commented out all
# three matching block registrations. They therefore had no normally placeable
# block/NBT path and cannot be safely remapped to a current block type.
$deadCableTileIds = @('ardatapipe', 'arenergypipe', 'arliquidpipe')
$commentedCableBlocks = @('blockDataPipe', 'blockEnergyPipe', 'blockFluidPipe')
foreach ($blockField in $commentedCableBlocks) {
    if ($legacyText -notmatch "(?m)^\s*//\s*LibVulpesBlocks\.registerBlock\(AdvancedRocketryBlocks\.$blockField\.") {
        throw "Expected the legacy $blockField registration to remain commented out."
    }
}
foreach ($tileId in $deadCableTileIds) {
    if ($tileId -notin $legacyTileIds) {
        throw "Expected dead cable TileEntity ID is absent from 1.12: $tileId"
    }
}

$normallyReachableTileIds = @(
    $legacyTileIds | Where-Object { $_ -notin $deadCableTileIds }
)

Assert-Covered `
    -Description '1.12 entity registry' `
    -Expected $legacyEntityIds `
    -Covered @($currentEntityIds + $entityAliases)
Assert-Covered `
    -Description 'normally reachable 1.12 TileEntity registry' `
    -Expected $normallyReachableTileIds `
    -Covered @($currentTileIds + $tileAliases)

Write-Host '1.12 entity/TileEntity registry audit passed:'
Write-Host "  legacy entity IDs: $($legacyEntityIds.Count)"
Write-Host "  current entity IDs: $($currentEntityIds.Count)"
Write-Host "  entity aliases: $($entityAliases.Count)"
Write-Host "  legacy TileEntity IDs: $($legacyTileIds.Count)"
Write-Host "  normally reachable legacy TileEntity IDs: $($normallyReachableTileIds.Count)"
Write-Host "  classified dead cable TileEntity IDs: $($deadCableTileIds.Count)"
Write-Host "  current TileEntity IDs: $($currentTileIds.Count)"
Write-Host "  TileEntity aliases: $($tileAliases.Count)"
