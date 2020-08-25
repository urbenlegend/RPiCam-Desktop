package com.rpicam.config;

import java.io.IOException;
import java.nio.file.Path;

public interface ConfigService {
    void loadConfigFile(Path configPath) throws IOException;
    void saveConfigFile(Path configPath) throws IOException;
    ConfigRoot getConfig();
}
