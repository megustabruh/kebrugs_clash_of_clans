package per.coc.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class COCWarReader {

    public static String fetchClanWarDetails(String apiKey, String clanTag) {
        try {
            // Construct the API endpoint URL for clan war details
            String endpoint = "https://api.example-game.com/clanwar/clans/" + clanTag + "/wars";

            // Set up the HttpURLConnection
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");

            // Check if the request was successful (status code 200)
            if (connection.getResponseCode() == 200) {
                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                // Return the clan war details
                return response.toString();
            } else {
                // Print an error message if the request was not successful
                System.out.println("Error: " + connection.getResponseCode() + " - " + connection.getResponseMessage());
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        // Replace 'YOUR_API_KEY' and 'CLAN_TAG' with actual values
        String apiKey = "YOUR_API_KEY";
        String clanTag = "CLAN_TAG";

        // Fetch clan war details
        String clanWarDetails = fetchClanWarDetails(apiKey, clanTag);

        if (clanWarDetails != null) {
            System.out.println("Clan War Details:");
            System.out.println(clanWarDetails);
        }
    }
}