
public class App {
    private static final String HOST = "127.0.0.1";

    public static void main(String[] args) {
        final String port = System.getenv("rebase_server_port");
        System.out.println("rebase_server_port: " + port);

        final String message;
        if (args != null && 0 < args.length) {
            message = args[0];
        } else {
            message = "メッセージはありません";
        }
        int exit = new RebaseProxyClient().start(HOST, Integer.parseInt(port), message);
        System.exit(exit);
    }
}
