package groundbreaking.newbieguard.utils.colorizer;

public final class VanillaColorizer implements IColorizer {

    @Override
    public String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        return ColorCodesTranslator.translateAlternateColorCodes('&', message);
    }
}
