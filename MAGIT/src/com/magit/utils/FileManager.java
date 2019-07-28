package com.magit.utils;

import com.magit.engine.BasicFile;
import com.magit.engine.Blob;
import com.magit.engine.Folder;
import com.magit.engine.eFileTypes;
import com.magit.exceptions.RepositoryException;
import com.magit.exceptions.eErrorCodes;
import com.magit.settings.Settings;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileManager {
    public static String readFile(Path filePath) throws IOException {
        return FileUtils.readFileToString(new File(filePath.toUri()), Settings.FILE_ENCODING);
    }

    public static List<File> scanFolder(String rootPath) {
        return new ArrayList<>(Arrays.asList(Objects.requireNonNull(new File(rootPath).listFiles())));
    }

    public static void createFile(BasicFile file, Path pathToObject) throws RepositoryException {
        try {
            String shaone;
            File temp = new File(file.getFullPathName());
            if (file.getType() == eFileTypes.FOLDER) {
                String tempFolderContent = ((Folder) file).getBlobMap().toString();
                shaone = DigestUtils.sha1Hex(tempFolderContent);
                temp = new File(pathToObject.toString() + "/temp.magit");
                temp.createNewFile();
                PrintWriter writer = new PrintWriter(temp.toString());
                writer.println(tempFolderContent);
                writer.close();
            } else {
                shaone = ((Blob) file).getSHA_ONE();
            }
            FileOutputStream fos = new FileOutputStream(pathToObject.toString() + "/" + shaone);
            ZipOutputStream zipOS = new ZipOutputStream(fos);
            writeToZipFile(temp.toString(),zipOS);
            if(file.getType() == eFileTypes.FOLDER)
            {
                temp.delete();
            }
            zipOS.close();
            fos.close();

        } catch (IOException e) {
            throw new RepositoryException(eErrorCodes.CREATE_ZIP_FILE_FAILED);
        }
    }
    public static void writeToZipFile(String path, ZipOutputStream zipStream)
            throws IOException {

        System.out.println("Writing file : '" + path + "' to zip file");

        File aFile = new File(path);
        FileInputStream fis = new FileInputStream(aFile);
        ZipEntry zipEntry = new ZipEntry(path);
        zipStream.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipStream.write(bytes, 0, length);
        }

        zipStream.closeEntry();
        fis.close();
    }
}
