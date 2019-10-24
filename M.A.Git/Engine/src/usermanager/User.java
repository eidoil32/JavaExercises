package usermanager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import exceptions.MyWebException;
import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import magit.Magit;
import magit.Repository;
import org.apache.commons.codec.digest.DigestUtils;
import settings.Settings;
import utils.FileManager;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

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


    /**
     * @param messageParts :
     *                     1. Type (Fork, pull request)
     *                     2. Repository
     *                     3. Message content
     *                     4. Time
     *                     5. Creator
     * @throws IOException
     */
    public void leaveMessageToMe(String ...messageParts) throws IOException {
        int size = 5;
        File messageCenter = new File(String.format(Settings.USER_MESSAGES_CENTER, name));
        StringBuilder messages = new StringBuilder(FileManager.readFile(messageCenter.toPath()));
        if (messages.length() > 0) {
            messages.append(System.lineSeparator());
        }
        Map<String, String> message = new HashMap<>();
        for (int i = 0; i < size; i++) {
            message.put(Settings.USER_MESSAGE_PARTS[i], messageParts[i]);
        }

        messages.append(new Gson().toJson(message));

        try (PrintWriter writer = new PrintWriter(messageCenter)) {
            writer.print(messages.toString());
        }
    }

    // returns JSON as String.
    public String readMessages() throws IOException {
        Map<Integer, Map<String, String>> messagesMap = new HashMap<>();
        int i = 0;
        Scanner scanner = new Scanner(new File(String.format(Settings.USER_MESSAGES_CENTER, name)));
        while(scanner.hasNextLine()) {
            Type converter = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> message = new HashMap<>(new Gson().fromJson(scanner.nextLine(), converter));
            messagesMap.put(i++, message);
        }

        return new Gson().toJson(messagesMap);
    }

    public void clearMessages() throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(String.format(Settings.USER_MESSAGES_CENTER, name))) {
            writer.write("");
        }
    }

    public void createPullRequest(Map<String, String> data) throws IOException {
        File prCenter = new File(String.format(Settings.USER_PULL_REQUEST_CENTER, name));
        int lines = FileManager.countLines(prCenter);
        data.put(Settings.PR_ID, Integer.toString(lines));
        String newLine = (prCenter.length() > 0) ? System.lineSeparator() : "";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(prCenter, true))) {
            bw.append(newLine).append(new Gson().toJson(data));
        }
    }

    public List<String> getPullRequests(String repository_id) throws IOException {
        List<String> results = new LinkedList<>();

        Scanner scanner = new Scanner(new File(String.format(Settings.USER_PULL_REQUEST_CENTER, name)));
        while(scanner.hasNextLine()) {
            Type converter = new TypeToken<Map<String, String>>() {}.getType();
            String line = scanner.nextLine();
            Map<String, String> pullRequest = new HashMap<>(new Gson().fromJson(line, converter));
            if (pullRequest.get(Settings.PR_REMOTE_REPOSITORY_ID).equals(repository_id)) {
                results.add(line);
            }
        }

        return results;
    }

    public Map<String, String> getPullRequest(String repository_id, String pullRequest_id) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(String.format(Settings.USER_PULL_REQUEST_CENTER, name)));
        while(scanner.hasNextLine()) {
            Type converter = new TypeToken<Map<String, String>>() {}.getType();
            String line = scanner.nextLine();
            Map<String, String> pullRequest = new HashMap<>(new Gson().fromJson(line, converter));
            if (pullRequest.get(Settings.PR_REMOTE_REPOSITORY_ID).equals(repository_id)
                    && pullRequest.get(Settings.PR_ID).equals(pullRequest_id)) {
                return pullRequest;
            }
        }

        return null;
    }
}
