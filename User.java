package parkingsystem;
public abstract class User {
    String userId;
    String userName;
    String phoneNumber;
    String password;
    String userType;

    public User(String userId, String userName, String phoneNumber, String password, String userType) {
        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public String getUserType() {
        return userType;
    }

      public String viewProfile() {
        return "User ID: " + userId + "\n" +
               "User Name: " + userName + "\n" +
               "Phone Number: " + phoneNumber + "\n" +
               "User Type: " + userType;
    }
}


