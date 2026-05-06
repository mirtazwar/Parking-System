package parkingsystem;

public class UserFactoryManager {
    public static UserFactory getUserFactory(String userType) {
        if ("Admin".equalsIgnoreCase(userType)) {
            return new AdminUserFactory();
        } else if ("Client".equalsIgnoreCase(userType)) {
            return new ClientUserFactory();
        }
        throw new IllegalArgumentException("Invalid user type: " + userType);
    }
}
