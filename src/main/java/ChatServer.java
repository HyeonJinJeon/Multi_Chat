import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ChatServer {
    public static void main(String[] args) {
        Set<Room> roomSet = new HashSet<>();
        Set<String> clients = new HashSet<>();

        //1. 서버소켓을 생성!!
        try (ServerSocket serverSocket = new ServerSocket(12345);) {
            System.out.println("서버가 준비되었습니다.");
            //여러명의 클라이언트의 정보를 기억할 공간
            Map<String, PrintWriter> chatClients = new HashMap<>();

            while (true) {
                //2. accept() 를 통해서 소켓을 얻어옴.   (여러명의 클라이언트와 접속할 수 있도록 구현)
                Socket socket = serverSocket.accept();
                //Thread 이용!!
                //여러명의 클라이언트의 정보를 기억할 공간
                new ChatThread(socket, chatClients, roomSet, clients).start();
//                new Room(socket, chatClients).run();

            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
