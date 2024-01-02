package git.aatufutaa.server.play.confirm;

import git.aatufutaa.server.Server;
import git.aatufutaa.server.net.client.ClientBase;
import git.aatufutaa.server.net.packet.IncomingPacket;
import git.aatufutaa.server.net.packet.OutgoingPacket;
import git.aatufutaa.server.play.session.SessionBase;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedList;
import java.util.Queue;

public class PacketConfirmManager {

    @Getter
    private int latestAcceptedId;

    private int currentOutgoingId;
    private final Queue<QueuedPacket> queue = new LinkedList<>();

    public void handle(ClientBase client, int id, IncomingPacket packet) throws Exception {
        if (this.latestAcceptedId + 1 != id) {
            Server.warn("next id was not expect " + (this.latestAcceptedId + 1) + " " + id);
            // TODO: disconnect should not happen
            return;
        }

        this.latestAcceptedId = id;

        packet.handle(client);
    }

    public void send(SessionBase session, OutgoingPacket packet) {
        // TODO: make sure this is called from main thread only

        int id = ++this.currentOutgoingId;

        ClientBase client = session.getClient();

        // check if too many packets in queue (player not accepting them) then disconnect and clear queue
        if (this.queue.size() > 500) {
            Server.warn("too many packets in queue for " + session);
            if (client != null) {
                client.getChannel().close();
            }
            this.reset();
            return;
        }

        QueuedPacket queuedPacket = new QueuedPacket(id, packet);
        this.queue.add(queuedPacket);

        if (client != null && client.isDataLoaded())
            session.sendPacket(new ConfirmOutgoingPacket(id, packet));
    }

    public void onClientConfirm(int latestClientAcceptedId) {
        while (!this.queue.isEmpty()) {
            QueuedPacket packet = this.queue.peek();
            if (packet.id <= latestClientAcceptedId) {
                System.out.println("removed already handled packet " + packet.id);
                this.queue.poll();
            } else {
                break;
            }
        }
    }

    public void onConnected(ClientBase client, int latestClientAcceptedId) {
        System.out.println("on connected " + latestClientAcceptedId);
        this.onClientConfirm(latestClientAcceptedId); // remove old packets

        System.out.println("sending packets " + this.queue.size());
        // send new packets
        for (QueuedPacket packet : this.queue) {
            client.sendPacket(new ConfirmOutgoingPacket(packet.id, packet.packet));
        }
    }

    // if connect
    public void reset() {
        this.queue.clear();
        this.currentOutgoingId = 0;
        this.latestAcceptedId = 0;
    }

    @AllArgsConstructor
    public static class QueuedPacket {
        private int id;
        private OutgoingPacket packet;
    }
}
