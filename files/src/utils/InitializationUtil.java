package utils;

import java.io.*;

/**
 * Utility class for initializing application data
 */
public class InitializationUtil {
    
    /**
     * Initialize default data if not already present
     */
    public static void initializeDefaultData() {
        initializeDirectory();
        initializeApplicantList();
        initializeManagerList();
        initializeOfficerList();
        initializeProjectList();
        initializeEmptyFiles();
    }
    
    /**
     * Ensure the resources directory exists
     */
    private static void initializeDirectory() {
        File directory = new File("files/resources");
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
    
    /**
     * Initialize the applicant list if the file doesn't exist
     */
    private static void initializeApplicantList() {
        File file = new File("files/resources/ApplicantList.csv");
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("Name,NRIC,Age,Marital Status,Password");
                writer.println("John,S1234567A,35,Single,password");
                writer.println("Sarah,T7654321B,40,Married,password");
                writer.println("Grace,S9876543C,37,Married,password");
                writer.println("James,T2345678D,30,Married,password");
                writer.println("Rachel,S3456789E,25,Single,password");
                writer.println("Aileen,S1245678F,21,Single,password");
            } catch (IOException e) {
                System.err.println("Error creating applicant list: " + e.getMessage());
            }
        }
    }
    
    /**
     * Initialize the manager list if the file doesn't exist
     */
    private static void initializeManagerList() {
        File file = new File("files/resources/ManagerList.csv");
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("Name,NRIC,Age,Marital Status,Password");
                writer.println("Michael,T8765432F,36,Single,password");
                writer.println("Jessica,S5678901G,26,Married,password");
            } catch (IOException e) {
                System.err.println("Error creating manager list: " + e.getMessage());
            }
        }
    }
    
    /**
     * Initialize the officer list if the file doesn't exist
     */
    private static void initializeOfficerList() {
        File file = new File("files/resources/OfficerList.csv");
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("Name,NRIC,Age,Marital Status,Password");
                writer.println("Daniel,T2109876H,36,Single,password");
                writer.println("Emily,S6543210I,28,Single,password");
                writer.println("David,T1234567J,29,Married,password");
            } catch (IOException e) {
                System.err.println("Error creating officer list: " + e.getMessage());
            }
        }
    }
    
    /**
     * Initialize the project list if the file doesn't exist
     */
    private static void initializeProjectList() {
        File file = new File("files/resources/ProjectList.csv");
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("ProjectID,ProjectName,Neighborhood,Type1,NumUnits1,Type2,NumUnits2,OpenDate,CloseDate,Manager,OfficerSlots,Visibility");
                writer.println("PRO001,Sunrise Heights,Yishun,2-Room,100,3-Room,150,01/05/2024,01/08/2024,S5678901G,5,true");
                writer.println("PRO002,Garden View,Boon Lay,2-Room,80,3-Room,120,15/05/2024,15/08/2024,S5678901G,3,true");
                writer.println("PRO003,Skyline Residences,Tampines,2-Room,150,3-Room,200,01/06/2024,01/09/2024,T8765432F,6,true");
                writer.println("PRO004,Sunset Sunny,Bugis,2-Room,200,3-Room,100,08/11/2025,11/08/2026,T8765432F,10,false");
                writer.println("PRO005,Acacia Breeze,Yishun,2-Room,20,3-Room,30,02/03/2026,03/08/2026,S5678901G,3,false");
            } catch (IOException e) {
                System.err.println("Error creating project list: " + e.getMessage());
            }
        }
    }
    
    /**
     * Initialize empty files for other data if they don't exist
     */
    private static void initializeEmptyFiles() {
        createEmptyFile("files/resources/ApplicationList.csv", "ApplicationID,ApplicantNRIC,ProjectID,Status,ApplicationDate,StatusUpdateDate,BookedFlatID");
        createEmptyFile("files/resources/OfficerRegistrations.csv", "OfficerNRIC,ProjectID,Status,RegistrationDate");
        createEmptyFile("files/resources/WithdrawalRequests.csv", "ApplicationID,RequestDate,Status");
        createEmptyFile("files/resources/EnquiryList.csv", "EnquiryID,ApplicantNRIC,ProjectID,SubmissionDate,Content,Replies");
        createEmptyFile("files/resources/BookingReceipts.csv", "ReceiptID,ApplicationID,ApplicantNRIC,ProjectID,FlatID,FlatType,GenerationDate");
    }
    
    /**
     * Create an empty file with a header if it doesn't exist
     * @param filePath the file path
     * @param header the CSV header
     */
    private static void createEmptyFile(String filePath, String header) {
        File file = new File(filePath);
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println(header);
            } catch (IOException e) {
                System.err.println("Error creating file " + filePath + ": " + e.getMessage());
            }
        }
    }
}