package parkingsystem;

public class AdminUserFactory implements UserFactory {
    @Override
    public User createUser(String userId, String userName, String phoneNumber, String password, String userType) {
        AdminUser admin = new AdminUser(userId, userName, phoneNumber, password, userType);
        admin.setAdminCode(1234);
        return admin;
    }
}
