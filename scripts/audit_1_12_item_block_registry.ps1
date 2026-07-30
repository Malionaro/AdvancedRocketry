$ErrorActionPreference = 'Stop'

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$legacyReference = 'origin/1.12'
$legacyMainPath = 'src/main/java/zmaster587/advancedRocketry/AdvancedRocketry.java'
$currentBlocksPath = Join-Path $repositoryRoot 'src/main/java/zmaster587/advancedRocketry/api/AdvancedRocketryBlocks.java'
$currentItemsPath = Join-Path $repositoryRoot 'src/main/java/zmaster587/advancedRocketry/api/AdvancedRocketryItems.java'
$mappingPath = Join-Path $repositoryRoot 'src/main/java/zmaster587/advancedRocketry/util/LegacyRegistryMappings.java'
$migrationPath = Join-Path $repositoryRoot 'src/main/java/zmaster587/advancedRocketry/util/LegacyItemStackMigration.java'

function Get-Matches {
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
    if ($missing.Count -gt 0) {
        throw "Uncovered $Description IDs: $($missing -join ', ')"
    }
}

$legacyText = (& git -C $repositoryRoot show "${legacyReference}:$legacyMainPath") -join "`n"
if ($LASTEXITCODE -ne 0) {
    throw "Unable to read $legacyMainPath from $legacyReference."
}

# Do not treat the three explicitly commented-out cable registrations as old
# registry entries.
$legacyText = [regex]::Replace($legacyText, '(?m)^\s*//.*(?:\r?\n|$)', '')

$legacyItems = Get-Matches `
    -Text $legacyText `
    -Pattern 'LibVulpesBlocks\.registerItem\([^;]*?setRegistryName\("([^"]+)"\)'
$legacyBlocks = Get-Matches `
    -Text $legacyText `
    -Pattern 'LibVulpesBlocks\.registerBlock\([^;]*?setRegistryName\("([^"]+)"\)'

# These 1.12 registerBlock calls explicitly passed a null ItemBlock factory.
$legacyBlocksWithoutItems = @(
    'enrichedlavafluid',
    'hydrogenfluid',
    'nitrogenfluid',
    'oxygenfluid',
    'quartzcrucible',
    'rocketfire',
    'rocketfuel'
)
$legacyBlockItems = @(
    $legacyBlocks | Where-Object { $_ -notin $legacyBlocksWithoutItems }
)

$currentBlocksText = Get-Content $currentBlocksPath -Raw
$currentItemsText = Get-Content $currentItemsPath -Raw
$currentBlocks = Get-Matches `
    -Text $currentBlocksText `
    -Pattern '\.setRegistryName\("([^"]+)"\)'
$currentItems = Get-Matches `
    -Text $currentItemsText `
    -Pattern '\.setRegistryName\("([^"]+)"\)'

$mappingText = Get-Content $mappingPath -Raw
$blockMappingStart = $mappingText.IndexOf('private static Block blockTarget')
$itemMappingStart = $mappingText.IndexOf('private static Item itemTarget')
if ($blockMappingStart -lt 0 -or $itemMappingStart -lt 0) {
    throw 'Unable to locate block/item mapping sections.'
}
$blockMappingText = $mappingText.Substring(
    $blockMappingStart,
    $itemMappingStart - $blockMappingStart
)
$itemMappingText = $mappingText.Substring($itemMappingStart)
$blockAliases = Get-Matches -Text $blockMappingText -Pattern 'case "([^"]+)"'
$itemAliases = Get-Matches -Text $itemMappingText -Pattern 'case "([^"]+)"'

$migrationText = Get-Content $migrationPath -Raw
$metadataContainers = Get-Matches `
    -Text $migrationText `
    -Pattern 'targets\.put\("([^"]+)"'

Assert-Covered `
    -Description '1.12 block registry' `
    -Expected $legacyBlocks `
    -Covered @($currentBlocks + $blockAliases)
Assert-Covered `
    -Description '1.12 explicit item registry' `
    -Expected $legacyItems `
    -Covered @($currentItems + $itemAliases + $metadataContainers)
Assert-Covered `
    -Description '1.12 generated BlockItem registry' `
    -Expected $legacyBlockItems `
    -Covered @($currentItems + $itemAliases + $metadataContainers)

Write-Host '1.12 item/block registry audit passed:'
Write-Host "  legacy block IDs: $($legacyBlocks.Count)"
Write-Host "  legacy explicit item IDs: $($legacyItems.Count)"
Write-Host "  legacy generated BlockItem IDs: $($legacyBlockItems.Count)"
Write-Host "  current block IDs: $($currentBlocks.Count)"
Write-Host "  current item IDs: $($currentItems.Count)"
Write-Host "  block aliases: $($blockAliases.Count)"
Write-Host "  item aliases: $($itemAliases.Count)"
Write-Host "  metadata-container migrations: $($metadataContainers.Count)"
