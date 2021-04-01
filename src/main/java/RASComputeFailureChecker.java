import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class RASComputeFailureChecker {
    public static boolean CheckForSolutionSolverFailed(String computeLogFilePath){
        boolean RASFails = false;
        String RASFailureIndicator = "Message  |    ****   ERROR:  Solution Solver Failed   ****";
        BufferedReader brp = null;
        String line = "";
        try {
            brp = new BufferedReader(new FileReader(computeLogFilePath));
            String[] tmp = null;
            while ((line = brp.readLine()) != null){
                 if(line.equals(RASFailureIndicator))
                {
                    RASFails = true;
                }
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            RASFails = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return RASFails;
    }
}
