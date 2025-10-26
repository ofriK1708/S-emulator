package system.http.utils;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

public class SimpleCookieManager implements CookieJar {

    // region constants and data structures
    private final static String CACHE_MANAGER_PREFIX = "    [Cookie Manager] ---> ";
    // endregion
    private final @NotNull ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final @NotNull Lock writeLock = readWriteLock.writeLock();
    private final @NotNull Lock readLock = readWriteLock.readLock();
    Map<String, Map<String, Cookie>> cookies = new HashMap<>();
    private Consumer<String> logData = System.out::println;

    public void setLogData(Consumer<String> logData) {
        this.logData = logData;
    }

    public void disableLogging() {
        this.logData = s -> {
        };
    }

    @Override
    public @NotNull List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
        String host = httpUrl.host();
        StringBuilder sb = new StringBuilder();
        sb.append(CACHE_MANAGER_PREFIX).append("Fetching cookies for domain: [").append(host).append("]...");
        List<Cookie> cookiesPerDomain = Collections.emptyList();
        readLock.lock();
        try {
            if (cookies.containsKey(host)) {
                cookiesPerDomain = new ArrayList<>(cookies.get(host).values());
            }
        } finally {
            readLock.unlock();
        }

        sb.append(" Total of ").append(cookiesPerDomain.size()).append(" cookie(s) will be loaded !");
        logData.accept(sb.toString());
        return cookiesPerDomain;
    }

    @Override
    public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> responseCookies) {
        String host = httpUrl.host();
        writeLock.lock();
        try {
            Map<String, Cookie> cookiesMap = cookies.computeIfAbsent(host, key -> new HashMap<>());
            responseCookies
                    .stream()
                    .filter(cookie -> !cookiesMap.containsKey(cookie.name())) // filter out existing cookies
                    .forEach(cookie -> {
                        logData.accept(CACHE_MANAGER_PREFIX +
                                "Storing cookie [" + cookie.name() + "] --> [" + cookie.value() + "]");
                        cookiesMap.put(cookie.name(), cookie);
                    });
        } finally {
            writeLock.unlock();
        }

    }

    public void removeCookiesOf(String domain) {
        writeLock.lock();
        try {
            cookies.remove(domain);
        } finally {
            writeLock.unlock();
        }

    }
}
