package net.skykingdoms.backend.protocol.packet.play.processor;

import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.play.*;


public interface PlayClientProcessor extends PlayProcessor {
    default void process(SecurityToken.Request packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(OpenReportPacket.Request packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(RequestMap.Request packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(GameBoot.Request packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(ArenaBoot.Response packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(C22UpdateGameState packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(C21RegisterGameInstance packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(C20MessageChannelMessage packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(C19LoadFriends packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(C18ConnectExact packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(C17LookupGlobalOnline packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(C16RegisterLookups packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(C15GetBestServer packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(LobbyBoot.Request packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(ServerConnect.Request packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(C10ReconnectPlayers packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(C12ProxyPlayerDisconnect packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(ProxyPlayerLogin.Request packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(C09PlayerCommand packet, ChannelProcessorContext ctx) {
        // override me
    }
    default void process(C08SendServerCommand packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(C36ServerCommand packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(C07ServerRegisterCommands packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(Tabcomplete.Request packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(NetworkPlayerLoad.Request packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(C0PlayerConnected packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(NodeSnapshot.Response packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(ServerDeploy.Response packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(ServerDestroy.Response packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(C29RegisterPersistentInstance packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(DuelData.Request packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(C31CreateDuel packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(C32GetBestGameServerByMap packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(C34GetBestDuelServer packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(C35RemoveDuelRequest packet, ChannelProcessorContext ctx) {
        // override me
    }
}
