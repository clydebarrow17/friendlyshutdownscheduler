package friendlyshutdownscheduler;

import java.io.BufferedReader;
import java.io.File;
// import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
// import java.io.PrintWriter;
import java.util.Scanner;


public class mainfile 
{
    public static void main(String[] args) 
    {
        /*
        * TODO : 
        * 
        * Create specific methods for:
        *      Shutdown schedule create [DONE]
        *           Add user input for comment (using /c "comment") [DONE | Used comment parameter to store shutdown schedule]
        *
        *           
        *      Shutdown schedule delete [DONE]
        *           Add user input for reason of deletion [OPTIONAL]
        *       * Find a way to check for the time of the shutdown

        *      Check if shutdown schedule is created (using wevutil)
        *      Check if shutdown schedule is deleted (using wevutil)
        * 
        * User input for time to be used for shutdown schedule
        * 
        * Getting the current date (and time)
        * Getting the projected date (and time) after time input for schedule
        * Send notification 5 minutes before the time // OPTIONAL
        * Testing in console
        * 
        * Separate .java file for each major function
        *      // Do I _STILL_ need this? -Clyde
        * 
        * GUI integration
        *      Input/s
        *      Buttons
        *      Clock (TimePanel? TimePicker? TextBox?)
        *      Date (CalendarPicker? TextBox?) // Do I _REALLY_ need this? -Clyde
        * 
        * Compilation
        * Persistence (minimize to system tray, aware of previous session [run methods on first try to fill variables])
        * 
        */
        
        Scanner userinputscanner = new Scanner(System.in);
        System.out.print("option: ");
        String userinput = userinputscanner.nextLine();
        
        /*
        * Options:
        * 1 = Schedule shutdown
        * 2 = Delete scheduled shutdown
        * 3 = Date and time of schedule shutdown (Expected time of shutdown not included | TODO)
        * 4 = Date and time of deletion of schedule shutdown
        * 
        * 
        */
        
        if(userinput.equals("1"))
        {            
            Scanner inputinseconds = new Scanner(System.in);
            
            System.out.println("Please input the time to shutdown (In seconds):");
            String stringinseconds = inputinseconds.nextLine();
            
            try 
            {
                int checker = Integer.parseInt(stringinseconds);
                shutdowntocmd(stringinseconds);                
            } 
            catch (NumberFormatException e)
            {
                System.out.println("Error: " + e);
                System.out.println("=====");
                System.out.println("Must input an integer.");
            }
            catch (Exception e) 
            {
                System.out.println("Error: " + e);
            }
            
            inputinseconds.close();
        }
        else if(userinput.equals("2"))
        {
            deleteshutdownfromcmd();
        }
        else if(userinput.equals("3"))
        {
            checkeventlog_schedule();
        }
        else if(userinput.equals("4"))
        {
            checkeventlog_deletionschedule();
        }
        
        userinputscanner.close();
        
        
    }
    
    static void shutdowntocmd(String timeinseconds)
    {
        
        ProcessBuilder processBuilder = new ProcessBuilder();        
        
        File filetorun = new File("SAMPLE");
        
        processBuilder.directory(filetorun);
        processBuilder.command("cmd.exe", "/c", "shutdown /s /t " + timeinseconds + " /c \"Shutdown scheduled after " + timeinseconds + " seconds.\"");
        // processBuilder.command("cmd.exe", "/c", "wevtutil qe System /c:1 /rd:true /f:text /q:\"Event[System[(EventID=1075)]]\"");
        
        /*
        * Tentative commands:
        * 
        * processBuilder.command("cmd.exe", "/c", "shutdown /s /t [time]"); -> 
        *      Schedules the shutdown.
        *      Can use different types of shutdown options:
        *          Shutdown = /s
        *          Log off = /l
        *          Restart (Full shutdown + restart) = /r
        *          Hibernate = /h
        *      *Refer to "shutdown /?" in cmd for options such as adding comment on reason of shutdown, and ect
        *      // Error code 1190 in output (cmd) if shutdown is already scheduled
        *      // Error code 1116 in output (cmd) if shutdown wasnt scheduled in the first place.
        * 
        * 
        * processBuilder.command("cmd.exe", "/c", "shutdown /a");
        *      Aborts the scheduled shutdown.
        * 
        * processBuilder.command("cmd.exe", "/c", "wevtutil qe System /c:1 /rd:true /f:text /q:\"Event[System[(EventID=1075)]]\"");
        *      Checks the most recent event log of the "shutdown /a" based on the event ID.
        *      Refer to EventID=1074 for the schedule of the shutdown
        *      Currently used to identify whether or not the shutdown was scheduled already or not. 
        *      // Is this for persistence? -Clyde
        * 
        * 
        * 
        */
        
        // wevtutil qe System /c:1 /rd:true /f:text /q:"Event[System[(EventID=1075)]]"
        // NOTE : Line above is for shutdown.exe abort (Remove shutdown).
        //      Refer to EventID = 1074 for shutdown.exe creation,
        //      Error code 1190 if shutdown is already scheduled, and
        //      Error code 1116 if shutdown wasnt scheduled in the first place.
        
        
        
        try {
            
            Process process = processBuilder.start();
            
            BufferedReader reader =
            new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            String datehere = "";
            
            
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if(line.contains("Date"))
                {
                    datehere = line;
                }
            }
            
            if (!datehere.equals(""))
            {
                // NOTE : Only runs for .command() that outputs an event log.
                String dateheretimeonly = datehere.trim().substring(datehere.indexOf("T") - 1, datehere.indexOf(".") - 2);
                String dateheredateonly = datehere.trim().substring(datehere.indexOf("e") + 1, datehere.indexOf("T") - 2);
                
                System.out.println("date string: " + datehere.trim());
                System.out.println("time only: " + dateheretimeonly);
                System.out.println("date only: " + dateheredateonly);
            }
            
            
            int exitCode = process.waitFor(); 
            // NOTE : Waits for process Process data type to finish before proceeding below.
            //      Is this similar to .pause()? -Clyde
            
            // System.out.println("\nExited with error code : " + exitCode);
            if(exitCode == 1190)
            {
                System.out.println("Error: There is a current scheduled shutdown.");
                
                // TODO/COMMENT : Does event log checking goes here as well?
                //      Checking for details of the scheduled shutdown?
            }
            else
            {
                System.out.println("\nExited with error code : " + exitCode);
                System.out.println("=====");
                System.out.println("Schedule shutdown set.");
            }
            
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        } 
        catch (InterruptedException e) 
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e);
        }
        
    }
    
    static void deleteshutdownfromcmd()
    {
        ProcessBuilder processBuilder = new ProcessBuilder();        
        
        File filetorun = new File("SAMPLE");
        
        processBuilder.directory(filetorun);
        processBuilder.command("cmd.exe", "/c", "shutdown /a");
        
        try 
        {
            Process process = processBuilder.start();
            
            BufferedReader reader =
            new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            
            while ((line = reader.readLine()) != null) 
            {
                System.out.println(line);
            }
            
            int exitCode = process.waitFor();         
            
            if(exitCode == 1116)
            {
                System.out.println("Error: No scheduled shutdown to delete.");
            }
            else
            {
                System.out.println("\nExited with error code : " + exitCode);
                System.out.println("=====");
                System.out.println("Schedule deleted.");
            }
            
            
            
        } 
        catch (Exception e) 
        {
            System.out.println("Error: " + e);
        }
        
    }
    
    static void checkeventlog_schedule()
    {
        ProcessBuilder processBuilder = new ProcessBuilder();        
        
        File filetorun = new File("SAMPLE");
        
        processBuilder.directory(filetorun);
        
        processBuilder.command("cmd.exe", "/c", "wevtutil qe System /c:1 /rd:true /f:text /q:\"Event[System[(EventID=1074)]]\"");
        
        
        try {
            
            Process process = processBuilder.start();
            
            BufferedReader reader =
            new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            String datehere = "";
            String timesecondsstring = "";
            
            
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if(line.contains("Date"))
                {
                    datehere = line;
                }
                if(line.contains("Comment"))
                {
                    timesecondsstring = line;
                }

            }
            
            if (!datehere.equals(""))
            {
                // NOTE : Only runs for .command() that outputs an event log.
                String dateheretimeonly = datehere.trim().substring(datehere.indexOf("T") - 1, datehere.indexOf(".") - 2);
                String dateheredateonly = datehere.trim().substring(datehere.indexOf("e") + 1, datehere.indexOf("T") - 2);
                
                System.out.println("date string: " + datehere.trim());
                System.out.println("=====");
                System.out.println("Schedule shutdown date and time: " );
                System.out.println("time only: " + dateheretimeonly);
                System.out.println("date only: " + dateheredateonly);
            }
            
            if (!timesecondsstring.equals(""))
            {
                String timeseconds = timesecondsstring.trim().substring(timesecondsstring.indexOf("r") + 1, timesecondsstring.indexOf(".") - 2);
                
                System.out.println("date string: " + timeseconds.trim());
                System.out.println("=====");
                System.out.println("Time to shutdown: " );
                System.out.println("seconds only: " + timeseconds);
            }


            int exitCode = process.waitFor(); 
            
            System.out.println("\nExited with error code : " + exitCode);
            
        } 
        catch (Exception e)
        {
            System.out.println("Error: " + e);
        }
    }

    static void checkeventlog_deletionschedule()
    {
        ProcessBuilder processBuilder = new ProcessBuilder();        
        
        File filetorun = new File("SAMPLE");
        
        processBuilder.directory(filetorun);
        
        processBuilder.command("cmd.exe", "/c", "wevtutil qe System /c:1 /rd:true /f:text /q:\"Event[System[(EventID=1075)]]\"");
        
        
        try {
            
            Process process = processBuilder.start();
            
            BufferedReader reader =
            new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            String datehere = "";
            
            
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if(line.contains("Date"))
                {
                    datehere = line;
                }
            }
            
            if (!datehere.equals(""))
            {
                // NOTE : Only runs for .command() that outputs an event log.
                String dateheretimeonly = datehere.trim().substring(datehere.indexOf("T") - 1, datehere.indexOf(".") - 2);
                String dateheredateonly = datehere.trim().substring(datehere.indexOf("e") + 1, datehere.indexOf("T") - 2);
                
                System.out.println("date string: " + datehere.trim());
                System.out.println("=====");
                System.out.println("Schedule shutdown deletion date and time: " );
                System.out.println("time only: " + dateheretimeonly);
                System.out.println("date only: " + dateheredateonly);
            }
            
            int exitCode = process.waitFor(); 
            
            System.out.println("\nExited with error code : " + exitCode);
            
        } 
        catch (Exception e)
        {
            System.out.println("Error: " + e);
        }

    }
    
}
