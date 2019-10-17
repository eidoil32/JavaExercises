package usermanager;

import exceptions.MyWebException;
import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import magit.Magit;
import magit.Repository;
import org.apache.commons.codec.digest.DigestUtils;
import settings.Settings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class  User  {
    private String name, password;

    public User(String name, String password) {
        this.name = name;
        this.password = DigestUtils.sha1Hex(password);
    }

    public List<Magit> getRepositories() throws IOException, RepositoryException {
        List<Magit> repositories = new LinkedList<>();
        File repositoriesFolder = new File(String.format(Settings.USERS_REPOSITORY_ROOT_FOLDER, this.name));
        File[] folders = repositoriesFolder.listFiles();
        if (folders != null) {
            for (File repository : folders) {
                if (repository.isDirectory()) {
                    Magit magit = new Magit();
                    magit.setCurrentRepository(new Repository(repository.toPath(), magit.getCurrentUser(), null, true));
                    repositories.add(magit);
                }
            }
            return repositories;
        } else {
            return null;
        }
    }

    public String getName() {
        return name;
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

    public Map<Integer, Map<String, String>> getRepositoriesMap() throws IOException, RepositoryException {
        Map<Integer, Map<String, String>> result = new HashMap<>();
        List<Magit> repositories = getRepositories();
        if (repositories != null) {
            int i = 0;
            for (Magit magit : repositories) {
                result.put(i++, magit.createRepositoryJson());
            }
        }
        return result;
    }

    public Integer countRepositories() {
        File rootFolder = new File(String.format(Settings.USERS_REPOSITORY_ROOT_FOLDER, name));
        File[] repositories = rootFolder.listFiles();
        if (repositories != null) {
            return repositories.length - 1;
        }
        return -1;
    }

    public Magit getRepository(int repositoryID) throws IOException, RepositoryException {
        File repository = new File(String.format(Settings.USERS_REPOSITORY_FOLDER, name, repositoryID));
        if (repository.exists()) {
            Magit magit = new Magit();
            magit.changeRepo(repository.getPath());
            return magit;
        }
        throw new RepositoryException(eErrorCodes.NO_REPOSITORY);
    }
}
