package org.ants.parser;

import com.google.common.cache.CacheBuilder;
import org.apache.commons.io.FileUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.main.Batfish;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedMap;

public class BatfishAPI {
    private String testcase;
    private String workingPath;
    private String currentConfigPath;

    public BatfishAPI(String testcase, String workingPath) throws IOException{
        this.testcase = testcase;
        this.workingPath = workingPath;
    }

    public SortedMap<String, Configuration> parse() throws IOException {
        return parse(currentConfigPath);
    }

    public SortedMap<String, Configuration> parse(String configPath) throws IOException{
        currentConfigPath = configPath;

        // Copy configs to batfish working directory (batfish_path/<containter>/snapshots/<testcase>/input)
        String container = "test_container";
        String batfishPath = Paths.get(workingPath, "batfish").toString();
        FileUtils.deleteDirectory(Paths.get(workingPath).toFile()); // clear first
        Path batfishInputPath = Paths.get(batfishPath, container, "snapshots", testcase, "input");
        FileUtils.deleteDirectory(batfishInputPath.toFile());
        FileUtils.copyDirectory(Paths.get(configPath).toFile(), batfishInputPath.toFile());

        // outputPath/container/snapshots/test_case/...
        String[] initArgs = {"-storagebase", batfishPath,
                "-container", container,
                "-testrig", testcase,
                "-snapshotname", "test_snapshot",
                "-sv", // SerializeVendor
                "-si", // SerializeIndependent
        };
        Settings settings = new Settings(initArgs);
        settings.setLogger(new BatfishLogger("error", false, null, false, false));
        Batfish.initTestrigSettings(settings);

        Batfish batfish =
                new Batfish(
                        settings,
                        CacheBuilder.newBuilder().softValues().maximumSize(5).build(),
                        null,
                        null,
                        null,
                        null);
        batfish.run();
        return batfish.loadConfigurations();
    }
}
