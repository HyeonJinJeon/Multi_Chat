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

            //닉네임이 중복이면 다시 작성할 수 있도록
            while(true){
                String check = in.readLine();
                System.out.println(check);
                if(check.equals("id를 다시 입력해주세요")){
                    System.out.print("Enter your another nickname: ");
                    nickname = stdIn.nextLine();
                    out.println(nickname);
                } else {
                    break;
                }
            }

            String clientAddress = socket.getInetAddress().getHostAddress();
            out.println(clientAddress); // 서버에 IP 주소 전송

            // 서버로부터 메시지를 읽어 화면에 출력하는 별도의 스레드
            Thread readThread = new Thread(new ServerMessageReader(in));
            readThread.start(); // 메시지 읽기 스레드 시작

            // 사용자 입력 처리
            String userInput;
            while (true) {
                userInput = stdIn.nextLine();
                out.println(userInput);

                // '/bye'를 입력하면 클라이언트를 종료합니다.
                if ("/bye".equals(userInput)) {
                    out.println(userInput);
                    break;
                }else if (userInput.startsWith("/create")) {
                    System.out.println("방 이름을 입력하세요");
                    String roomName = stdIn.nextLine();
                    out.println(roomName);
                } else if (userInput.startsWith("/join")) {
                    System.out.println("참여할 방 이름을 입력하세요");
                    String roomName = stdIn.nextLine();
                    out.println(roomName);
                } else if(userInput.startsWith("/exit")){
                    String remove = in.readLine();
                    if(!remove.equals(null)){
                        System.out.println(remove);
                    }
                }
                    // 서버에 메시지를 전송합니다.

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
