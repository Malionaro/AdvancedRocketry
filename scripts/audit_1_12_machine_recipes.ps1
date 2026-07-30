param(
    [string]$LegacyRef = "origin/1.12"
)

# Load the shared legacy ID/tag normalization and verify crafting recipes first.
. "$PSScriptRoot/audit_1_12_crafting_recipes.ps1" -LegacyRef $LegacyRef

$machineRecipeNames = @{
    advbasiccircuit           = "advancedcircuit"
    advcircuitplate           = "advancedcircuitwafer"
    advcircuitplateetcher     = "advancedcircuitwafer_etcher"
    alienplanks               = "lightwoodplanks"
    antifogvisor              = "antifogvisorupgrade"
    atmanalyser               = "atmosphereanalyzer"
    basiccircuitplate         = "basiccircuitwafer"
    basiccircuitplateetcher   = "basiccircuitwafer_etcher"
    bionicleg                 = "bioniclegupgrade"
    flightspeed               = "flightspeedupgrade"
    highpressuretank          = "aluminumpressuretank"
    iocircuitboard_prec       = "itemiocircuit_prec"
    liquidiocircuitboard_prec = "fluidiocircuit_prec"
    lowpressuretank           = "ironpressuretank"
    paddedboots               = "paddedbootsupgrade"
    pressuretank              = "steelpressuretank"
    superhighpressuretank     = "titaniumpressuretank"
}

function Convert-MachineRecipeType {
    param([string]$Type)

    if ($Type -eq "advancedrocketry:electrolyser") {
        return "advancedrocketry:electrolyzer"
    }
    return $Type
}

function Convert-FluidId {
    param([string]$FluidId)

    if ($FluidId.Contains(":")) {
        return $FluidId
    }

    $advancedRocketryFluids = @{
        hydrogen   = "advancedrocketry:hydrogen"
        nitrogen   = "advancedrocketry:nitrogen"
        oxygen     = "advancedrocketry:oxygen"
        rocketfuel = "advancedrocketry:rocket_fuel"
    }
    if ($advancedRocketryFluids.ContainsKey($FluidId)) {
        return $advancedRocketryFluids[$FluidId]
    }
    return "minecraft:$FluidId"
}

function Convert-ToRecipeList {
    param($Value)

    if ($null -eq $Value) {
        return @()
    }
    if ($Value -is [System.Collections.IDictionary]) {
        return @($Value)
    }
    return @($Value)
}

function Convert-MachineItemStack {
    param(
        $Stack,
        [bool]$IsLegacy
    )

    $ingredient = Convert-Ingredient -Ingredient $Stack -IsLegacy $IsLegacy
    $count = Get-JsonValue -Object $Stack -Key "count"
    if ($null -eq $count) {
        $count = 1
    }
    return "$ingredient*$([int]$count)"
}

function Convert-MachineFluidStack {
    param($Stack)

    $fluid = Convert-FluidId -FluidId (Get-JsonValue -Object $Stack -Key "fluid")
    $amount = [int](Get-JsonValue -Object $Stack -Key "amount")
    return "fluid:$fluid*$amount"
}

function Get-MachineRecipeSignature {
    param(
        $Recipe,
        [bool]$IsLegacy
    )

    $type = Convert-MachineRecipeType -Type (Get-JsonValue -Object $Recipe -Key "type")
    $time = [int](Get-JsonValue -Object $Recipe -Key "time")
    $energy = [int](Get-JsonValue -Object $Recipe -Key "energy")

    $itemInputs = @(
        foreach ($stack in (Convert-ToRecipeList (Get-JsonValue -Object $Recipe -Key "itemingredients"))) {
            Convert-MachineItemStack -Stack $stack -IsLegacy $IsLegacy
        }
    ) | Sort-Object
    $fluidInputs = @(
        foreach ($stack in (Convert-ToRecipeList (Get-JsonValue -Object $Recipe -Key "fluidingredients"))) {
            Convert-MachineFluidStack -Stack $stack
        }
    ) | Sort-Object
    $itemOutputs = @(
        foreach ($stack in (Convert-ToRecipeList (Get-JsonValue -Object $Recipe -Key "itemresults"))) {
            Convert-MachineItemStack -Stack $stack -IsLegacy $IsLegacy
        }
    )
    $fluidOutputs = @(
        foreach ($stack in (Convert-ToRecipeList (Get-JsonValue -Object $Recipe -Key "fluidresults"))) {
            Convert-MachineFluidStack -Stack $stack
        }
    )

    return @(
        "type=$type"
        "time=$time"
        "energy=$energy"
        "itemInputs=$($itemInputs -join ',')"
        "fluidInputs=$($fluidInputs -join ',')"
        "itemOutputs=$($itemOutputs -join ',')"
        "fluidOutputs=$($fluidOutputs -join ',')"
    ) -join ";"
}

$missingMachineRecipes = @()
$changedMachineRecipes = @()
$comparedMachineRecipes = 0
$legacyMachineFiles = git ls-tree -r --name-only $LegacyRef |
    Where-Object {
        $_ -match "^src/main/resources/assets/advancedrocketry/recipes/[^/]+\.json$" -and
        $_ -notmatch "_factories\.json$"
    }

foreach ($legacyPath in $legacyMachineFiles) {
    $legacyRecipe = (git show "${LegacyRef}:$legacyPath") -join "`n" |
        ConvertFrom-Json -AsHashtable
    $legacyType = Get-JsonValue -Object $legacyRecipe -Key "type"
    if ($legacyType -match "^minecraft:crafting_") {
        continue
    }
    $comparedMachineRecipes++

    $legacyName = [IO.Path]::GetFileNameWithoutExtension($legacyPath)
    $currentName = if ($machineRecipeNames.ContainsKey($legacyName)) {
        $machineRecipeNames[$legacyName]
    } else {
        $legacyName
    }
    $currentPath = "src/main/resources/data/advancedrocketry/recipes/$currentName.json"
    if (-not (Test-Path -LiteralPath $currentPath)) {
        $missingMachineRecipes += [pscustomobject]@{
            LegacyRecipe = $legacyName
            ExpectedFile = $currentPath
        }
        continue
    }

    $currentRecipe = Read-JsonFile -Path $currentPath
    $legacySignature = Get-MachineRecipeSignature -Recipe $legacyRecipe -IsLegacy $true
    $currentSignature = Get-MachineRecipeSignature -Recipe $currentRecipe -IsLegacy $false
    if ($legacySignature -ne $currentSignature) {
        $changedMachineRecipes += [pscustomobject]@{
            LegacyRecipe     = $legacyName
            CurrentRecipe    = $currentName
            LegacySignature  = $legacySignature
            CurrentSignature = $currentSignature
        }
    }
}

Write-Output "Compared machine recipes: $comparedMachineRecipes"
Write-Output "Missing machine recipes: $($missingMachineRecipes.Count)"
$missingMachineRecipes | Format-Table -AutoSize
Write-Output "Changed machine recipes: $($changedMachineRecipes.Count)"
$changedMachineRecipes | Format-List

if ($missingMachineRecipes.Count -gt 0 -or $changedMachineRecipes.Count -gt 0) {
    exit 1
}
