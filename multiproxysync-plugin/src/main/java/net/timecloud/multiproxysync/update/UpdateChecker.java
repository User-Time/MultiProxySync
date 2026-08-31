package net.timecloud.multiproxysync.update;

import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UpdateChecker {
    private static final URI LATEST_RELEASE_API = URI.create(
            "https://api.github.com/repos/User-Time/MultiProxySync/releases/latest"
    );
    private static final String RELEASES_URL = "https://github.com/User-Time/MultiProxySync/releases/latest";
    private static final Pattern TAG_NAME_PATTERN = Pattern.compile(
            "\\\"tag_name\\\"\\s*:\\s*\\\"([^\\\"]+)\\\""
    );
    private static final Pattern VERSION_PATTERN = Pattern.compile(
            "^[vV]?(\\d+)(?:\\.(\\d+))?(?:\\.(\\d+))?([-+].*)?$"
    );

    private UpdateChecker() {
    }

    public static void check(Logger logger, String currentVersion) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            HttpRequest request = HttpRequest.newBuilder(LATEST_RELEASE_API)
                    .timeout(Duration.ofSeconds(5))
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "MultiProxySync/" + currentVersion)
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (response.statusCode() != 200) {
                logger.debug("Update check returned HTTP status {}.", response.statusCode());
                return;
            }

            Matcher tagMatcher = TAG_NAME_PATTERN.matcher(response.body());
            if (!tagMatcher.find()) {
                logger.debug("Update check response did not contain a release tag.");
                return;
            }

            String latestVersion = tagMatcher.group(1);
            if (compareVersions(latestVersion, currentVersion) > 0) {
                logger.warn(
                        "A new MultiProxySync version is available: {} (current: {}). Download: {}",
                        latestVersion,
                        currentVersion,
                        RELEASES_URL
                );
            } else {
                logger.debug("MultiProxySync is up to date ({}).", currentVersion);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.debug("Failed to check for MultiProxySync updates.", e);
        }
    }

    private static int compareVersions(String left, String right) {
        Matcher leftMatcher = VERSION_PATTERN.matcher(left.trim());
        Matcher rightMatcher = VERSION_PATTERN.matcher(right.trim());
        if (!leftMatcher.matches() || !rightMatcher.matches()) {
            return 0;
        }

        for (int group = 1; group <= 3; group++) {
            int leftPart = parseVersionPart(leftMatcher.group(group));
            int rightPart = parseVersionPart(rightMatcher.group(group));
            int comparison = Integer.compare(leftPart, rightPart);
            if (comparison != 0) {
                return comparison;
            }
        }

        boolean leftPrerelease = leftMatcher.group(4) != null
                && leftMatcher.group(4).startsWith("-");
        boolean rightPrerelease = rightMatcher.group(4) != null
                && rightMatcher.group(4).startsWith("-");
        return Boolean.compare(rightPrerelease, leftPrerelease);
    }

    private static int parseVersionPart(String part) {
        return part == null ? 0 : Integer.parseInt(part);
    }
}
