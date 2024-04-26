import java.io.*;
import java.net.Socket;
import java.util.*;

public class ChatClient {
    public static void main(String[] args) {
        Set<Room> roomSet = new HashSet<>();
        String hostName = "localhost"; // 서버가 실행 중인 호스트의 이름 또는 IP 주소
        int portNumber = 12345; // 서버와 동일한 포트 번호 사용

        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        try{
            socket = new Socket(hostName, portNumber);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner stdIn = new Scanner(System.in);

            System.out.print("Enter your nickname: ");
            String nickname = stdIn.nextLine();
            out.println(nickname); // 서버에 닉네임을 전송
            String clientAddress = socket.getInetAddress().getHostAddress();
            out.println(clientAddress); // 서버에 IP 주소 전송

            // 서버로부터 메시지를 읽어 화면에 출력하는 별도의 스레드
            Thread readThread = new Thread(new ServerMessageReader(in));
            readThread.start(); // 메시지 읽기 스레드 시작

            // 사용자 입력 처리
            String userInput;
            while (true) {
                userInput = stdIn.nextLine();

                // '/bye'를 입력하면 클라이언트를 종료합니다.
                if ("/bye".equals(userInput)) {
                    out.println(userInput);
                    break;
                }else if(userInput.startsWith("/list")){
                    System.out.println("채팅방 목록입니다");
                    int cnt = 1;
                    for (Room room : roomSet) {
                        System.out.println(cnt + ": " + room.getName());
                        cnt++;
                    }
                }else if (userInput.startsWith("/create")) {
                    System.out.println("방 이름을 입력하세요");
                    String roomName = stdIn.nextLine();
                    boolean roomExists = false;
                    for (Room room : roomSet) {
                        if (room.getName().equals(roomName)) {
                            roomExists = true;
                            System.out.println("이미 존재하는 방입니다");
                            break;
                        }
                    }
                    if (!roomExists) {
                        System.out.println("새로운 채팅방 생성 완료!");
                        Room newRoom = new Room(roomName);
                        newRoom.addClient(nickname);
                        roomSet.add(newRoom);
                        out.println(newRoom);
                    }
                } else if (userInput.startsWith("/join")) {
                    System.out.println("참여할 방 이름을 입력하세요");
                    String roomName = stdIn.nextLine();
                    boolean roomFound = false;
                    for (Room room : roomSet) {
                        if (room.getName().equals(roomName)) {
                            roomFound = true;
                            room.addClient(nickname);
                            System.out.println("방에 참여하였습니다.");
                            break;
                        }
                    }
                    if (!roomFound) {
                        System.out.println("해당하는 방을 찾을 수 없습니다.");
                    }
                } else if(userInput.startsWith("/exit")){

                }

                // 서버에 메시지를 전송합니다.
                out.println(userInput);
            } // while

            // 클라이언트와 서버는 명시적으로 close를 합니다. close를 할 경우 상대방쪽의 readLine()이 null을 반환됩니다. 이 값을 이용하여 접속이 종료된 것을 알 수 있습니다.
            in.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            System.out.println("Exception caught when trying to connect to " + hostName + " on port " + portNumber);
            e.printStackTrace();
        }
    }
}
