package parkingsystem;

public interface UserFactory {
    User createUser(String userId, String userName, String phoneNumber, String password, String userType);
}
