package groundbreaking.newbieguard.utils.config;

import java.security.SecureRandom;

public final class PasswordUtil {

    public String generatePass() {
        final String letters = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz";
        final String numbers = "1234567890";
        final String chars = "!@#$%&/\\|?";

        final int lettersLength = letters.length();
        final int numbersLength = numbers.length();
        final int charsLength = chars.length();

        final StringBuilder result = new StringBuilder();
        final SecureRandom random = new SecureRandom();

        final int length = random.nextInt(14) + 17;

        result.append(letters.charAt(random.nextInt(lettersLength)));
        result.append(numbers.charAt(random.nextInt(numbersLength)));
        result.append(chars.charAt(random.nextInt(charsLength)));

        for (int i = 3; i < length; i++) {
            final int choice = random.nextInt(3);
            if (choice == 0) {
                result.append(letters.charAt(random.nextInt(lettersLength)));
            } else if (choice == 1) {
                result.append(numbers.charAt(random.nextInt(numbersLength)));
            } else {
                result.append(chars.charAt(random.nextInt(charsLength)));
            }
        }

        return result.toString();
    }
}
