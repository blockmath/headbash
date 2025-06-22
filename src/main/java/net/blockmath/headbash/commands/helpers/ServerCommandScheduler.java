package net.blockmath.headbash.commands.helpers;

import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

public class ServerCommandScheduler {
    public static final Map<MinecraftServer, DelayedCommandScheduler> SCHEDULERS = new WeakHashMap<>();

    public static DelayedCommandScheduler get(MinecraftServer server) {
        return SCHEDULERS.computeIfAbsent(server, s -> new DelayedCommandScheduler());
    }

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        get(event.getServer()).tick();
    }

    public static class DelayedCommandScheduler {
        private final List<DelayedCommand> mainQueue = new ArrayList<>();
        private final List<DelayedCommand> pendingQueue = new ArrayList<>();
        private boolean ticking = false;

        public void schedule(Runnable task, int delayTicks) {
            //System.out.println("Scheduling task for " + delayTicks + " ticks");
            if (delayTicks <= 0) {
                task.run();
                return;
            }

            DelayedCommand cmd = new DelayedCommand(task, delayTicks);
            if (ticking) {
                pendingQueue.add(cmd);  // avoid modifying during iteration
            } else {
                mainQueue.add(cmd);
            }
        }

        public void tick() {
            ticking = true;
            mainQueue.removeIf(DelayedCommand::tick);
            ticking = false;
            if (!pendingQueue.isEmpty()) {
                mainQueue.addAll(pendingQueue);
                pendingQueue.clear();
            }
        }



        private static class DelayedCommand {
            private final Runnable task;
            private int ticksRemaining;

            public DelayedCommand(Runnable task, int ticks) {
                this.task = task;
                this.ticksRemaining = ticks;
            }

            public boolean tick() {
                if (--ticksRemaining <= 0) {
                    task.run();
                    return true;
                } else {
                    return false;
                }
            }
        }
    }
}


