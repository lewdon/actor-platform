package im.actor.model;

import java.util.ArrayList;

import im.actor.model.crypto.bouncycastle.BouncyCastleProvider;
import im.actor.model.network.ConnectionEndpoint;

/**
 * Created by ex3ndr on 16.02.15.
 */
public class ConfigurationBuilder {

    private LogCallback log;

    private Networking networking;

    private Threading threading;
    private MainThread mainThread;

    private Storage enginesFactory;

    private ArrayList<ConnectionEndpoint> endpoints = new ArrayList<ConnectionEndpoint>();

    private LocaleProvider localeProvider;

    private PhoneBookProvider phoneBookProvider;

    private CryptoProvider cryptoProvider = new BouncyCastleProvider();

    private FileSystemProvider fileSystemProvider;

    private boolean enableContactsLogging = false;
    private boolean enableNetworkLogging = false;

    private NotificationProvider notificationProvider;

    private ApiConfiguration apiConfiguration;

    public ConfigurationBuilder setApiConfiguration(ApiConfiguration apiConfiguration) {
        this.apiConfiguration = apiConfiguration;
        return this;
    }

    public ConfigurationBuilder setNotificationProvider(NotificationProvider notificationProvider) {
        this.notificationProvider = notificationProvider;
        return this;
    }

    public ConfigurationBuilder setFileSystemProvider(FileSystemProvider fileSystemProvider) {
        this.fileSystemProvider = fileSystemProvider;
        return this;
    }

    public ConfigurationBuilder setEnableContactsLogging(boolean enableContactsLogging) {
        this.enableContactsLogging = enableContactsLogging;
        return this;
    }

    public ConfigurationBuilder setEnableNetworkLogging(boolean enableNetworkLogging) {
        this.enableNetworkLogging = enableNetworkLogging;
        return this;
    }

    public ConfigurationBuilder setCryptoProvider(CryptoProvider cryptoProvider) {
        this.cryptoProvider = cryptoProvider;
        return this;
    }

    public ConfigurationBuilder setPhoneBookProvider(PhoneBookProvider phoneBookProvider) {
        this.phoneBookProvider = phoneBookProvider;
        return this;
    }

    public ConfigurationBuilder setLog(LogCallback log) {
        this.log = log;
        return this;
    }

    public ConfigurationBuilder setNetworking(Networking networking) {
        this.networking = networking;
        return this;
    }

    public ConfigurationBuilder setThreading(Threading threading) {
        this.threading = threading;
        return this;
    }

    public ConfigurationBuilder setStorage(Storage storage) {
        this.enginesFactory = storage;
        return this;
    }

    public ConfigurationBuilder setLocale(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
        return this;
    }

    public ConfigurationBuilder addEndpoint(String url) {
        // Manual baggy parsing for GWT
        String scheme = url.substring(0, url.indexOf(":")).toLowerCase();
        String host = url.substring(url.indexOf("://") + "://".length());
        if (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        int port = -1;
        if (host.contains(":")) {
            String[] parts = host.split(":");
            host = parts[0];
            port = Integer.parseInt(parts[1]);
        }

        if (scheme.equals("ssl") || scheme.equals("tls")) {
            if (port <= 0) {
                port = 443;
            }
            endpoints.add(new ConnectionEndpoint(host, port, ConnectionEndpoint.Type.TCP_TLS));
        } else if (scheme.equals("tcp")) {
            if (port <= 0) {
                port = 80;
            }
            endpoints.add(new ConnectionEndpoint(host, port, ConnectionEndpoint.Type.TCP));
        } else if (scheme.equals("ws")) {
            if (port <= 0) {
                port = 80;
            }
            endpoints.add(new ConnectionEndpoint(host, port, ConnectionEndpoint.Type.WS));
        } else if (scheme.equals("wss")) {
            if (port <= 0) {
                port = 443;
            }
            endpoints.add(new ConnectionEndpoint(host, port, ConnectionEndpoint.Type.WS_TLS));
        } else {
            throw new RuntimeException("Unknown scheme type: " + scheme);
        }
        return this;
    }

    public ConfigurationBuilder setMainThread(MainThread mainThread) {
        this.mainThread = mainThread;
        return this;
    }

    public Configuration build() {
        if (networking == null) {
            throw new RuntimeException("Networking is not set");
        }
        if (threading == null) {
            throw new RuntimeException("Threading is not set");
        }
        if (mainThread == null) {
            throw new RuntimeException("Main Thread is not set");
        }
        if (enginesFactory == null) {
            throw new RuntimeException("Storage not set");
        }
        if (endpoints.size() == 0) {
            throw new RuntimeException("Endpoints not set");
        }
        if (localeProvider == null) {
            throw new RuntimeException("Locale Provider not set");
        }
        if (phoneBookProvider == null) {
            throw new RuntimeException("Phonebook Provider not set");
        }
        if (cryptoProvider == null) {
            throw new RuntimeException("Crypto Provider not set");
        }
        if (apiConfiguration == null) {
            throw new RuntimeException("Api Configuration not set");
        }
        return new Configuration(networking, endpoints.toArray(new ConnectionEndpoint[endpoints.size()]),
                threading, mainThread, enginesFactory, log, localeProvider,
                phoneBookProvider, cryptoProvider, fileSystemProvider, notificationProvider,
                apiConfiguration, enableContactsLogging, enableNetworkLogging);
    }
}
