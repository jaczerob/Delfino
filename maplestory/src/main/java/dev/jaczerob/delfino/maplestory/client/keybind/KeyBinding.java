package dev.jaczerob.delfino.maplestory.client.keybind;

public class KeyBinding {
    private final int type;
    private final int action;

    public KeyBinding(int type, int action) {
        this.type = type;
        this.action = action;
    }

    public int getType() {
        return type;
    }

    public int getAction() {
        return action;
    }
}
