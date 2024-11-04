import java.util.HashMap;

public class ManagerUser {
    private static final HashMap<Integer, User> users = new HashMap();
    private Integer userId = 0;

    public User createNewUser(User user) {
        if (user != null) {
            Integer idUser = createId();
            users.put(idUser, user);
            user.setId(idUser);
        }
        return user;
    }

    public HashMap<Integer, User> getUsers() {
        return users;
    }

    private int createId() {                                //Увеличение ID на 1
        return userId++;
    }

    public User getUser(Integer num) {

            return users.get(num);
    }
}
