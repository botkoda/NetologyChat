package Client;


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Scanner;

public class UserInputHander implements Runnable {
    private int sleep = 1000;
    private SocketClient socketClient;
    private String name = "Мистер Инкогнито";
    private String forName = "Введите имя";
    private String msg = "Введите сообщение либо '/exit' чтоб покинуть чат";

    public UserInputHander(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    @Override
    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            Thread.sleep(sleep);
            System.out.println(forName);
            name = scanner.nextLine();

            System.out.println(msg);
            while (true) {
                String dateTime = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM).format(LocalDateTime.now());
                String msg = scanner.nextLine();
                socketClient.send(dateTime, name, msg);
                if (socketClient.readyToQuit(msg)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
