package cn.nukkit.player.handler;

import cn.nukkit.Server;
import cn.nukkit.event.player.PlayerAsyncPreLoginEvent;
import cn.nukkit.event.player.PlayerPreLoginEvent;
import cn.nukkit.network.BedrockInterface;
import cn.nukkit.network.ProtocolInfo;
import cn.nukkit.player.Player;
import cn.nukkit.player.PlayerLoginData;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.ClientChainData;
import cn.nukkit.utils.TextFormat;
import com.nukkitx.protocol.bedrock.BedrockPacketCodec;
import com.nukkitx.protocol.bedrock.BedrockServerSession;
import com.nukkitx.protocol.bedrock.handler.BedrockPacketHandler;
import com.nukkitx.protocol.bedrock.packet.LoginPacket;
import com.nukkitx.protocol.bedrock.packet.PlayStatusPacket;
import lombok.extern.log4j.Log4j2;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Extollite
 */
@Log4j2
public class LoginPacketHandler implements BedrockPacketHandler {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[aA-zZ\\s\\d_]{3,16}+$");

    private final BedrockServerSession session;
    private final Server server;

    private final PlayerLoginData loginData;

    public LoginPacketHandler(BedrockServerSession session, Server server, BedrockInterface interfaz) {
        this.session = session;
        this.server = server;
        this.loginData = new PlayerLoginData(session, server, interfaz);
    }

    @Override
    public boolean handle(LoginPacket packet) {
        int protocolVersion = packet.getProtocolVersion();
        BedrockPacketCodec packetCodec = ProtocolInfo.getPacketCodec(protocolVersion);

        if (packetCodec == null) {
            String message;
            PlayStatusPacket statusPacket = new PlayStatusPacket();
            if (protocolVersion < ProtocolInfo.getDefaultProtocolVersion()) {
                message = "disconnectionScreen.outdatedClient";

                statusPacket.setStatus(PlayStatusPacket.Status.FAILED_CLIENT);
            } else {
                message = "disconnectionScreen.outdatedServer";

                statusPacket.setStatus(PlayStatusPacket.Status.FAILED_SERVER);
            }
            session.sendPacket(statusPacket);
            session.disconnect(message);
            return true;
        }
        session.setPacketCodec(packetCodec);

        this.loginData.setChainData(ClientChainData.read(packet));

        if (!this.loginData.getChainData().isXboxAuthed() && this.server.getPropertyBoolean("xbox-auth")) {
            session.disconnect("disconnectionScreen.notAuthenticated");
            return true;
        }

        String username = this.loginData.getChainData().getUsername();
        Matcher matcher = NAME_PATTERN.matcher(username);

        if (!matcher.matches() || username.equalsIgnoreCase("rcon") || username.equalsIgnoreCase("console")) {
            session.disconnect("disconnectionScreen.invalidName");
            return true;
        }

        this.loginData.setName(TextFormat.clean(username));

        if (!this.loginData.getChainData().getSkin().isValid()) {
            session.disconnect("disconnectionScreen.invalidSkin");
            return true;
        }

        PlayerPreLoginEvent playerPreLoginEvent;
        this.server.getPluginManager().callEvent(playerPreLoginEvent = new PlayerPreLoginEvent(loginData, "Plugin reason"));
        if (playerPreLoginEvent.isCancelled()) {
            session.disconnect(playerPreLoginEvent.getKickMessage());
            return true;
        }
        session.setPacketHandler(new ResourcePackPacketHandler(session, server, loginData));

        PlayerLoginData loginDataInstance = loginData;
        loginData.setPreLoginEventTask(new AsyncTask() {

            private PlayerAsyncPreLoginEvent e;

            @Override
            public void onRun() {
                e = new PlayerAsyncPreLoginEvent(loginDataInstance);
                server.getPluginManager().callEvent(e);
            }

            @Override
            public void onCompletion(Server server) {
                if (!loginDataInstance.getSession().isClosed()) {
                    if (e.getLoginResult() == PlayerAsyncPreLoginEvent.LoginResult.KICK) {
                        loginDataInstance.getSession().disconnect(e.getKickMessage());
                    } else if (loginDataInstance.isShouldLogin()) {
                        Player player = loginDataInstance.initializePlayer();

                        for (Consumer<Player> action : e.getScheduledActions()) {
                            action.accept(player);
                        }
                    }
                }
            }
        });

        this.server.getScheduler().scheduleAsyncTask(loginData.getPreLoginEventTask());

        PlayStatusPacket statusPacket = new PlayStatusPacket();
        statusPacket.setStatus(PlayStatusPacket.Status.LOGIN_SUCCESS);
        session.sendPacket(statusPacket);

        session.sendPacket(this.server.getPackManager().getPacksInfos());
        return true;
    }
}
