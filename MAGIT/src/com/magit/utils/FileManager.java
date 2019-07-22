package com.magit.utils;

import com.magit.settings.Settings;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FileManager {
        public static String ReadFile(Path filePath) throws IOException
        {
            return FileUtils.readFileToString(new File(filePath.toUri()), Settings.FILE_ENCODING);
        }
}
