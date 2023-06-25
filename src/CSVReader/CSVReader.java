package CSVReader;

import java.io.*;
import java.util.*;

/**
 * The CSVReader class is responsible for reading, parsing, and saving data from a CSV file.
 * It provides methods to access the parsed data and update the leaderboard with new scores.
 *
 * @see ArrayList
 * @see Arrays
 * @see BufferedReader
 * @see Collections
 * @see Comparator
 * @see File
 * @see FileWriter
 * @see IOException
 */
public class CSVReader
{
    private final File csvFile;
    static ArrayList<ArrayList<String>> parsedFile;
    int maxNumberOfScores = 5;

    /**
     * Constructs a new CSVReader object.
     * Initializes the csvFile and parsedFile variables.
     * Parses the content of the CSV file.
     *
     * @throws IOException if an I/O error occurs while creating the CSV file or reading its content
     */
    public CSVReader()
    {
        csvFile = new File("LeaderBoard.csv");
        try
        {
            csvFile.createNewFile();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        parsedFile = new ArrayList<>();
        parseFile();
    }

    /**
     * Retrieves the parsed file data.
     *
     * @return the parsed file data as an ArrayList of ArrayList of strings
     */
    public ArrayList<ArrayList<String>> getParsedFile() { 
    	return parsedFile; }
    
    private void parseFile()
    {
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(csvFile));
            String line;

            while ((line = br.readLine()) != null)
            {
                parsedFile.add(new ArrayList<>(Arrays.asList(line.split(","))));
            }
        } catch (IOException ioException)
        {
            ioException.printStackTrace();
        }
    }

    /**
     * Saves a new score to the leaderboard for the specified player.
     * If the player already exists, their score will be updated if the new score is better.
     * The leaderboard will be sorted in descending order based on scores.
     * If the player does not exist, a new entry will be added to the leaderboard.
     * If the leaderboard exceeds the maximum number of scores, the lowest score will be removed.
     * Finally, the updated leaderboard will be saved to the CSV file.
     *
     * @param score the score to be saved
     * @param nick  the player's nickname
     */
    public void saveScore(int score, String nick)
    {
        boolean playerExists = false;
        boolean playerScoreUpdated = false;

        // Update player score if the player exists in the leaderboard AND if the score is better
        for  (int i = 0; i < parsedFile.size(); ++i)
        {
            if (parsedFile.get(i).get(0).equals(nick))
            {
                playerExists = true;
                if (Integer.parseInt(parsedFile.get(i).get(1)) < score)
                {
                    playerScoreUpdated = true;
                    ArrayList<String> tmp = parsedFile.get(i);
                    tmp.set(1, String.valueOf(score));
                    parsedFile.set(i, tmp);
                }
            }
        }

        if (playerExists && playerScoreUpdated)
        {
            parsedFile.sort(Collections.reverseOrder(Comparator.comparingInt(a -> Integer.parseInt(a.get(1)))));
        }
        else if (!playerExists)
        {
            parsedFile.add(new ArrayList<>(Arrays.asList(nick, String.valueOf(score))));
            parsedFile.sort(Collections.reverseOrder(Comparator.comparingInt(a -> Integer.parseInt(a.get(1)))));

            if (parsedFile.size() > maxNumberOfScores)
            {
                parsedFile.remove(parsedFile.size() - 1);
            }
        }
        saveNewFile();
    }

    private void saveNewFile()
    {
        try
        {
            FileWriter csvWriter = new FileWriter(csvFile);

            for (ArrayList<String> strings : parsedFile)
            {
                csvWriter.append(strings.get(0));
                csvWriter.append(",");
                csvWriter.append(strings.get(1));
                csvWriter.append("\n");
            }
            csvWriter.flush();
            csvWriter.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
