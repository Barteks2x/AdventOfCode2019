package day23;

import intcode.Intcode;
import utils.Utils;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Main23 {
    private static final int VM_MX = 2254; // minimum amount of memory that works
    public static long startTime;

    public static void main(String[] args) throws IOException, InterruptedException {
        long t = System.currentTimeMillis();
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                System.out.println("Total time: " + (System.currentTimeMillis() - t) * 0.001)));
        startTime = System.currentTimeMillis();

        BigInteger[] program = Files.readAllLines(Paths.get("run/in23.txt")).stream()
                .flatMap(x -> Arrays.stream(x.split("[,\n\r]+")))
                .map(BigInteger::new).toArray(len -> new BigInteger[VM_MX]);

        for (int i = 0; i < program.length; i++)
            if (program[i] == null) program[i] = BigInteger.ZERO;

        Thread thread = new Network().start(program);

        System.out.println("Initialization time: " + (System.currentTimeMillis() - startTime) * 0.001);
        thread.join();

    }

    private static class Packet {
        int dest;
        BigInteger x, y;
    }

    private static class NIC {
        final Network network;
        final int address;
        boolean queriedAddress;
        boolean isIdle;
        Packet sendingPacket;
        Packet readingPacket;
        final ConcurrentLinkedQueue<Packet> queue = new ConcurrentLinkedQueue<>();

        NIC(Network network, int address) {
            this.network = network;
            this.address = address;
        }

        BigInteger readNextIntword() throws InterruptedException {
            if (!queryAddress()) return BigInteger.valueOf(address);
            if (readingPacket != null) {
                BigInteger y = readingPacket.y;
                readingPacket = null;
                return y;
            }
            Packet nextPacket = queue.poll();
            if (nextPacket == null) {
                if (network.getAndSetIdle(address)) {
                    waitForPacket();
                    nextPacket = queue.poll();
                    if (nextPacket == null) throw new RuntimeException("No next packet?");
                } else return BigInteger.ONE.negate();
            }
            network.unsetIdle(address);
            readingPacket = nextPacket;
            return nextPacket.x;
        }

        void writeNextIntword(BigInteger intword) {
            if (sendingPacket == null) {
                sendingPacket = new Packet();
                sendingPacket.dest = intword.intValueExact();
                return;
            }
            Packet p = sendingPacket;
            if (p.x == null) {
                p.x = intword;
            } else {
                p.y = intword;
                sendingPacket = null;
                network.sendPacket(p, p.dest);
            }
        }

        void waitForPacket() throws InterruptedException {
            synchronized (queue) {
                queue.wait();
            }
        }

        void pushPacket(Packet p) {
            queue.add(p);
            synchronized (queue) {
                queue.notify();
            }
        }

        boolean queryAddress() {
            boolean ret = queriedAddress;
            queriedAddress = true;
            return ret;
        }
    }

    private static class Network {
        final Set<Thread> threads = new HashSet<>();
        final AtomicInteger idleCount = new AtomicInteger();

        final NIC[] states = new NIC[50];
        final Nat nat;

        Network() {
            this.nat = new Nat(this, this::shutdown);
            for (int i = 0; i < states.length; i++) {
                states[i] = new NIC(this, i);
            }
        }

        Thread start(BigInteger[] program) {
            Thread nat = new Thread(this.nat);
            nat.setName("NAT Thread");
            for (int i = 0; i < 50; i++) {
                NIC state = states[i];
                Thread thread = Intcode.makeThread(program, Utils.catching(state::readNextIntword), state::writeNextIntword, VM_MX);
                thread.setName("NIC #" + i);
                threads.add(thread);
                thread.start();
            }
            nat.start();
            return nat;
        }

        @SuppressWarnings("deprecation")
        void shutdown() {
            for (Thread thread : threads) {
                thread.stop();
            }
        }

        void waitForIdle() throws InterruptedException {
            while (idleCount.get() != 50) {
                synchronized (idleCount) {
                    idleCount.wait();
                }
            }
        }

        void sendPacket(Packet p, int dest) {
            if (dest == 255) {
                nat.pushPacket(p);
                return;
            }
            states[dest].pushPacket(p);
        }

        void unsetIdle(int address) {
            if (states[address].isIdle) {
                idleCount.decrementAndGet();
                synchronized (idleCount) {
                    idleCount.notify();
                }
                states[address].isIdle = false;
            }
        }

        // returns: true if was idle before
        private boolean getAndSetIdle(int address) {
            boolean previous;
            if (!(previous = states[address].isIdle)) {
                idleCount.incrementAndGet();
                synchronized (idleCount) {
                    idleCount.notify();
                }
                states[address].isIdle = true;
            }
            return previous;
        }
    }

    private static class Nat implements Runnable {
        final AtomicBoolean hadNat = new AtomicBoolean(false);
        final AtomicReference<Packet> natPacket = new AtomicReference<>();

        final Network network;
        final Runnable shutdown;

        public Nat(Network network, Runnable shutdown) {
            this.network = network;
            this.shutdown = shutdown;
        }

        private boolean checkLastY(BigInteger last, Packet packet) {
            if (packet.y.equals(last)) {
                System.out.println("Twice Y " + last);
                System.out.println("Time to answer: " + (System.currentTimeMillis() - startTime) * 0.001);
                shutdown.run();
                return true;
            }
            return false;
        }

        @Override
        public void run() {
            try {
                BigInteger lastY = null;
                while (true) {
                    waitForNatPacket();
                    network.waitForIdle();
                    Packet packet = takeNatPacket();
                    network.sendPacket(packet, 0);
                    if (checkLastY(lastY, packet)) return;
                    lastY = packet.y;
                }
            } catch (InterruptedException ignored) {
            }
        }

        Packet takeNatPacket() {
            return natPacket.getAndSet(null);
        }

        void waitForNatPacket() throws InterruptedException {
            while (natPacket.get() == null) {
                synchronized (natPacket) {
                    natPacket.wait();
                }
            }
        }

        void pushPacket(Packet p) {
            if (!hadNat.getAndSet(true)) {
                System.out.println("First NAT Dest=255, y=" + p.y);
            }
            natPacket.set(p);
            synchronized (natPacket) {
                natPacket.notify();
            }
        }
    }
}
