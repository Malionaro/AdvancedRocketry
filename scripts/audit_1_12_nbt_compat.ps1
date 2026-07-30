$ErrorActionPreference = 'Stop'

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$legacyReference = 'origin/1.12'

function Get-LegacyText {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    $text = (& git -C $repositoryRoot show "${legacyReference}:$Path") -join "`n"
    if ($LASTEXITCODE -ne 0) {
        throw "Unable to read $Path from $legacyReference."
    }
    return $text
}

function Assert-Contains {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Description,

        [Parameter(Mandatory = $true)]
        [string]$Text,

        [Parameter(Mandatory = $true)]
        [string[]]$Needles
    )

    foreach ($needle in $Needles) {
        if (-not $Text.Contains($needle)) {
            throw "$Description is missing required compatibility marker: $needle"
        }
    }
}

$legacyChecks = @(
    @{
        Path = 'src/main/java/zmaster587/advancedRocketry/api/satellite/SatelliteBase.java'
        Needles = @('setInteger("dimId"')
    },
    @{
        Path = 'src/main/java/zmaster587/advancedRocketry/dimension/DimensionProperties.java'
        Needles = @(
            'getIntArray("biomesTerra")',
            'getCompoundTag("satallites")',
            'getIntArray("biomes")',
            'getIntArray("weights")',
            'getIntArray("craterBiomes")',
            'getIntArray("craterWeights")',
            'getIntArray("childrenPlanets")',
            'getInteger("parentPlanet")',
            'getInteger("starId")'
        )
    },
    @{
        Path = 'src/main/java/zmaster587/advancedRocketry/entity/EntityRocket.java'
        Needles = @(
            'getInteger("destinationDimId")',
            'getInteger("lastDimensionFrom")',
            'getCompoundTag("satallite")'
        )
    },
    @{
        Path = 'src/main/java/zmaster587/advancedRocketry/mission/MissionResourceCollection.java'
        Needles = @('getInteger("startDimid")', 'getInteger("launchDim")')
    },
    @{
        Path = 'src/main/java/zmaster587/advancedRocketry/util/SpacePosition.java'
        Needles = @('getInteger("star")', 'getInteger("world")')
    },
    @{
        Path = 'src/main/java/zmaster587/advancedRocketry/item/ItemPlanetIdentificationChip.java'
        Needles = @('getInteger(dimensionIdIdentifier)')
    },
    @{
        Path = 'src/main/java/zmaster587/advancedRocketry/item/ItemStationChip.java'
        Needles = @('getInteger(uuidIdentifier)')
    }
)

foreach ($check in $legacyChecks) {
    Assert-Contains `
        -Description "Legacy source $($check.Path)" `
        -Text (Get-LegacyText -Path $check.Path) `
        -Needles $check.Needles
}

$currentChecks = @(
    @{
        Path = 'src/main/java/zmaster587/advancedRocketry/util/LegacyDimensionIdMigration.java'
        Needles = @(
            'NBT.TAG_ANY_NUMERIC',
            'Integer.MIN_VALUE',
            'Integer.MIN_VALUE + 1',
            'DimensionManager.spaceId',
            'Dimension.THE_NETHER',
            'Dimension.OVERWORLD',
            'Dimension.THE_END',
            'Constants.PLANET_NAMESPACE',
            'Constants.STAR_NAMESPACE',
            'SpaceObjectManager.STATION_NAMESPACE'
        )
    },
    @{
        Path = 'src/main/java/zmaster587/advancedRocketry/dimension/DimensionProperties.java'
        Needles = @(
            'NBT.TAG_INT_ARRAY',
            '"satallites"',
            '"craterBiomes"',
            '"craterWeights"',
            'LegacyDimensionIdMigration.fromLegacyId',
            'LegacyDimensionIdMigration.fromLegacyStarId'
        )
    },
    @{
        Path = 'src/main/java/zmaster587/advancedRocketry/entity/EntityRocket.java'
        Needles = @(
            'LegacyDimensionIdMigration.read(nbt, "destinationDimId")',
            'LegacyDimensionIdMigration.read(nbt, "lastDimensionFrom")',
            'nbt.contains("satallite")'
        )
    },
    @{
        Path = 'src/main/java/zmaster587/advancedRocketry/api/SatelliteRegistry.java'
        Needles = @('nbt.contains("dataType")', 'nbt.getString("DataType")')
    },
    @{
        Path = 'src/main/java/zmaster587/advancedRocketry/mission/MissionResourceCollection.java'
        Needles = @(
            'LegacyDimensionIdMigration.read(nbt, "startDimid")',
            'LegacyDimensionIdMigration.read(nbt, "launchDim")'
        )
    },
    @{
        Path = 'src/main/java/zmaster587/advancedRocketry/util/SpacePosition.java'
        Needles = @(
            'LegacyDimensionIdMigration.fromLegacyStarId',
            'LegacyDimensionIdMigration.read(subTag, "world")'
        )
    },
    @{
        Path = 'src/main/java/zmaster587/advancedRocketry/item/ItemPlanetChip.java'
        Needles = @('LegacyDimensionIdMigration.read(stack.getTag(), dimensionIdIdentifier)')
    },
    @{
        Path = 'src/main/java/zmaster587/advancedRocketry/item/ItemSatelliteChip.java'
        Needles = @('LegacyDimensionIdMigration.read(nbt, "dimId")')
    },
    @{
        Path = 'src/main/java/zmaster587/advancedRocketry/item/ItemStationChip.java'
        Needles = @(
            'ResourceLocation.tryCreate(nbt.getString(uuidIdentifier))',
            'LegacyDimensionIdMigration.fromLegacyStationId'
        )
    },
    @{
        Path = 'src/main/java/zmaster587/advancedRocketry/stations/SpaceStationObject.java'
        Needles = @(
            'LegacyDimensionIdMigration.read(nbt, "destinationDimId")',
            'LegacyDimensionIdMigration.fromLegacyStationId',
            'nbt.getIntArray("knownPlanets")'
        )
    }
)

foreach ($check in $currentChecks) {
    $path = Join-Path $repositoryRoot $check.Path
    Assert-Contains `
        -Description "Current source $($check.Path)" `
        -Text (Get-Content -LiteralPath $path -Raw) `
        -Needles $check.Needles
}

Write-Host '1.12 NBT compatibility source audit passed:'
Write-Host "  legacy format contracts checked: $($legacyChecks.Count)"
Write-Host "  current migration readers checked: $($currentChecks.Count)"
