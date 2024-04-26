import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Room implements Runnable{
    private String name;
    private Set<String> clients = new HashSet<>();

    private Socket socket;
    private String id;
    private Map<String, PrintWriter> chatClients;
    private String roomName;

    private BufferedReader in;
    PrintWriter out;
    public Room(Socket socket, Map<String, PrintWriter> chatClients) {
        this.socket = socket;
        this.chatClients = chatClients;

        //클라이언트가 생성될 때 클라이언트로 부터 아이디를 얻어오게 하고 싶어요.
        //각각 클라이언트와 통신 할 수 있는 통로얻어옴.
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //Client가 접속하자마 id를 보낸다는 약속!!
            id = in.readLine();
            String clientAddress = in.readLine();
            //이때..  모든 사용자에게 id님이 입장했다라는 정보를 알려줌.
            broadcast(id + "님이 입장하셨습니다." );
            System.out.println("새로운 사용자의 아이디는 " + id + "입니다." + "(" + clientAddress + ")");

            //동시에 일어날 수도..
            synchronized (chatClients) {
                chatClients.put(this.id, out);
            }


        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
    public Room(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addClient(String username) {
        clients.add(username);
    }

    public void removeClient(String username) {
        clients.remove(username);
    }

    @Override
    public void run() {
        System.out.println(id + " 닉네임의 사용자가 연결했습니다.");
        //run
        //연결된 클라이언트가 메시지를 전송하면, 그 메시지를 받아서 다른 사용자들에게 보내줌..
        String msg = null;
        try {
            while ((msg = in.readLine()) != null) {
                if ("/bye".equalsIgnoreCase(msg)){ // 접속 종료
                    System.out.println(id + " 닉네임의 사용자가 연결을 끊었습니다.");
                    break;
                }else if(msg.startsWith("/to")){ // 귓속말
                    String[] parts = msg.split(" ", 3);
                    for (String name : chatClients.keySet()){
                        if(name.equalsIgnoreCase(parts[1])){
                            chatClients.get(parts[1]).println("[귓속말] " + id + " : " + parts[2]);
                            break;
                        }
                    }
                }else if(msg.startsWith("/list")){ // 방 목록 보기

                }else if(msg.startsWith("/create")){ // 방 생성

                }else if(msg.startsWith("/join")){ // 방 입장

                }else if(msg.startsWith("/exit")){ // 방 나가기

                }else{
                    broadcast(id + " : " + msg);
                }
            }

        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        } finally {
            synchronized (chatClients) {
                chatClients.remove(id);
            }
            broadcast(id + "님이 채팅에서 나갔습니다.");

            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    //전체 사용자에게 알려주는 메서드
    public void broadcast(String msg) {
        for (PrintWriter out : chatClients.values()) {
            out.println(msg);
//            out.flush();
        }
    }
}
