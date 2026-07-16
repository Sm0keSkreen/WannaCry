package dev.mg.wannacry.features.modules.render;

import dev.mg.wannacry.features.modules.Module;
import dev.mg.wannacry.features.settings.Setting;
import net.minecraft.world.InteractionHand;

public class ViewModelModule extends Module {

    private static ViewModelModule INSTANCE;

    public final Setting<Boolean> mirrorMode = bool("Mirror", false);

    public final Setting<Float> sPosXMaster  = num("PosX",  0.0f, -1.0f,   1.0f);
    public final Setting<Float> sPosYMaster  = num("PosY",  0.0f, -1.0f,   1.0f);
    public final Setting<Float> sPosZMaster  = num("PosZ",  0.0f, -1.0f,   1.0f);
    public final Setting<Float> sRotXMaster  = num("RotX",  0.0f, -180.0f, 180.0f);
    public final Setting<Float> sRotYMaster  = num("RotY",  0.0f, -180.0f, 180.0f);
    public final Setting<Float> sRotZMaster  = num("RotZ",  0.0f, -180.0f, 180.0f);
    public final Setting<Float> sScaleMaster = num("Scale", 1.0f, 0.1f,    3.0f);

    public final Setting<Float> sPosXMain  = num("Main PosX",  0.0f, -1.0f,   1.0f);
    public final Setting<Float> sPosYMain  = num("Main PosY",  0.0f, -1.0f,   1.0f);
    public final Setting<Float> sPosZMain  = num("Main PosZ",  0.0f, -1.0f,   1.0f);
    public final Setting<Float> sRotXMain  = num("Main RotX",  0.0f, -180.0f, 180.0f);
    public final Setting<Float> sRotYMain  = num("Main RotY",  0.0f, -180.0f, 180.0f);
    public final Setting<Float> sRotZMain  = num("Main RotZ",  0.0f, -180.0f, 180.0f);
    public final Setting<Float> sScaleMain = num("Main Scale", 1.0f, 0.1f,    3.0f);

    public final Setting<Float> sPosXOff   = num("Off PosX",  0.0f, -1.0f,   1.0f);
    public final Setting<Float> sPosYOff   = num("Off PosY",  0.0f, -1.0f,   1.0f);
    public final Setting<Float> sPosZOff   = num("Off PosZ",  0.0f, -1.0f,   1.0f);
    public final Setting<Float> sRotXOff   = num("Off RotX",  0.0f, -180.0f, 180.0f);
    public final Setting<Float> sRotYOff   = num("Off RotY",  0.0f, -180.0f, 180.0f);
    public final Setting<Float> sRotZOff   = num("Off RotZ",  0.0f, -180.0f, 180.0f);
    public final Setting<Float> sScaleOff  = num("Off Scale", 1.0f, 0.1f,    3.0f);

    public ViewModelModule() {
        super("ViewModel", "Customize your view model.", Category.CLOSET);
        INSTANCE = this;

        sPosXMaster .setVisibility(v -> mirrorMode.getValue());
        sPosYMaster .setVisibility(v -> mirrorMode.getValue());
        sPosZMaster .setVisibility(v -> mirrorMode.getValue());
        sRotXMaster .setVisibility(v -> mirrorMode.getValue());
        sRotYMaster .setVisibility(v -> mirrorMode.getValue());
        sRotZMaster .setVisibility(v -> mirrorMode.getValue());
        sScaleMaster.setVisibility(v -> mirrorMode.getValue());

        sPosXMain .setVisibility(v -> !mirrorMode.getValue());
        sPosYMain .setVisibility(v -> !mirrorMode.getValue());
        sPosZMain .setVisibility(v -> !mirrorMode.getValue());
        sRotXMain .setVisibility(v -> !mirrorMode.getValue());
        sRotYMain .setVisibility(v -> !mirrorMode.getValue());
        sRotZMain .setVisibility(v -> !mirrorMode.getValue());
        sScaleMain.setVisibility(v -> !mirrorMode.getValue());

        sPosXOff .setVisibility(v -> !mirrorMode.getValue());
        sPosYOff .setVisibility(v -> !mirrorMode.getValue());
        sPosZOff .setVisibility(v -> !mirrorMode.getValue());
        sRotXOff .setVisibility(v -> !mirrorMode.getValue());
        sRotYOff .setVisibility(v -> !mirrorMode.getValue());
        sRotZOff .setVisibility(v -> !mirrorMode.getValue());
        sScaleOff.setVisibility(v -> !mirrorMode.getValue());
    }

    public static ViewModelModule getInstance() {
        return INSTANCE;
    }

    public float getPosX(InteractionHand hand) {
        if (mirrorMode.getValue()) {

            return hand == InteractionHand.MAIN_HAND
                    ? sPosXMaster.getValue()
                    : -sPosXMaster.getValue();
        }
        return hand == InteractionHand.MAIN_HAND ? sPosXMain.getValue() : sPosXOff.getValue();
    }

    public float getPosY(InteractionHand hand) {
        if (mirrorMode.getValue()) {

            return sPosYMaster.getValue();
        }
        return hand == InteractionHand.MAIN_HAND ? sPosYMain.getValue() : sPosYOff.getValue();
    }

    public float getPosZ(InteractionHand hand) {
        if (mirrorMode.getValue()) {

            return sPosZMaster.getValue();
        }
        return hand == InteractionHand.MAIN_HAND ? sPosZMain.getValue() : sPosZOff.getValue();
    }

    public float getRotX(InteractionHand hand) {
        if (mirrorMode.getValue()) return sRotXMaster.getValue();
        return hand == InteractionHand.MAIN_HAND ? sRotXMain.getValue() : sRotXOff.getValue();
    }

    public float getRotY(InteractionHand hand) {
        if (mirrorMode.getValue()) return sRotYMaster.getValue();
        return hand == InteractionHand.MAIN_HAND ? sRotYMain.getValue() : sRotYOff.getValue();
    }

    public float getRotZ(InteractionHand hand) {
        if (mirrorMode.getValue()) return sRotZMaster.getValue();
        return hand == InteractionHand.MAIN_HAND ? sRotZMain.getValue() : sRotZOff.getValue();
    }

    public float getScale(InteractionHand hand) {
        if (mirrorMode.getValue()) return sScaleMaster.getValue();
        return hand == InteractionHand.MAIN_HAND ? sScaleMain.getValue() : sScaleOff.getValue();
    }
}
