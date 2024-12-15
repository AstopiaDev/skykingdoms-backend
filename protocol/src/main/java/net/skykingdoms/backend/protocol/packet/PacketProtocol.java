package net.skykingdoms.backend.protocol.packet;

import net.skykingdoms.backend.protocol.packet.handshake.Handshake;
import net.skykingdoms.backend.protocol.packet.play.*;


public enum PacketProtocol {

    HANDSHAKE {
        {
            toServer(0x0, Handshake.Request.class, Handshake.Request::new);
            toClient(0x0, Handshake.Response.class, Handshake.Response::new);
        }
    },
    PLAY {
        {
            both(101, B101UpdateRank.class, B101UpdateRank::new);
            both(102, B102SetProperty.class, B102SetProperty::new);
            both(103, B103RemoveProperty.class, B103RemoveProperty::new);
            both(104, B104BroadcastChat.class, B104BroadcastChat::new);
            // 105
            both(106, B106Chat.class, B106Chat::new);
            both(107, B107ProxyKick.class, B107ProxyKick::new);
            both(108, B108UpdatePlayerLanguage.class, B108UpdatePlayerLanguage::new);
            both(109, B109UpdateSetting.class, B109UpdateSetting::new);
            both(110, B110UserUpdatePermission.class, B110UserUpdatePermission::new);
            both(111, B111GroupUpdatePermission.class, B111GroupUpdatePermission::new);

            toClient(1, Tabcomplete.Response.class, Tabcomplete.Response::new);
            toClient(2, NodeSnapshot.Request.class, NodeSnapshot.Request::new);
            toClient(3, ServerDeploy.Request.class, ServerDeploy.Request::new);
            toClient(4, S04UpdateRemoteData.class, S04UpdateRemoteData::new);
            toClient(5, S05UpdateNodeContainerInformation.class, S05UpdateNodeContainerInformation::new);
            toClient(6, S06RegisterServers.class, S06RegisterServers::new);
            toClient(7, S07UnregisterServer.class, S07UnregisterServer::new);
            toClient(8, ServerDestroy.Request.class, ServerDestroy.Request::new);
            toClient(9, NetworkPlayerLoad.Response.class, NetworkPlayerLoad.Response::new);
            toClient(10, S10RegisterCommands.class, S10RegisterCommands::new);
            toClient(11, S11ServerCommand.class, S11ServerCommand::new);
            toClient(12, ProxyPlayerLogin.Response.class, ProxyPlayerLogin.Response::new);
            toClient(13, S13SendTitle.class, S13SendTitle::new);
            toClient(14, S14SetPlayerTablist.class, S14SetPlayerTablist::new);
            toClient(15, S15ReloadLanguages.class, S15ReloadLanguages::new);
            toClient(16, ServerConnect.Response.class, ServerConnect.Response::new);
            toClient(17, LobbyBoot.Response.class, LobbyBoot.Response::new);
            toClient(18, S18SendPlayer.class, S18SendPlayer::new);
            toClient(19, S19BestServer.class, S19BestServer::new);
            toClient(20, S20LookupResponse.class, S20LookupResponse::new);
            toClient(21, S21SetGlobalOnline.class, S21SetGlobalOnline::new);
            toClient(22, S22ConnectResponse.class, S22ConnectResponse::new);
            toClient(23, S23FriendshipAction.class, S23FriendshipAction::new);
            toClient(24, S24PlayerMuteAction.class, S24PlayerMuteAction::new);
            toClient(25, S25UpdateGameMap.class, S25UpdateGameMap::new);
            toClient(26, S26UpdatePlayerGameServer.class, S26UpdatePlayerGameServer::new);
            toClient(27, GameBoot.Response.class, GameBoot.Response::new);
            toClient(28, S28UpdateNetworkState.class, S28UpdateNetworkState::new);
            toClient(29, RequestMap.Response.class, RequestMap.Response::new);
            toClient(30, S30ChangePlayerNickResponse.class, S30ChangePlayerNickResponse::new);
            toClient(31, OpenReportPacket.Response.class, OpenReportPacket.Response::new);
            toClient(32, SecurityToken.Response.class, SecurityToken.Response::new);
            toClient(33, S33VoteReceived.class, S33VoteReceived::new);
            toClient(34, DuelData.Response.class, DuelData.Response::new);
            toClient(35, S35BestGameServerByMap.class, S35BestGameServerByMap::new);
            toClient(36, ArenaBoot.Request.class, ArenaBoot.Request::new);
            toClient(37, S37BestDuelServer.class, S37BestDuelServer::new);
            toClient(38, S38UpdateDuelData.class, S38UpdateDuelData::new);
            toClient(39, S39UpdateAuth.class, S39UpdateAuth::new);

            toServer(1, C0PlayerConnected.class, C0PlayerConnected::new);
            toServer(2, Tabcomplete.Request.class, Tabcomplete.Request::new);
            toServer(3, NodeSnapshot.Response.class, NodeSnapshot.Response::new);
            toServer(4, ServerDeploy.Response.class, ServerDeploy.Response::new);
            toServer(5, ServerDestroy.Response.class, ServerDestroy.Response::new);
            toServer(6, NetworkPlayerLoad.Request.class, NetworkPlayerLoad.Request::new);
            toServer(7, C07ServerRegisterCommands.class, C07ServerRegisterCommands::new);
            toServer(8, C08SendServerCommand.class, C08SendServerCommand::new);
            toServer(9, C09PlayerCommand.class, C09PlayerCommand::new);
            toServer(10, C10ReconnectPlayers.class, C10ReconnectPlayers::new);
            toServer(11, ProxyPlayerLogin.Request.class, ProxyPlayerLogin.Request::new);
            toServer(12, C12ProxyPlayerDisconnect.class, C12ProxyPlayerDisconnect::new);
            toServer(13, ServerConnect.Request.class, ServerConnect.Request::new);
            toServer(14, LobbyBoot.Request.class, LobbyBoot.Request::new);
            toServer(15, C15GetBestServer.class, C15GetBestServer::new);
            toServer(16, C16RegisterLookups.class, C16RegisterLookups::new);
            toServer(17, C17LookupGlobalOnline.class, C17LookupGlobalOnline::new);
            toServer(18, C18ConnectExact.class, C18ConnectExact::new);
            toServer(19, C19LoadFriends.class, C19LoadFriends::new);
            toServer(20, C20MessageChannelMessage.class, C20MessageChannelMessage::new);
            toServer(21, C21RegisterGameInstance.class, C21RegisterGameInstance::new);
            toServer(22, C22UpdateGameState.class, C22UpdateGameState::new);
            toServer(23, GameBoot.Request.class, GameBoot.Request::new);
            toServer(24, RequestMap.Request.class, RequestMap.Request::new);
            toServer(27, OpenReportPacket.Request.class, OpenReportPacket.Request::new);
            toServer(28, SecurityToken.Request.class, SecurityToken.Request::new);
            toServer(29, C29RegisterPersistentInstance.class, C29RegisterPersistentInstance::new);
            toServer(30, DuelData.Request.class, DuelData.Request::new);
            toServer(31, C31CreateDuel.class, C31CreateDuel::new);
            toServer(32, C32GetBestGameServerByMap.class, C32GetBestGameServerByMap::new);
            toServer(33, ArenaBoot.Response.class, ArenaBoot.Response::new);
            toServer(34, C34GetBestDuelServer.class, C34GetBestDuelServer::new);
            toServer(35, C35RemoveDuelRequest.class, C35RemoveDuelRequest::new);
            toServer(36, C36ServerCommand.class, C36ServerCommand::new);
        }
    };

    public final PacketMapper TO_SERVER = new PacketMapper(), TO_CLIENT = new PacketMapper();

    public <T extends Packet<?>> void toServer(int id, Class<T> cls, PacketFactory factory) {
        TO_SERVER.registerPacket(id, cls, factory);
    }

    public <T extends Packet<?>> void toClient(int id, Class<T> cls, PacketFactory factory) {
        TO_CLIENT.registerPacket(id, cls, factory);
    }

    public <T extends Packet<?>> void both(int id, Class<T> cls, PacketFactory factory) {
        toServer(id, cls, factory);
        toClient(id, cls, factory);
    }

}