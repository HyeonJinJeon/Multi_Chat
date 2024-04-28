import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class ChatThread extends Thread {
    private Set<Room> roomSet = new HashSet<>(); //채팅망 객체 목록
    private Set<String> clients = new HashSet<>(); //사용자 목록
    private BadWords badWords = new BadWords(); //혐오 발언 객체 생성


    //생성자를 통해서 클라이언트 소켓을 얻어옴.
    private Socket socket;
    private String id;
    private Map<String, PrintWriter> chatClients;
    private String roomName;

    private BufferedReader in;
    private PrintWriter out;
    private FileWriter fw;

    public ChatThread(Socket socket, Map<String, PrintWriter> chatClients, Set<Room> roomSet, Set<String> clients) {
        this.socket = socket;
        this.chatClients = chatClients;
        this.roomSet = roomSet;
        this.clients = clients;

        //클라이언트가 생성될 때 클라이언트로 부터 아이디를 얻어오게 하고 싶어요.
        //각각 클라이언트와 통신 할 수 있는 통로얻어옴.
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //Client가 접속하자마 id를 보낸다는 약속!!

            //id가 담긴 Set을 순회하여 중복 확인
            while(true){
                id = in.readLine();
                //중복일 경우 다시 id를 입력 받도록 보낸다
                if(clients.contains(id)) {
                    out.println("id를 다시 입력해주세요");
                }else {
                    clients.add(id);
                    out.println("새로운 사용자의 아이디는 " + id + "입니다.");
                    break;
                }
            }
            //사용자의 주소를 받아옴
            String clientAddress = in.readLine();

            //서버의 콘솔에 접속한 사용자와 사용자의 주소를 출력
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
        //연결된 클라이언트가 메시지를 전송하면, 그 메시지를 받아서 다른 사용자들에게 보내줌..
        String msg = null;
        try {
            Scanner stdIn = new Scanner(System.in);
            fw = new FileWriter("src/main/java/chatTextFile/waitingRoom.txt", true);
            while ((msg = in.readLine()) != null) {
                if ("/bye".equalsIgnoreCase(msg)){ // 접속 종료
                    System.out.println(id + " 닉네임의 사용자가 연결을 끊었습니다.");
                    break;
                }else if("/list".equalsIgnoreCase(msg)){ //채팅방 목록 보기
                    out.println("채팅방 목록입니다");
                    int cnt = 1;
                    for (Room room : roomSet) {
                        out.println(cnt + ": " + room.getName());
                        cnt++;
                    }
                }else if ("/create".equalsIgnoreCase(msg)) { //채팅방 생성하기
                    roomName = in.readLine();
                    boolean roomExists = false;
                    //생성하려는 채팅방이 이미 존재하는 경우
                    for (Room room : roomSet) {
                        if (room.getName().equals(roomName)) {
                            roomExists = true;
                            out.println("이미 존재하는 방입니다");
                            break;
                        }
                    }
                    //생성하려는 채팅방이 없는 경우 채팅방 생성
                    if (!roomExists) {
                        out.println("새로운 채팅방 생성 완료!");
                        Room newRoom = new Room(roomName);
                        //새롭게 생성한 채팅방에 입장
                        newRoom.addClient(id);
                        out.println("\"" + roomName + "\"" + "방에 참여하였습니다.");
                        roomSet.add(newRoom);
                    }
                } else if ("/join".equalsIgnoreCase(msg)) { //채팅방 참사하기
                    roomName = in.readLine();
                    out.println(roomName);
                    boolean roomFound = false;
                    for (Room room : roomSet) {
                        //참가하려는 채팅방이 존재하는 경우
                        if (room.getName().equals(roomName)) {
                            roomFound = true;
                            room.addClient(id);
                            out.println("\"" + roomName + "\"" + " 방에 참여하였습니다.");
                            joinRoom(id);
                            break;
                        }
                    }
                    //참가하려는 채팅바잉 존재하지 않는 경우
                    if (!roomFound) {
                        out.println("해당하는 방을 찾을 수 없습니다.");
                    }
                } else if(msg.startsWith("/exit")){ //채팅방 나가기
                    boolean clientFound = false;
                    //현재 사용자가 채팅방에 있는 경우
                    for (Room room : roomSet) {
                        if (room.getClients().contains(id)) {
                            clientFound = true;
                            exitRoom(id);
                            room.removeClient(id);
                            out.println("\"" + room.getName() + "\"" + "방에서 퇴장했습니다.");
                            //채팅방에 아무도 없으면 채팅방 삭제
                            if(room.getClients().isEmpty()){
                                out.println("\"" + room.getName() + "\"" + "방이 삭제되었습니다.");
                                roomSet.remove(room);
                            }
                            break;
                        }
                    }
                    //현재 사용자가 채팅방에 없는 경우
                    if (!clientFound) {
                        out.println("현재 채팅방에 있지 않습니다.");
                    }
                }else if(msg.startsWith("/to")){ //귓속말 기능
                    String[] parts = msg.split(" ", 3);
                    whisper(id, parts);
                }else if(msg.startsWith("/users")){ // 전체 유저 목록 확인
                    out.println("전체 유저 목록입니다");
                    for(String client : clients){
                        out.println(client);
                    }
                }else if(msg.startsWith("/roomusers")){ // 채팅방에 있는 유저 목록 확인
                    for (Room room : roomSet){
                        if (room.getClients().contains(id)) {
                            out.println("\"" + room.getName() + "\"" + " 채팅방 유저 목록입니다");
                            for(String client : room.getClients()){
                                out.println(client);
                            }
                        } else{
                            out.println("방에 참가하고 있지 않습니다");
                        }
                    }
                }
                else{
                    //채팅방에 있는 사용자에게 채팅 보내기
                    broadcast(id + " : " + msg, roomName);
                }

            }
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        } finally {
            synchronized (chatClients) {
                chatClients.remove(id);
            }
            //사용자가 채팅방에서 나갔을 경우 다른 사용자에게 알려주기
            broadcast(id + "님이 채팅에서 나갔습니다.", roomName);

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

    //방 클라이언트에게 알려주는 메서드
    public void broadcast(String msg, String roomName) {
        for(Room room : roomSet){
            if(room.getClients().contains(id)){
                for(String client : room.getClients()){
                    PrintWriter out = chatClients.get(client);
                    if (out != null) {
                        String filteringMsg = badWords.filteringBadWords(msg);
                        out.println(filteringMsg);
                    }
                }
                try{
                    fw = new FileWriter("src/main/java/chatTextFile/" + roomName + "_log.txt", true);
                    fw.write(msg + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    if (fw != null) {
                        try {
                            fw.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
    //귓속말 기능
    public void whisper(String id, String[] parts) {
        for(Room room : roomSet){
            if(room.getClients().contains(id)){
                if (room.getClients().contains(parts[1])){
                    chatClients.get(parts[1]).println("[귓속말] " + id + " : " + parts[2]);
                }else{
                    out.println("해당 사용자가 없습니다");
                }
            }
        }
    }
    //방 사람인지 확인하는 메서드
    public void joinRoom(String id) {
        for(Room room : roomSet){
            if(room.getClients().contains(id)){
                for(String client : room.getClients()){
                    PrintWriter out = chatClients.get(client);
                    out.println(id + " 사용자가 입장했습니다.");;
                }
            }
        }
    }
    //방에서 나가는 사람 확인하는 메서드
    public void exitRoom(String ㅑㅇ) {
        for(Room room : roomSet){
            if(room.getClients().contains(id)){
                for(String client : room.getClients()){
                    PrintWriter out = chatClients.get(client);
                    out.println(id + " 사용자가 퇴장했습니다.");;
                }
            }
        }
    }
}