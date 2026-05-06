package parkingsystem;

public class ClientUserFactory implements UserFactory {
    @Override
    public User createUser(String userId, String userName, String phoneNumber, String password, String userType) {
        return new ClientUser(userId, null, userName, phoneNumber, password, userType, -1, 0.0, null);
    }
}

