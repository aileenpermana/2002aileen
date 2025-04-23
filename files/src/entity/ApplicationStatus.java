package entity;

public enum ApplicationStatus {
    PENDING("Pending"),
    SUCCESSFUL("Successful"),
    UNSUCCESSFUL("Unsuccessful"),
    BOOKED("Booked"),
    WITHDRAW("Withdrawal Requested");  // Add this new status with clear display value
    
    private final String displayValue;
    
    ApplicationStatus(String displayValue) {
        this.displayValue = displayValue;
    }
    
    public String getDisplayValue() {
        return displayValue;
    }
    
    public static ApplicationStatus fromDisplayValue(String displayValue) {
        for (ApplicationStatus status : values()) {
            if (status.getDisplayValue().equalsIgnoreCase(displayValue)) {
                return status;
            }
        }
        return null;
    }
}