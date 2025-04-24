package entity;

/**
 * Base User class for the BTO system.
 * Represents common attributes and behavior for all user types.
 */
public class User {
    private final String name;
    private final String NRIC;
    private String password;
    private final int age;
    private final MaritalStatus maritalStatus;
    private String role;

    
    public User(String name, String NRIC, String password, int age, MaritalStatus maritalStatus, String role) {
        this.name = name;
        this.NRIC = NRIC;
        this.password = password;
        this.age = age;
        this.maritalStatus = maritalStatus;
        this.role = role;
    }
    
    public User(String name, String NRIC, String password, int age, String maritalStatusStr, String role) {
        this.name = name;
        this.NRIC = NRIC;
        this.password = password;
        this.age = age;
        this.maritalStatus = MaritalStatus.fromString(maritalStatusStr);
        this.role = role;
    }
    
    /**
     * Get user's age
     * @return age of the user
     */
    public int getAge() {
        return this.age;
    }
    
    /**
     * Get user's marital status
     * @return marital status of the user
     */
    public MaritalStatus getMaritalStatus() {
        return this.maritalStatus;
    }
    
    /**
     * Get marital status display value
     * @return string representation of marital status
     */
    public String getMaritalStatusDisplayValue() {
        return this.maritalStatus.getDisplayValue();
    }
    
    /**
     * Get user's name
     * @return name of the user
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Get user's NRIC
     * @return NRIC of the user
     */
    public String getNRIC() {
        return this.NRIC;
    }
    
    /**
     * Get user's password
     * @return password of the user
     */
    public String getPassword() {
        return this.password;
    }
    
    /**
     * Set user's password
     * @param password new password
     */
    public void setPassword(String password) {
        this.password = password;
        
    }
    
    /**
     * Get user's role
     * @return role of the user
     */
    public String getRole() {
        return this.role;
    }
    
    /**
     * Verify login with provided password
     * @param inputPassword password to check
     * @return true if password matches, false otherwise
     */
    public boolean login(String inputPassword) {
        return this.password.equals(inputPassword);
    }
    
    /**
     * Change user's password
     * @param newPassword new password to set
     */
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", NRIC='" + NRIC + '\'' +
                ", age=" + age +
                ", maritalStatus='" + maritalStatus + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}