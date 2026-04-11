package top.timeblog.multiproxysync.config;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {

    private final Path dataDirectory;
    private final String fileName;

    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private CommentedConfigurationNode root;

    public ConfigManager(Path dataDirectory, String fileName) {
        this.dataDirectory = dataDirectory;
        this.fileName = fileName;
    }

    public void load() {
        try {

            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            Path configFile = dataDirectory.resolve(fileName);

            if (!Files.exists(configFile)) {
                createDefaultConfig(configFile);
            }

            loader = YamlConfigurationLoader.builder()
                    .path(configFile)
                    .build();

            root = loader.load();

        } catch (IOException e) {
            throw new RuntimeException("Failed to load config", e);
        }
    }

    private void createDefaultConfig(Path configFile) throws IOException {

        Files.writeString(configFile, """
plugin:
  serverName: OnlineServer
  enabled: false
redis:
  host: 127.0.0.1
  port: 6379
  password: TestPassword
""");

    }

    public void save() {
        try {
            loader.save(root);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config", e);
        }
    }

    public void reload() {
        load();
    }

    public ConfigurationNode getNode(Object... path) {
        return root.node(path);
    }

    public String getString(Object... path) {
        return root.node(path).getString();
    }

    public int getInt(Object... path) {
        return root.node(path).getInt();
    }

    public boolean getBoolean(Object... path) {
        return root.node(path).getBoolean();
    }

    public double getDouble(Object... path) {
        return root.node(path).getDouble();
    }

    public long getLong(Object... path) {
        return root.node(path).getLong();
    }

    public CommentedConfigurationNode getRoot() {
        return root;
    }
}