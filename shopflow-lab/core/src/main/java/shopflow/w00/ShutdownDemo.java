package shopflow.w00;

/**
 * Week 2: see the difference between SIGTERM and SIGKILL from a Java program.
 *
 *   java core/src/main/java/shopflow/w00/ShutdownDemo.java
 *   # in another terminal:  kill -TERM <pid>   -> the shutdown hook prints "cleaning up"
 *   #                       kill -9 <pid>      -> nothing is printed: the JVM never got a chance
 *
 * Find the pid with:  pgrep -f ShutdownDemo
 */
public class ShutdownDemo {

    public static void main(String[] args) throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                System.out.println("[hook] SIGTERM received: finishing in-flight work and cleaning up")));
        System.out.println("Running as pid " + ProcessHandle.current().pid() + ". Waiting to be stopped...");
        while (true) {
            Thread.sleep(1000);
        }
    }
}
