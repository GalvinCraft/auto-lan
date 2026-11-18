package me.imgalvin.Mixin;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Runs against the real mixin's executor without bootstrapping Minecraft. */
public final class SchedulerShutdownTest {
    public static void main(String[] args) throws Exception {
        if (args.length != 0 && args[0].equals("child")) {
            runChild();
            return;
        }

        Process child = new ProcessBuilder(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("java.class.path"),
                SchedulerShutdownTest.class.getName(), "child")
                .redirectErrorStream(true)
                .start();
        boolean exited;
        String output;
        try {
            exited = child.waitFor(30, TimeUnit.SECONDS);
        } finally {
            // The regression must not leave its own hung JVM behind on failure.
            if (child.isAlive()) {
                child.destroyForcibly();
                if (!child.waitFor(10, TimeUnit.SECONDS)) {
                    throw new AssertionError("Could not terminate scheduler test JVM");
                }
            }
            output = new String(child.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
        System.out.print(output);
        if (!exited) {
            throw new AssertionError("JVM stayed alive after scheduler tasks completed");
        }
        if (child.exitValue() != 0 || !output.contains("COMPLETED daemon=true")) {
            throw new AssertionError("Scheduler child failed: exit=" + child.exitValue());
        }
        System.out.println("PASS: scheduler permits natural JVM exit and remains reusable");
    }

    private static void runChild() throws Exception {
        Field field = Class.forName("me.imgalvin.Mixin.PublishLANServerMixin")
                .getDeclaredField("SCHEDULER");
        field.setAccessible(true);
        ScheduledExecutorService scheduler = (ScheduledExecutorService) field.get(null);
        AtomicReference<Thread> worker = new AtomicReference<>();
        boolean daemon = true;
        // Completing a world's polling task must not prevent a later world's task.
        for (int world = 0; world < 2; world++) {
            ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
                worker.set(Thread.currentThread());
                // Match the existing publication loop's exceptional completion.
                throw new IllegalStateException("poll complete");
            }, 0, 500, TimeUnit.MILLISECONDS);
            try {
                task.get(5, TimeUnit.SECONDS);
                throw new AssertionError("Polling task did not finish exceptionally");
            } catch (ExecutionException expected) {
                if (!(expected.getCause() instanceof IllegalStateException)
                        || !"poll complete".equals(expected.getCause().getMessage())) {
                    throw expected;
                }
            }
            daemon &= worker.get().isDaemon();
            System.out.println("WORLD " + world + " worker=" + worker.get().getName()
                    + " daemon=" + worker.get().isDaemon());
        }
        System.out.println("COMPLETED daemon=" + daemon);
        // Deliberately no shutdown(), System.exit(), or daemonizing test wrapper:
        // returning from main must let this JVM terminate by itself.
    }
}
