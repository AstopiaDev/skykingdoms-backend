package net.skykingdoms.backend.protocol.packet.play.processor;

import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.play.*;


public interface PlayServerProcessor extends PlayProcessor {
    default void process(SecurityToken.Response packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(OpenReportPacket.Response packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S30ChangePlayerNickResponse packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(RequestMap.Response packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S28UpdateNetworkState packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(GameBoot.Response packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(ArenaBoot.Request packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S26UpdatePlayerGameServer packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S25UpdateGameMap packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S24PlayerMuteAction packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S23FriendshipAction packet, ChannelProcessorContext ctx) {
        // override me
    }
    default void process(S22ConnectResponse packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S21SetGlobalOnline packet, ChannelProcessorContext ctx) {
        // override me
    }
    default void process(S20LookupResponse packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S19BestServer packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S18SendPlayer packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(LobbyBoot.Response packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(ServerConnect.Response packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S15ReloadLanguages packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S14SetPlayerTablist packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S13SendTitle packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(ProxyPlayerLogin.Response packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S12FailedReconnectPlayers packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S11ServerCommand packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S10RegisterCommands packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(NetworkPlayerLoad.Response packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(Tabcomplete.Response packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S06RegisterServers packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S07UnregisterServer packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(NodeSnapshot.Request packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(ServerDeploy.Request packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(ServerDestroy.Request packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S04UpdateRemoteData packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S05UpdateNodeContainerInformation packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S33VoteReceived packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(DuelData.Response packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S35BestGameServerByMap packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S37BestDuelServer packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S38UpdateDuelData packet, ChannelProcessorContext ctx) {
        // override me
    }

    default void process(S39UpdateAuth packet, ChannelProcessorContext ctx) {
        // override me
    }
}
