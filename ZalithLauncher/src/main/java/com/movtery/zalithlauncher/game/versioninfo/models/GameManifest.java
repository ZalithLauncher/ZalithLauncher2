/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.versioninfo.models;

import static com.movtery.zalithlauncher.path.UrlManagerKt.URL_MINECRAFT_ASSETS_INDEX;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class GameManifest {
    @SerializedName("arguments")
    private Arguments arguments;
    @SerializedName("assetIndex")
    private AssetIndex assetIndex;
    @SerializedName("assets")
    private String assets;
    @SerializedName("complianceLevel")
    private int complianceLevel;
    @SerializedName("downloads")
    private Downloads downloads;
    @SerializedName("id")
    private String id;
    @SerializedName("javaVersion")
    private JavaVersion javaVersion;
    @SerializedName("libraries")
    private List<Library> libraries;
    @SerializedName("mainClass")
    private String mainClass;
    @SerializedName("minecraftArguments")
    private String minecraftArguments;
    @SerializedName("minimumLauncherVersion")
    private int minimumLauncherVersion;
    @SerializedName("releaseTime")
    private String releaseTime;
    @SerializedName("time")
    private String time;
    @SerializedName("type")
    private String type;
    @SerializedName("logging")
    private Logging logging;
    @SerializedName("inheritsFrom")
    private String inheritsFrom; //作为非合并版本的标记

    public Arguments getArguments() {
        return arguments;
    }

    public void setArguments(Arguments arguments) {
        this.arguments = arguments;
    }

    /**
     * [Modified from FCL](https://github.com/FCL-Team/FoldCraftLauncher/blob/6d88587eb62d84ae7ef26a9003640535265ff4f2/FCLCore/src/main/java/com/tungsten/fclcore/game/Version.java#L229-L267)
     */
    public AssetIndex getAssetIndex() {
        String assetsId = assets == null ? "legacy" : assets;

        if (assetIndex == null) {
            String hash;
            switch (assetsId) {
                case "1.8":
                    hash = "f6ad102bcaa53b1a58358f16e376d548d44933ec";
                    break;
                case "14w31a":
                    hash = "10a2a0e75b03cfb5a7196abbdf43b54f7fa61deb";
                    break;
                case "14w25a":
                    hash = "32ff354a3be1c4dd83027111e6d79ee4d701d2c0";
                    break;
                case "1.7.4":
                    hash = "545510a60f526b9aa8a38f9c0bc7a74235d21675";
                    break;
                case "1.7.10":
                    hash = "1863782e33ce7b584fc45b037325a1964e095d3e";
                    break;
                case "1.7.3":
                    hash = "f6cf726f4747128d13887010c2cbc44ba83504d9";
                    break;
                case "pre-1.6":
                    hash = "3d8e55480977e32acd9844e545177e69a52f594b";
                    break;
                case "legacy":
                default:
                    assetsId = "legacy";
                    hash = "770572e819335b6c0a053f8378ad88eda189fc14";
            }

            String url = URL_MINECRAFT_ASSETS_INDEX + "/" + hash + "/" + assetsId + ".json";
            return new AssetIndex(assetsId, url);
        } else {
            return assetIndex;
        }
    }

    public AssetIndex getRawAssetIndex() {
        return assetIndex;
    }

    public void setAssetIndex(AssetIndex assetIndex) {
        this.assetIndex = assetIndex;
    }

    public String getAssets() {
        return assets;
    }

    public void setAssets(String assets) {
        this.assets = assets;
    }

    public int getComplianceLevel() {
        return complianceLevel;
    }

    public void setComplianceLevel(int complianceLevel) {
        this.complianceLevel = complianceLevel;
    }

    public Downloads getDownloads() {
        return downloads;
    }

    public void setDownloads(Downloads downloads) {
        this.downloads = downloads;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JavaVersion getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(JavaVersion javaVersion) {
        this.javaVersion = javaVersion;
    }

    public List<Library> getLibraries() {
        return libraries;
    }

    public void setLibraries(List<Library> libraries) {
        this.libraries = libraries;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public String getMinecraftArguments() {
        return minecraftArguments;
    }

    public void setMinecraftArguments(String minecraftArguments) {
        this.minecraftArguments = minecraftArguments;
    }

    public int getMinimumLauncherVersion() {
        return minimumLauncherVersion;
    }

    public void setMinimumLauncherVersion(int minimumLauncherVersion) {
        this.minimumLauncherVersion = minimumLauncherVersion;
    }

    public String getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(String releaseTime) {
        this.releaseTime = releaseTime;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Logging getLogging() {
        return logging;
    }

    public void setLogging(Logging logging) {
        this.logging = logging;
    }

    public String getInheritsFrom() {
        return inheritsFrom;
    }

    public void setInheritsFrom(String inheritsFrom) {
        this.inheritsFrom = inheritsFrom;
    }

    public static class Arguments {
        @SerializedName("game")
        private List<Object> game;
        @SerializedName("jvm")
        private List<Object> jvm;

        public List<Object> getGame() {
            return game;
        }

        public void setGame(List<Object> game) {
            this.game = game;
        }

        public List<Object> getJvm() {
            return jvm;
        }

        public void setJvm(List<Object> jvm) {
            this.jvm = jvm;
        }
    }

    public static class AssetIndex {
        @SerializedName("id")
        private String id;
        @SerializedName("sha1")
        private String sha1;
        @SerializedName("size")
        private long size;
        @SerializedName("totalSize")
        private long totalSize;
        @SerializedName("url")
        private String url;

        public AssetIndex(String id, String url) {
            this(id, null, url);
        }

        public AssetIndex(String id, String sha1, String url) {
            this(id, sha1, 0, url);
        }

        public AssetIndex(String id, String sha1, long size, String url) {
            this(id, sha1, size, 0, url);
        }

        public AssetIndex(String id, String sha1, long size, long totalSize, String url) {
            this.id = id;
            this.sha1 = sha1;
            this.size = size;
            this.totalSize = totalSize;
            this.url = url;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSha1() {
            return sha1;
        }

        public void setSha1(String sha1) {
            this.sha1 = sha1;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public void setTotalSize(long totalSize) {
            this.totalSize = totalSize;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Downloads {
        @SerializedName("client")
        private Client client;
        @SerializedName("client_mappings")
        private ClientMappings clientMappings;
        @SerializedName("server")
        private Server server;
        @SerializedName("server_mappings")
        private ServerMappings serverMappings;

        public Client getClient() {
            return client;
        }

        public void setClient(Client client) {
            this.client = client;
        }

        public ClientMappings getClientMappings() {
            return clientMappings;
        }

        public void setClientMappings(ClientMappings clientMappings) {
            this.clientMappings = clientMappings;
        }

        public Server getServer() {
            return server;
        }

        public void setServer(Server server) {
            this.server = server;
        }

        public ServerMappings getServerMappings() {
            return serverMappings;
        }

        public void setServerMappings(ServerMappings serverMappings) {
            this.serverMappings = serverMappings;
        }
    }

    public static class Client {
        @SerializedName("sha1")
        private String sha1;
        @SerializedName("size")
        private long size;
        @SerializedName("url")
        private String url;

        public String getSha1() {
            return sha1;
        }

        public void setSha1(String sha1) {
            this.sha1 = sha1;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class ClientMappings {
        @SerializedName("sha1")
        private String sha1;
        @SerializedName("size")
        private long size;
        @SerializedName("url")
        private String url;

        public String getSha1() {
            return sha1;
        }

        public void setSha1(String sha1) {
            this.sha1 = sha1;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Server {
        @SerializedName("sha1")
        private String sha1;
        @SerializedName("size")
        private long size;
        @SerializedName("url")
        private String url;

        public String getSha1() {
            return sha1;
        }

        public void setSha1(String sha1) {
            this.sha1 = sha1;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class ServerMappings {
        @SerializedName("sha1")
        private String sha1;
        @SerializedName("size")
        private long size;
        @SerializedName("url")
        private String url;

        public String getSha1() {
            return sha1;
        }

        public void setSha1(String sha1) {
            this.sha1 = sha1;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class JavaVersion {
        @SerializedName("component")
        private String component;
        @SerializedName("majorVersion")
        private int majorVersion;
        @SerializedName("version")
        private int version; // parameter used by LabyMod 4

        public String getComponent() {
            return component;
        }
        public void setComponent(String component) {
            this.component = component;
        }
        public int getMajorVersion() {
            return majorVersion;
        }
        public void setMajorVersion(int majorVersion) {
            this.majorVersion = majorVersion;
        }
        public int getVersion() {
            return version;
        }
        public void setVersion(int version) {
            this.version = version;
        }
    }

    public static class Library {
        @SerializedName("downloads")
        private DownloadsX downloads;
        @SerializedName("name")
        private String name;
        @Nullable
        @SerializedName("natives")
        private Map<OperatingSystem, String> natives;
        @SerializedName("rules")
        private List<Rule> rules;
        @SerializedName("url")
        private String url;
        @Nullable
        @SerializedName("sha1")
        private String sha1;
        @SerializedName("size")
        private long size;

        public DownloadsX getDownloads() {
            return downloads;
        }

        public void setDownloads(DownloadsX downloads) {
            this.downloads = downloads;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public @Nullable Map<OperatingSystem, String> getNatives() {
            return natives;
        }

        public void setNatives(@Nullable Map<OperatingSystem, String> natives) {
            this.natives = natives;
        }

        public List<Rule> getRules() {
            return rules;
        }

        public void setRules(List<Rule> rules) {
            this.rules = rules;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public @Nullable String getSha1() {
            return sha1;
        }

        public void setSha1(@Nullable String sha1) {
            this.sha1 = sha1;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public boolean isNative() {
            return this.natives != null && Rule.checkRules(rules);
        }
    }

    public static class DownloadsX {
        @SerializedName("artifact")
        private Artifact artifact;

        public Artifact getArtifact() {
            return artifact;
        }
        public void setArtifact(Artifact artifact) {
            this.artifact = artifact;
        }
    }

    public static class Artifact {
        @SerializedName("path")
        private String path;
        @SerializedName("sha1")
        private String sha1;
        @SerializedName("size")
        private long size;
        @SerializedName("url")
        private String url;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getSha1() {
            return sha1;
        }

        public void setSha1(String sha1) {
            this.sha1 = sha1;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class Rule {
        @SerializedName("action")
        private Action action;
        @SerializedName("os")
        private Os os;
        @SerializedName("features")
        private Features features;
        @SerializedName("value")
        private List<Object> value;

        public Action getAction() {
            return action;
        }

        public void setAction(Action action) {
            this.action = action;
        }

        public Os getOs() {
            return os;
        }

        public void setOs(Os os) {
            this.os = os;
        }

        public Features getFeatures() {
            return features;
        }

        public void setFeatures(Features features) {
            this.features = features;
        }

        public List<Object> getValue() {
            return value;
        }

        public void setValue(List<Object> value) {
            this.value = value;
        }

        /**
         * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/a6f3fc0/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/Tools.java#L815-L823)
         */
        public static boolean checkRules(@Nullable List<Rule> rules) {
            if (rules == null || rules.isEmpty()) return true; // always allow

            for (Rule rule : rules) {
                if (rule.action == Action.ALLOW && rule.os != null && rule.os.name.equals("osx")) {
                    return false; //disallow
                }
            }
            return true; // allow if none match
        }
    }

    public enum Action {
        @SerializedName("allow")
        ALLOW,
        @SerializedName("disallow")
        DISALLOW
    }

    public static class Os {
        @SerializedName("name")
        private String name;
        @SerializedName("arch")
        private String arch;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getArch() {
            return arch;
        }

        public void setArch(String arch) {
            this.arch = arch;
        }
    }

    public static class Features {
        @SerializedName("is_demo_user")
        private Boolean isDemoUser;
        @SerializedName("has_custom_resolution")
        private Boolean hasCustomResolution;
        @SerializedName("has_quick_plays_support")
        private Boolean hasQuickPlaysSupport;
        @SerializedName("is_quick_play_singleplayer")
        private Boolean isQuickPlaySingleplayer;
        @SerializedName("is_quick_play_multiplayer")
        private Boolean isQuickPlayMultiplayer;
        @SerializedName("is_quick_play_realms")
        private Boolean isQuickPlayRealms;

        public Boolean getDemoUser() {
            return isDemoUser;
        }

        public void setDemoUser(Boolean demoUser) {
            isDemoUser = demoUser;
        }

        public Boolean getHasCustomResolution() {
            return hasCustomResolution;
        }

        public void setHasCustomResolution(Boolean hasCustomResolution) {
            this.hasCustomResolution = hasCustomResolution;
        }

        public Boolean getHasQuickPlaysSupport() {
            return hasQuickPlaysSupport;
        }

        public void setHasQuickPlaysSupport(Boolean hasQuickPlaysSupport) {
            this.hasQuickPlaysSupport = hasQuickPlaysSupport;
        }

        public Boolean getQuickPlaySingleplayer() {
            return isQuickPlaySingleplayer;
        }

        public void setQuickPlaySingleplayer(Boolean quickPlaySingleplayer) {
            isQuickPlaySingleplayer = quickPlaySingleplayer;
        }

        public Boolean getQuickPlayMultiplayer() {
            return isQuickPlayMultiplayer;
        }

        public void setQuickPlayMultiplayer(Boolean quickPlayMultiplayer) {
            isQuickPlayMultiplayer = quickPlayMultiplayer;
        }

        public Boolean getQuickPlayRealms() {
            return isQuickPlayRealms;
        }

        public void setQuickPlayRealms(Boolean quickPlayRealms) {
            isQuickPlayRealms = quickPlayRealms;
        }
    }

    public static class Logging {
        @SerializedName("client")
        private LoggingClient client;

        public LoggingClient getClient() {
            return client;
        }

        public void setClient(LoggingClient client) {
            this.client = client;
        }
    }

    public static class LoggingClient {
        @SerializedName("argument")
        private String argument;
        @SerializedName("file")
        private LoggingFile file;
        @SerializedName("type")
        private String type;

        public String getArgument() {
            return argument;
        }

        public void setArgument(String argument) {
            this.argument = argument;
        }

        public LoggingFile getFile() {
            return file;
        }

        public void setFile(LoggingFile file) {
            this.file = file;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class LoggingFile {
        @SerializedName("id")
        private String id;
        @SerializedName("sha1")
        private String sha1;
        @SerializedName("size")
        private long size;
        @SerializedName("url")
        private String url;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSha1() {
            return sha1;
        }

        public void setSha1(String sha1) {
            this.sha1 = sha1;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}