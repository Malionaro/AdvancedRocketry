$ErrorActionPreference = 'Stop'

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$blocksSource = Join-Path $repositoryRoot 'src/main/java/zmaster587/advancedRocketry/api/AdvancedRocketryBlocks.java'
$itemsSource = Join-Path $repositoryRoot 'src/main/java/zmaster587/advancedRocketry/api/AdvancedRocketryItems.java'
$lootDirectory = Join-Path $repositoryRoot 'src/main/resources/data/advancedrocketry/loot_tables/blocks'
$blocksWithoutLoot = @('lightsource', 'rocketfire')
$generatedMaterialBlocks = @('blocktitaniumaluminide', 'blocktitaniumiridium')

function Get-RegistryNames {
    param([string]$Source, [string]$Pattern)
    $text = Get-Content $Source -Raw
    return @(
        [regex]::Matches($text, $Pattern) |
            ForEach-Object { $_.Groups[1].Value } |
            Sort-Object -Unique
    )
}

function Get-LootItemNames {
    param([AllowNull()]$Node)
    if ($null -eq $Node) { return }
    if ($Node -is [System.Collections.IDictionary]) {
        if ($Node.Contains('type') -and $Node.Contains('name') -and
            $Node.type -eq 'minecraft:item' -and
            $Node.name -like 'advancedrocketry:*') {
            $Node.name.Substring('advancedrocketry:'.Length)
        }
        foreach ($value in $Node.Values) { Get-LootItemNames $value }
        return
    }
    if ($Node -is [System.Collections.IEnumerable] -and $Node -isnot [string]) {
        foreach ($value in $Node) { Get-LootItemNames $value }
    }
}

$blockRegistryNames = Get-RegistryNames $blocksSource `
    'AdvancedRocketryBlocks\.[A-Za-z0-9_]+\s*\.setRegistryName\("([^"]+)"\)'
$itemRegistryNames = Get-RegistryNames $itemsSource '\.setRegistryName\("([^"]+)"\)'
$lootFiles = @(Get-ChildItem $lootDirectory -Filter '*.json' -File)
$lootNames = @($lootFiles | ForEach-Object BaseName | Sort-Object -Unique)
$expectedLootNames = @(
    $blockRegistryNames | Where-Object { $_ -notin $blocksWithoutLoot }
    $generatedMaterialBlocks
) | Sort-Object -Unique

$missingLoot = @(Compare-Object $expectedLootNames $lootNames |
    Where-Object SideIndicator -eq '<=' | ForEach-Object InputObject)
$unexpectedLoot = @(Compare-Object $expectedLootNames $lootNames |
    Where-Object SideIndicator -eq '=>' | ForEach-Object InputObject)

$lootItemNames = foreach ($lootFile in $lootFiles) {
    try {
        $loot = Get-Content $lootFile.FullName -Raw | ConvertFrom-Json -AsHashtable
    }
    catch {
        throw "Invalid loot table JSON: $($lootFile.FullName): $($_.Exception.Message)"
    }
    Get-LootItemNames $loot
}
$lootItemNames = @($lootItemNames | Sort-Object -Unique)
$knownItemNames = @($itemRegistryNames + $generatedMaterialBlocks | Sort-Object -Unique)
$unknownLootItems = @(Compare-Object $knownItemNames $lootItemNames |
    Where-Object SideIndicator -eq '=>' | ForEach-Object InputObject)

if ($missingLoot.Count) { throw "Blocks without a loot table: $($missingLoot -join ', ')" }
if ($unexpectedLoot.Count) { throw "Loot tables without a registered block: $($unexpectedLoot -join ', ')" }
if ($unknownLootItems.Count) { throw "Loot tables reference unknown Advanced Rocketry items: $($unknownLootItems -join ', ')" }

Write-Host 'Block loot audit passed:'
Write-Host "  registered Advanced Rocketry blocks: $($blockRegistryNames.Count)"
Write-Host "  intentional no-drop blocks: $($blocksWithoutLoot.Count)"
Write-Host "  generated material blocks: $($generatedMaterialBlocks.Count)"
Write-Host "  valid block loot tables: $($lootFiles.Count)"
Write-Host "  referenced Advanced Rocketry item IDs: $($lootItemNames.Count)"
