package Client;


import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Scanner;

public class UserInput implements Runnable {
    private SocketClient socketClient;
    private String name = "Мистер Инкогнито";
    private String forName = "Введите имя";
    private String msg = "Введите сообщение либо '/exit' чтоб покинуть чат";

    public UserInput(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    @Override
    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println(forName);
            name = scanner.nextLine();

            System.out.println(msg);
            while (true) {
                String dateTime = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM).format(LocalDateTime.now());
                String msg = scanner.nextLine();
                socketClient.write(dateTime, name, msg);
                if (socketClient.readyToQuit(msg)) {
                    break;
                }
            }
        }
    }
}
