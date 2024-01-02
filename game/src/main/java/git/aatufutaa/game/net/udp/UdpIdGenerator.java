package git.aatufutaa.game.net.udp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UdpIdGenerator {

    private static final UdpIdGenerator GENERATOR = new UdpIdGenerator();
    private final Queue<Short> list = new ConcurrentLinkedQueue<>();

    private UdpIdGenerator() {
        List<Short> list = new ArrayList<>(Short.MAX_VALUE*2);
        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
            list.add((short)i);
        }
        Collections.shuffle(list);
        this.list.addAll(list);
    }

    private Short poll0() {
        return this.list.poll();
    }

    private void release0(short id) {
        this.list.add(id);
    }

    public static short poll() {
        return GENERATOR.poll0();
    }

    public static void release(short id) {
        GENERATOR.release0(id);
    }
}
