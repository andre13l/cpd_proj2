import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    public static List<Client> read() {
        String csvFile = "../doc/users.csv";
        String line = "";
        String csvSplitBy = ",";
        List<Client> clients = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Read the header line to skip it
            br.readLine();

            // Read data lines
            while ((line = br.readLine()) != null) {
                String[] data = line.split(csvSplitBy);
                System.out.println("Name: " + data[0] + ", rank: " + data[2]);
                Client client = new Client(data[0], data[1], Integer.parseInt(data[2]), null, "", false);
                clients.add(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return clients;
    }
}
