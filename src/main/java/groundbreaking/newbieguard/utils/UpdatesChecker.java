package groundbreaking.newbieguard.utils;

import groundbreaking.newbieguard.NewbieGuard;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public final class UpdatesChecker {

    @Getter
    private static boolean newVersion = false;
    @Getter
    private static String currentVersion, latestVersion, downloadLink;

    private final NewbieGuard plugin;
    private final FileConfiguration config;
    private final Logger logger;

    public UpdatesChecker(NewbieGuard plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.logger = plugin.getLogger();
    }

    public void startCheck() {
        if (!config.getBoolean("settings.check-updates", true)) {
            logger.info("\u001b[33mUpdates checker were disabled, but it's not recomend by author to do it!\u001b[0m");
            return;
        }

        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            String versionUrl = "https://raw.githubusercontent.com/groundbreakingmc/NewbieGuard/main/version.txt";
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(versionUrl))
                    .build();

            CompletableFuture<HttpResponse<String>> responseFuture = httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());

            responseFuture.thenAccept(response -> {
                if (response.statusCode() == 200) {
                    final String[] versionIfo = response.body().split("->", 2);
                    currentVersion = plugin.getDescription().getVersion();
                    final String[] currentVersionParams = currentVersion.split("\\.");
                    final String[] newVersion = versionIfo[0].split("\\.");

                    for (int i = 0; i < newVersion.length; i++) {
                        final int currVer = Integer.parseInt(currentVersionParams[i]);
                        final int newVer = Integer.parseInt(newVersion[i]);

                        if (currVer < newVer) {
                            UpdatesChecker.newVersion = true;
                        }
                    }

                    if (UpdatesChecker.newVersion) {
                        logger.info("\u001b[33m=========== NewbieGuard ===========\u001b[0m");
                        logger.info("\u001b[33mCurrent version: \u001b[93m" + currentVersion + "\u001b[0m");
                        logger.info("\u001b[33mNewest version: \u001b[92m" + latestVersion + "\u001b[0m");
                        logger.info("\u001b[33mDownload link: \u001b[94m" + downloadLink + "\u001b[0m");
                        logger.info("\u001b[33m===================================\u001b[0m");
                    } else {
                        logger.info("\u001b[92mNo updates were found!\u001b[0m");
                    }
                } else {
                    logger.warning("\u001b[31mCheck was canceled with response code: \u001b[91m" + response.statusCode() + "\u001b[31m.\u001b[0m");
                    logger.warning("\u001b[31mPlease create an issue \u001b[94https://github.com/groundbreakingmc/NewbieGuard/issues \u001b[31mand report this error.\u001b[0m");
                }
            }).join();
        }
        catch (Exception ex) {
            logger.warning("\u001b[31mCheck was canceled with exception message: \u001b[91m" + ex.getMessage() + "\u001b[31m.\u001b[0m");
            logger.warning("\u001b[31mPlease create an issue here: \u001b[94mhttps://github.com/groundbreakingmc/NewbieGuard/issues\u001b[31m, and report this error.\u001b[0m");
            ex.printStackTrace();
        }
    }

}