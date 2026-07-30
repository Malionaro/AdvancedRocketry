param(
    [string]$LegacyRef = "origin/1.12"
)

$ErrorActionPreference = "Stop"

$legacyAliases = @{
    advbipropellantrocketmotor = "advancedbipropellantrocketengine"
    advrocketmotor              = "advancedmonopropellantrocketengine"
    alienleaves                 = "lightwoodleaves"
    aliensapling                = "lightwoodsapling"
    alienwood                   = "lightwoodlog"
    atmanalyser                 = "atmosphereanalyzer"
    beaconfinder                = "beaconfinderupgrade"
    bipropellantrocketmotor     = "bipropellantrocketengine"
    blockpump                   = "pump"
    circlelight                 = "stationlight"
    deployablerocketbuilder     = "unmannedrocketassembler"
    electrolyser                = "electrolyzer"
    fueltank                    = "monopropellantfueltank"
    gravitymachine              = "areagravitycontroller"
    iquartzcrucible             = "quartzcrucible"
    microwavereciever           = "microwavereceiver"
    monitoringstation           = "rocketcontrolcenter"
    nuclearfueltank             = "nuclearworkingfluidtank"
    nuclearrocketmotor          = "nuclearrocketengine"
    oxygencharger               = "gaschargepad"
    oxygendetection             = "atmospheredetector"
    pipesealer                  = "seal"
    planetanalyser              = "astrobodydataprocessor"
    planetholoselector          = "holographicplanetselector"
    planetidchip                = "planetchip"
    planks                      = "lightwoodplanks"
    platepress                  = "smallplatepress"
    precisionassemblingmachine  = "precisionassembler"
    rocketbuilder               = "rocketassembler"
    rocketmotor                 = "monopropellantrocketengine"
    satellitebuilder            = "satelliteassembler"
    satelliteidchip             = "satellitechip"
    spaceboots                  = "spacesuitboots"
    spacechestplate             = "spacesuitchestpiece"
    spaceelevatorcontroller     = "spaceelevator"
    spacehelmet                 = "spacesuithelmet"
    spaceleggings               = "spacesuitleggings"
    spacelaser                  = "orbitallaserdrill"
    stationbuilder              = "spacestationassembler"
    stationmarker               = "stationdockingport"
    vacuumlaser                 = "laser"
    warpmonitor                 = "warpcontroller"
    wirelesstransciever         = "wirelesstransceiver"
}

$legacyMaterials = @(
    "dilithium",
    "iron",
    "gold",
    "silicon",
    "copper",
    "tin",
    "steel",
    "titanium",
    "rutile",
    "aluminum",
    "iridium"
)

function Get-LegacyVariant {
    param(
        [string]$Path,
        [int]$Metadata
    )

    $variants = switch ($Path) {
        "ic" {
            @("basiccircuit", "trackingcircuit", "advancedcircuit",
                "controlcircuit", "itemiocircuit", "fluidiocircuit")
        }
        "itemcircuitplate" {
            @("basiccircuitwafer", "advancedcircuitwafer")
        }
        "itemupgrade" {
            @("hoverupgrade", "flightspeedupgrade", "bioniclegsupgrade",
                "paddedbootsupgrade", "antifogvisorupgrade",
                "earthbrightvisorupgrade")
        }
        "loader" {
            @("databus", "satellitebay", "rocketunloader", "rocketloader",
                "rocketfluidunloader", "rocketfluidloader",
                "guidancecomputeraccesshatch")
        }
        "misc" {
            @("userinterface", "carbonbrick")
        }
        "pressuretank" {
            @("ironpressuretank", "steelpressuretank",
                "aluminumpressuretank", "titaniumpressuretank")
        }
        "satellitepowersource" {
            @("basicsolarpanel", "largesolarpanel")
        }
        "satelliteprimaryfunction" {
            @("opticalsensor", "compositionsensor", "masssensor",
                "microwavetransmitter", "oresensor", "biomechangercomponent")
        }
        default {
            $null
        }
    }

    if ($null -eq $variants -or $Metadata -lt 0 -or $Metadata -ge $variants.Count) {
        return $null
    }
    return $variants[$Metadata]
}

function Get-JsonValue {
    param(
        $Object,
        [string]$Key
    )

    if ($null -eq $Object) {
        return $null
    }

    if ($Object -is [System.Collections.IDictionary]) {
        if ($Object.Contains($Key)) {
            return $Object[$Key]
        }
        return $null
    }

    $property = $Object.PSObject.Properties[$Key]
    if ($null -ne $property) {
        return $property.Value
    }
    return $null
}

function Convert-LegacyItemId {
    param(
        [string]$ItemId,
        [int]$Metadata
    )

    $namespace, $path = $ItemId -split ":", 2

    if ($namespace -eq "advancedrocketry") {
        if ($path -eq "wafer") {
            return "advancedrocketry:siliconwafer"
        }
        if ($path -eq "crystal" -and $Metadata -eq 3) {
            return "advancedrocketry:crystal_red"
        }
        $variant = Get-LegacyVariant -Path $path -Metadata $Metadata
        if ($null -ne $variant) {
            return "advancedrocketry:$variant"
        }
        if ($legacyAliases.ContainsKey($path)) {
            return "advancedrocketry:$($legacyAliases[$path])"
        }
        return $ItemId
    }

    if ($namespace -eq "libvulpes") {
        $libVulpesAliases = @{
            advstructuremachine = "advancedmachinestructure"
            forgepowerinput      = "forge_power_input"
            forgepoweroutput     = "forge_power_output"
            structuremachine     = "machinestructure"
        }
        if ($libVulpesAliases.ContainsKey($path)) {
            return "libvulpes:$($libVulpesAliases[$path])"
        }
        if ($path -eq "holoprojector") {
            return "libvulpes:holo_projector"
        }
        if ($path -eq "hatch") {
            $hatches = @(
                "itemihatch", "itemohatch", "fluidihatch", "fluidohatch",
                "itemihatch", "itemohatch", "fluidihatch", "fluidohatch",
                "itemihatch", "itemohatch", "fluidihatch", "fluidohatch"
            )
            if ($Metadata -ge 0 -and $Metadata -lt $hatches.Count) {
                return "libvulpes:$($hatches[$Metadata])"
            }
        }

        $productKinds = @{
            productboule  = "boule"
            productdust   = "dust"
            productfan    = "fan"
            productgear   = "gear"
            productgem    = "gem"
            productingot  = "ingot"
            productnugget = "nugget"
            productplate  = "plate"
            productrod    = "rod"
            productsheet  = "sheet"
            productstick  = "rod"
        }
        if ($productKinds.ContainsKey($path) -and
            $Metadata -ge 0 -and $Metadata -lt $legacyMaterials.Count) {
            return "libvulpes:$($productKinds[$path])$($legacyMaterials[$Metadata])"
        }
    }

    return $ItemId
}

function Convert-LegacyOreName {
    param([string]$OreName)

    $specialNames = @{
        blockMotor                = "tag:forge:motors"
        itemBattery               = "item:libvulpes:battery"
        lensPrecisionLaserEtcher  = "tag:forge:lenses/precision_laser_etcher"
        paneGlass                 = "tag:forge:glass_panes"
        paneGlassColorless        = "tag:forge:glass_panes/colorless"
        slabWood                  = "tag:minecraft:wooden_slabs"
    }
    if ($specialNames.ContainsKey($OreName)) {
        return $specialNames[$OreName]
    }

    if ($OreName -match "^(block|boule|coil|dust|dye|fan|gear|gem|ingot|nugget|ore|plate|rod|sheet|stick)(.+)$") {
        $category = $Matches[1]
        $material = $Matches[2].ToLowerInvariant()
        if ($category -eq "stick") {
            $category = "rod"
        }
        $pluralCategories = @{
            block  = "storage_blocks"
            boule  = "boules"
            coil   = "coils"
            dust   = "dusts"
            dye    = "dyes"
            fan    = "fans"
            gear   = "gears"
            gem    = "gems"
            ingot  = "ingots"
            nugget = "nuggets"
            ore    = "ores"
            plate  = "plates"
            rod    = "rods"
            sheet  = "sheets"
        }
        return "tag:forge:$($pluralCategories[$category])/$material"
    }

    return "ore:$($OreName.ToLowerInvariant())"
}

function Convert-Ingredient {
    param(
        $Ingredient,
        [bool]$IsLegacy
    )

    if ($Ingredient -is [System.Array]) {
        $alternatives = @(
            foreach ($entry in $Ingredient) {
                Convert-Ingredient -Ingredient $entry -IsLegacy $IsLegacy
            }
        ) | Sort-Object
        return "any:[$($alternatives -join "|")]"
    }

    $type = Get-JsonValue -Object $Ingredient -Key "type"
    $item = Get-JsonValue -Object $Ingredient -Key "item"
    $tag = Get-JsonValue -Object $Ingredient -Key "tag"
    $metadata = Get-JsonValue -Object $Ingredient -Key "data"

    if ($IsLegacy -and $type -eq "forge:ore_dict") {
        return Convert-LegacyOreName -OreName (Get-JsonValue -Object $Ingredient -Key "ore")
    }
    if ($null -ne $item) {
        if ($IsLegacy -and $item -eq "advancedrocketry:pressuretank" -and [int]$metadata -eq 32767) {
            return "tag:advancedrocketry:pressure_tanks"
        }
        if ($IsLegacy -and $item -eq "minecraft:coal" -and [int]$metadata -eq 1) {
            return "item:minecraft:charcoal"
        }
        if ($IsLegacy -and $item -eq "minecraft:dye" -and [int]$metadata -eq 15) {
            return "item:minecraft:bone_meal"
        }
        if ($IsLegacy -and $item -eq "minecraft:wool" -and [int]$metadata -eq 32767) {
            return "tag:minecraft:wool"
        }
        $itemId = if ($IsLegacy) {
            Convert-LegacyItemId -ItemId $item -Metadata ([int]$metadata)
        } else {
            $item
        }
        $renamedVanillaItems = @{
            "minecraft:brick_block" = "minecraft:bricks"
            "minecraft:netherbrick" = "minecraft:nether_brick"
        }
        if ($renamedVanillaItems.ContainsKey($itemId)) {
            $itemId = $renamedVanillaItems[$itemId]
        }
        return "item:$itemId"
    }
    if ($null -ne $tag) {
        return "tag:$tag"
    }

    return "unknown:$($Ingredient | ConvertTo-Json -Compress -Depth 8)"
}

function Get-RecipeSignature {
    param(
        $Recipe,
        [bool]$IsLegacy
    )

    $recipeType = Get-JsonValue -Object $Recipe -Key "type"
    if ($recipeType -eq "minecraft:crafting_shaped") {
        $keyValues = @{}
        foreach ($entry in (Get-JsonValue -Object $Recipe -Key "key").GetEnumerator()) {
            $keyValues[[char]$entry.Key] = Convert-Ingredient `
                -Ingredient $entry.Value `
                -IsLegacy $IsLegacy
        }

        $pattern = @(
            foreach ($row in (Get-JsonValue -Object $Recipe -Key "pattern")) {
                [string]$row
            }
        )
        while ($pattern.Count -gt 0 -and $pattern[0].Trim().Length -eq 0) {
            $pattern = @($pattern | Select-Object -Skip 1)
        }
        while ($pattern.Count -gt 0 -and $pattern[-1].Trim().Length -eq 0) {
            $pattern = @($pattern | Select-Object -First ($pattern.Count - 1))
        }

        if ($pattern.Count -gt 0) {
            $left = 0
            $right = ($pattern | Measure-Object -Property Length -Maximum).Maximum - 1
            while ($left -le $right -and
                (@($pattern | Where-Object { $_.Length -gt $left -and $_[$left] -ne " " }).Count -eq 0)) {
                $left++
            }
            while ($right -ge $left -and
                (@($pattern | Where-Object { $_.Length -gt $right -and $_[$right] -ne " " }).Count -eq 0)) {
                $right--
            }

            $grid = @(
                foreach ($row in $pattern) {
                    $cells = @(
                        for ($column = $left; $column -le $right; $column++) {
                            $symbol = if ($column -lt $row.Length) { $row[$column] } else { " " }
                            if ($symbol -eq " ") {
                                "_"
                            } else {
                                $keyValues[$symbol]
                            }
                        }
                    )
                    $cells -join "|"
                }
            )
        } else {
            $grid = @()
        }
        return "shaped grid=$($grid -join "/")"
    }

    if ($recipeType -eq "minecraft:crafting_shapeless") {
        $ingredients = @(
            foreach ($ingredient in (Get-JsonValue -Object $Recipe -Key "ingredients")) {
                Convert-Ingredient -Ingredient $ingredient -IsLegacy $IsLegacy
            }
        ) | Sort-Object
        return "shapeless ingredients=$($ingredients -join ",")"
    }

    return $recipeType
}

function Get-ResultCount {
    param($Recipe)

    $result = Get-JsonValue -Object $Recipe -Key "result"
    $count = Get-JsonValue -Object $result -Key "count"
    if ($null -eq $count) {
        return 1
    }
    return [int]$count
}

function Read-JsonFile {
    param([string]$Path)

    return Get-Content -Raw -LiteralPath $Path | ConvertFrom-Json -AsHashtable
}

$currentByResult = @{}
$currentFiles = @(
    Get-ChildItem -LiteralPath "src/main/resources/data/advancedrocketry/recipes" -Filter "*.json"
    Get-ChildItem -LiteralPath "libVulpes/src/main/resources/data/libvulpes/recipes" -Filter "*.json" -Recurse
)
foreach ($file in $currentFiles) {
    $recipe = Read-JsonFile -Path $file.FullName
    $result = Get-JsonValue -Object $recipe -Key "result"
    $resultItem = Get-JsonValue -Object $result -Key "item"
    if ($null -eq $resultItem) {
        continue
    }
    if (-not $currentByResult.ContainsKey($resultItem)) {
        $currentByResult[$resultItem] = @{}
    }
    $currentByResult[$resultItem][$file.BaseName] = $recipe
}

$missing = @()
$changed = @()
$comparedCraftingRecipes = 0
$legacyFiles = git ls-tree -r --name-only $LegacyRef |
    Where-Object {
        $_ -match "^src/main/resources/assets/advancedrocketry/recipes/[^/]+\.json$" -and
        $_ -notmatch "_factories\.json$"
    }

foreach ($legacyPath in $legacyFiles) {
    $legacyRecipe = (git show "${LegacyRef}:$legacyPath") -join "`n" |
        ConvertFrom-Json -AsHashtable
    $legacyResult = Get-JsonValue -Object $legacyRecipe -Key "result"
    $legacyResultItem = Get-JsonValue -Object $legacyResult -Key "item"
    if ($null -eq $legacyResultItem) {
        continue
    }
    $comparedCraftingRecipes++

    $legacyName = [IO.Path]::GetFileNameWithoutExtension($legacyPath)
    $targetId = Convert-LegacyItemId `
        -ItemId $legacyResultItem `
        -Metadata ([int](Get-JsonValue -Object $legacyResult -Key "data"))

    if (-not $currentByResult.ContainsKey($targetId)) {
        $missing += [pscustomobject]@{
            LegacyRecipe = $legacyName
            ExpectedItem = $targetId
        }
        continue
    }

    $candidates = $currentByResult[$targetId]
    if ($candidates.Count -eq 1) {
        $currentName = @($candidates.Keys)[0]
    } elseif ($legacyName.EndsWith("2")) {
        $currentName = $candidates.Keys |
            Where-Object { $_ -match "_conv$" } |
            Select-Object -First 1
    } else {
        $currentName = $candidates.Keys |
            Where-Object { $_ -notmatch "_conv$" } |
            Select-Object -First 1
    }

    if ($null -eq $currentName) {
        $missing += [pscustomobject]@{
            LegacyRecipe = $legacyName
            ExpectedItem = "$targetId (ambiguous current recipes)"
        }
        continue
    }

    $currentRecipe = $candidates[$currentName]
    $legacySignature = Get-RecipeSignature -Recipe $legacyRecipe -IsLegacy $true
    $currentSignature = Get-RecipeSignature -Recipe $currentRecipe -IsLegacy $false
    $legacyCount = Get-ResultCount -Recipe $legacyRecipe
    $currentCount = Get-ResultCount -Recipe $currentRecipe

    if ($legacySignature -ne $currentSignature -or $legacyCount -ne $currentCount) {
        $changed += [pscustomobject]@{
            LegacyRecipe    = $legacyName
            CurrentRecipe   = $currentName
            LegacyCount     = $legacyCount
            CurrentCount    = $currentCount
            LegacySignature = $legacySignature
            CurrentSignature = $currentSignature
        }
    }
}

Write-Output "Compared crafting recipes: $comparedCraftingRecipes"
Write-Output "Missing crafting recipes: $($missing.Count)"
$missing | Format-Table -AutoSize
Write-Output "Changed crafting recipes: $($changed.Count)"
$changed | Format-List

if ($missing.Count -gt 0 -or $changed.Count -gt 0) {
    exit 1
}
