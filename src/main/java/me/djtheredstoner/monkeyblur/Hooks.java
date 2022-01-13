package me.djtheredstoner.monkeyblur;

@SuppressWarnings("unused")
public class Hooks {

    public static void startFrame() {
        if (MonkeyBlur.instance != null) {
            MonkeyBlur.instance.startFrame();
        }
    }

    public static void endFrame() {
        if (MonkeyBlur.instance != null) {
            MonkeyBlur.instance.endFrame();
        }
    }

    public static void bindFb() {
        if (MonkeyBlur.instance != null) {
            MonkeyBlur.instance.bindFb();
        }
    }

    public static void unbindFb() {
        if (MonkeyBlur.instance != null) {
            MonkeyBlur.instance.unbindFb();
        }
    }

    public static void setupCamera(float partialTicks) {
        if (MonkeyBlur.instance != null) {
            MonkeyBlur.instance.setupCamera(partialTicks);
        }
    }

}
