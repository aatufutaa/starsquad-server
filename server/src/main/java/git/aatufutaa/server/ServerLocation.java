package git.aatufutaa.server;

public enum ServerLocation {
    EU("EU"),
    NA("NA");

    private final String name;

    ServerLocation(String name) {
        this.name = name;
    }

    public static ServerLocation getByName(String name) {
        for (ServerLocation serverLocation : values()) {
            if (serverLocation.name.equals(name)) return serverLocation;
        }
        return null;
    }
}
