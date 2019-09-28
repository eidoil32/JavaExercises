package usermanager;

import exceptions.MyWebException;
import exceptions.eErrorCodes;
import magit.Magit;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.LinkedHashSet;
import java.util.Set;

public class  User  {
    private String name, password, id;
    private Set<Magit> repositories = new LinkedHashSet<>();

    public User(String name, String password, String id) {
        this.name = name;
        this.password = DigestUtils.sha1Hex(password);
        this.id = id;
    }

    public void addMagitRepository(Magit magit) {
        repositories.add(magit);
    }

    public Set<Magit> getRepositories() {
        return repositories;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public boolean credentials(String password) {
        String encrypted = DigestUtils.sha1Hex(password);
        return this.password.equals(encrypted);
    }

    public void updatePassword(String currentPassword, String newPassword) throws MyWebException {
        if (credentials(currentPassword) && newPassword.length() > 2) {
            this.password = DigestUtils.sha1Hex(newPassword);
        } else {
            throw new MyWebException(eErrorCodes.USER_PASSWORD_WRONG, null);
        }
    }

    public void updateID(String id) {
        this.id = id;
    }
}
