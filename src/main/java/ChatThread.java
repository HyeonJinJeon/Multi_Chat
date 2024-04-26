import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class ChatThread extends Thread {
    Set<Room> roomSet = new HashSet<>();

    //생성자를 통해서 클라이언트 소켓을 얻어옴.
    private Socket socket;
    private String id;
    private Map<String, PrintWriter> chatClients;
    private String roomName;

    private BufferedReader in;
    PrintWriter out;

    public ChatThread(Socket socket, Map<String, PrintWriter> chatClients) {
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

    @Override
    public void run() {
        System.out.println(id + " 닉네임의 사용자가 연결했습니다.");
        //run
        //연결된 클라이언트가 메시지를 전송하면, 그 메시지를 받아서 다른 사용자들에게 보내줌..
        String msg = null;
        try {
            Scanner stdIn = new Scanner(System.in);

            while ((msg = in.readLine()) != null) {
                if ("/bye".equalsIgnoreCase(msg)){ // 접속 종료
                    System.out.println(id + " 닉네임의 사용자가 연결을 끊었습니다.");
                    break;
                }else if("/list".equalsIgnoreCase(msg)){
                    out.println("채팅방 목록입니다");
                    int cnt = 1;
                    for (Room room : roomSet) {
                        System.out.println(cnt + ": " + room.getName());
                        cnt++;
                    }
                }else if ("/create".equalsIgnoreCase(msg)) {
                    in.readLine();
                    boolean roomExists = false;
                    for (Room room : roomSet) {
                        if (room.getName().equals(roomName)) {
                            roomExists = true;
                            out.println("이미 존재하는 방입니다");
                            break;
                        }
                    }
                    if (!roomExists) {
                        out.println("새로운 채팅방 생성 완료!");
                        Room newRoom = new Room(roomName);
                        newRoom.addClient(id);
                        roomSet.add(newRoom);
                        out.println(newRoom);
                    }
                } else if ("/join".equalsIgnoreCase(msg)) {
                    in.readLine();
                    String roomName = stdIn.nextLine();
                    boolean roomFound = false;
                    for (Room room : roomSet) {
                        if (room.getName().equals(roomName)) {
                            roomFound = true;
                            room.addClient(id);
                            out.println("방에 참여하였습니다.");
                            break;
                        }
                    }
                    if (!roomFound) {
                        out.println("해당하는 방을 찾을 수 없습니다.");
                    }
                } else if(msg.startsWith("/exit")){

                }
                else{
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