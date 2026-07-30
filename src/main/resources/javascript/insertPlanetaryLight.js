function initializeCoreMod() {
    return {
        'planetaryLightmap': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.renderer.LightTexture',
                'methodName': 'func_205106_a',
                'methodDesc': '(F)V'
            },
            'transformer': function(method) {
                var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
                var Opcodes = Java.type('org.objectweb.asm.Opcodes');
                var arrayLength = method.instructions.size();

                print('Attempting to transform the planetary light map!');
                for (var i = 0; i < arrayLength; ++i) {
                    var instruction = method.instructions.get(i);
                    if (instruction.getOpcode() == Opcodes.INVOKEVIRTUAL
                            && instruction.owner == 'net/minecraft/client/world/ClientWorld'
                            && instruction.name == 'func_228326_g_'
                            && instruction.desc == '(F)F') {
                        var invoke = ASMAPI.listOf(ASMAPI.buildMethodCall(
                            'zmaster587/advancedRocketry/client/ClientHelper',
                            'adjustSunBrightness',
                            '(F)F',
                            ASMAPI.MethodType.STATIC
                        ));
                        method.instructions.insert(instruction, invoke[0]);
                        print('Transformed the planetary light map!');
                        return method;
                    }
                }

                print('Unable to locate ClientWorld#getSunBrightness in LightTexture!');
                return method;
            }
        }
    };
}
