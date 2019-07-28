package com.magit.utils;

import com.magit.engine.Repository;

public class ParseSHA {
    public static String parseSHA(Repository repository)
    {
        return repository.getName() +
                repository.getCurrentPath().toString();
    }
}
