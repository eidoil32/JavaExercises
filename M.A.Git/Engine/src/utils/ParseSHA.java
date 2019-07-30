package utils;

import magit.Repository;

public class ParseSHA {
    public static String parseSHA(Repository repository)
    {
        return repository.getName() +
                repository.getCurrentPath().toString();
    }
}
