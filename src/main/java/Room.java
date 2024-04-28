import java.util.HashSet;
import java.util.Set;

public class Room{
    private String name;
    private Set<String> clients = new HashSet<>();

    public Room(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Set<String> getClients() {
        return clients;
    }

    public void addClient(String username) {
        clients.add(username);
    }

    public void removeClient(String username) {
        clients.remove(username);
    }
}
