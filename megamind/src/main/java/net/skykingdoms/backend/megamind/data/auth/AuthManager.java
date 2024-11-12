package net.skykingdoms.backend.megamind.data.auth;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.skykingdoms.api.backend.gamer.ApiGamer;
import net.skykingdoms.backend.base.data.auth.AuthSqlHandler;
import net.skykingdoms.backend.base.util.hash.HashType;
import net.skykingdoms.backend.base.util.pair.Pair;
import net.skykingdoms.backend.megamind.AsyncExecution;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.protocol.packet.play.S39UpdateAuth;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public enum AuthManager {
    INSTANCE;

    private final Cache<String, UUID> sessions = CacheBuilder.newBuilder()
            .expireAfterWrite(12, TimeUnit.HOURS)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

    private final HashSet<Gamer> loggedOutGamers = new HashSet<>();

    public void postBootstrap() {
        AsyncExecution.INSTANCE.runTimer(() -> {
            loggedOutGamers.removeIf(ApiGamer::isLoggedIn);
            loggedOutGamers.forEach(gamer -> {
                if (!gamer.isRegistered()) {
                    gamer.sendLocalizedMessage("authentication_register");
                } else {
                    gamer.sendLocalizedMessage("authentication_login");
                }
            });
        }, 3L, 3L, TimeUnit.SECONDS);
    }

    public void addAuthenticator(Gamer gamer) {
        loggedOutGamers.add(gamer);
    }

    public void registerGamer(Gamer gamer, String password) {
        AuthSqlHandler.INSTANCE.insertAuthRecord(
                gamer.getGamerId(),
                gamer.isPremium(),
                HashType.BCRYPT.hashPassword(password),
                HashType.BCRYPT
        );

        gamer.setRegistered(true);
        gamer.setLoggedIn(true);

        this.sendLoginPacket(gamer, S39UpdateAuth.Action.LOGIN);
        this.insertSession(gamer.getAddress().getHostAddress(), UUID.randomUUID());
    }

    public boolean unregisterGamer(Gamer gamer, String password) {
        Pair<String, HashType> hashInfo = AuthSqlHandler.INSTANCE.getPassword(gamer.getGamerId()).join();
        String hashPass = hashInfo.getFirst();
        HashType hashType = hashInfo.getSecond();

        if (hashType.comparePassword(password, hashPass)) {
            AuthSqlHandler.INSTANCE.removeAuthRecord(gamer.getGamerId());
            gamer.setRegistered(false);
            gamer.setLoggedIn(false);
            addAuthenticator(gamer);
            this.sendLoginPacket(gamer, S39UpdateAuth.Action.LOGOUT);
            return true;
        }

        return false;
    }

    public void forceUnregisterGamer(Gamer gamer) {
        AuthSqlHandler.INSTANCE.removeAuthRecord(gamer.getGamerId());
        gamer.setRegistered(false);
        gamer.setLoggedIn(false);
        addAuthenticator(gamer);
        this.sendLoginPacket(gamer, S39UpdateAuth.Action.LOGOUT);
    }

    public boolean logoutGamer(Gamer gamer) {
        if (gamer.isLoggedIn()) {
            gamer.setLoggedIn(false);
            addAuthenticator(gamer);
            this.sendLoginPacket(gamer, S39UpdateAuth.Action.LOGOUT);
            return true;
        }
        return false;
    }

    public boolean loginGamer(Gamer gamer, String password) {
        Pair<String, HashType> hashInfo = AuthSqlHandler.INSTANCE.getPassword(gamer.getGamerId()).join();
        String hashPass = hashInfo.getFirst();
        HashType hashType = hashInfo.getSecond();

        if (hashType.comparePassword(password, hashPass)) {
            gamer.setLoggedIn(true);
            this.sendLoginPacket(gamer, S39UpdateAuth.Action.LOGIN);
            this.insertSession(gamer.getAddress().getHostAddress(), UUID.randomUUID());
            return true;
        }

        return false;
    }

    public boolean changePassword(Gamer gamer, String oldPassword, String newPassword) {
        Pair<String, HashType> hashInfo = AuthSqlHandler.INSTANCE.getPassword(gamer.getGamerId()).join();
        String hashPass = hashInfo.getFirst();
        HashType hashType = hashInfo.getSecond();

        if (hashType.comparePassword(oldPassword, hashPass)) {
            AuthSqlHandler.INSTANCE.insertAuthRecord(gamer.getGamerId(), false, HashType.BCRYPT.hashPassword(newPassword), HashType.BCRYPT);
            gamer.setLoggedIn(false);
            addAuthenticator(gamer);
            this.sendLoginPacket(gamer, S39UpdateAuth.Action.LOGOUT);
            return true;
        }

        return false;
    }

    public void forceChangePassword(Gamer gamer, String newPassword) {
        AuthSqlHandler.INSTANCE.insertAuthRecord(gamer.getGamerId(), false, HashType.BCRYPT.hashPassword(newPassword), HashType.BCRYPT);
        gamer.setLoggedIn(false);
        addAuthenticator(gamer);
        this.sendLoginPacket(gamer, S39UpdateAuth.Action.LOGOUT);
    }

    private void sendLoginPacket(Gamer gamer, S39UpdateAuth.Action action) {
        S39UpdateAuth packet = new S39UpdateAuth(gamer.getGamerId(), action);
        gamer.sendToProxy(packet);
        gamer.sendToServer(packet);
    }

    public void insertSession(String address, UUID uuid) {
        sessions.put(address, uuid);
    }

    public boolean sessionMatches(String address, UUID uuid) {
        UUID session = sessions.getIfPresent(address);
        if (session == null) {
            return false;
        }

        return session == uuid;
    }
}
