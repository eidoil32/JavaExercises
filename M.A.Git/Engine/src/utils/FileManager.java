package utils;

import exceptions.MyFileException;
import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import magit.BasicFile;
import magit.Blob;
import magit.Folder;
import magit.eFileTypes;
import settings.Settings;
import xml.basic.MagitRepository;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileManager {
    public static String readFile(Path filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath.toString()));
        StringBuilder content = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            content.append(line);
            line = reader.readLine();
            if (line != null) {
                content.append(Settings.MAGIT_LINE_SEPARATOR);
            }
        }
        reader.close();
        return content.toString();
    }

    public static void zipFile(BasicFile file, Path pathToObject) throws MyFileException {
        boolean tempFileExist = false;
        File tempFile = null;

        String sourceFile = file.getFullPathName();
        FileOutputStream fos;
        if (file.getType() == eFileTypes.FOLDER) {
            File folderTXT = new File(pathToObject.toString() + File.separator + Settings.TEMP_FOLDER_NAME);
            try {
                Folder temp = (Folder) file;
                folderTXT.createNewFile();
                PrintWriter writer = new PrintWriter(folderTXT.getPath());
                temp.setContent(temp.getBlobMap().toString());
                writer.println(temp.getContent());
                writer.close();
                sourceFile = folderTXT.getPath();
            } catch (IOException e) {
                throw new MyFileException(eErrorCodes.CREATE_TEMP_FOLDER_FILE_FAILED, file.getName());
            }

        } else {
            if (!new File(sourceFile).exists()) {
                sourceFile = pathToObject + File.separator + Settings.TEMP_FILE;
                tempFile = new File(pathToObject + File.separator + Settings.TEMP_FILE);
                try {
                    tempFile.createNewFile();
                    PrintWriter writer = new PrintWriter(tempFile);
                    writer.print(((Blob) file).getContent());
                    writer.close();
                } catch (IOException e) {
                    throw new MyFileException(eErrorCodes.OPEN_FILE_FAILED, pathToObject.toString() + File.separator + ((Blob) file).getSHA_ONE());
                }
                tempFileExist = true;
            }
        }
        try {
            fos = new FileOutputStream(pathToObject.toString() + File.separator + ((Blob) file).getSHA_ONE());
        } catch (FileNotFoundException e) {
            throw new MyFileException(eErrorCodes.OPEN_FILE_FAILED, pathToObject.toString() + File.separator + ((Blob) file).getSHA_ONE());
        }

        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourceFile);
        FileInputStream fis;

        try {
            fis = new FileInputStream(fileToZip);
        } catch (FileNotFoundException e) {
            throw new MyFileException(eErrorCodes.OPEN_FILE_FAILED, pathToObject.toString() + File.separator + ((Blob) file).getSHA_ONE());
        }
        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        try {
            zipOut.putNextEntry(zipEntry);
        } catch (IOException e) {
            throw new MyFileException(eErrorCodes.INSERT_FILE_TO_ZIP_FAILED, fileToZip.getName());
        }
        byte[] bytes = new byte[1024];
        int length;
        try {
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        } catch (IOException e) {
            throw new MyFileException(eErrorCodes.WRITE_TO_FILE_FAILED, fileToZip.getName());
        }

        try {
            zipOut.close();
            fis.close();
            fos.close();
        } catch (IOException e) {
            throw new MyFileException(eErrorCodes.CLOSE_FILE_FAILED, e.getMessage());
        }
        if (file.getType() == eFileTypes.FOLDER)
            fileToZip.delete();
        if (tempFileExist) {
            tempFile.delete();
        }
    }

    public static MagitRepository deserializeFrom(StringReader stringReader) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Settings.XML_LOAD_PACKAGE);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (MagitRepository) unmarshaller.unmarshal(stringReader);
    }

    public static MagitRepository deserializeFrom(InputStream stringReader) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Settings.XML_LOAD_PACKAGE);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (MagitRepository) unmarshaller.unmarshal(stringReader);
    }

    public static File unZipFile(File file, String pathToTempFolder) {
        File dir = new File(pathToTempFolder);
        // create output directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(file);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(pathToTempFolder + File.separator + fileName);
                //System.out.println("Unzipping to "+newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
            return Objects.requireNonNull(dir.listFiles())[0];
        } catch (IOException e) {
            return null;
        }
    }

    public static String ignoreLastPartOfPath(String location) {
        String[] splitterFileName;
        StringBuilder stringBuilder = new StringBuilder();

        if (location.contains(Settings.BASIC_SLASH)) {
            splitterFileName = location.split(Settings.BASIC_SLASH);
        } else {
            splitterFileName = location.split(Settings.SEPARATOR_PATTERN);
        }

        for (int i = 0; i < splitterFileName.length - 1; i++) {
            stringBuilder.append(splitterFileName[i]).append(File.separator);
        }

        return stringBuilder.toString();
    }

    public static Set<File> getAllFilesFromFolderSHA(String rootFolderSHA_one, String pathToObjectFolder) throws RepositoryException {
        Set<File> files = new LinkedHashSet<>();

        File rootFolder = new File(pathToObjectFolder + File.separator + rootFolderSHA_one);
        files.add(rootFolder);

        File rootFolderContent = unZipFile(rootFolder, pathToObjectFolder + File.separator + Settings.TEMP_UNZIP_FOLDER);
        try {
            List<String> content = Files.readAllLines(rootFolderContent.toPath());
            for (String string : content) {
                if (string.length() > 0) {
                    String[] parts = string.split(Settings.FOLDER_DELIMITER);
                    if (parts[2].equals(Settings.FOLDER_TYPE_IN_FOLDER_TABLE)) {
                        files.addAll(getAllFilesFromFolderSHA(parts[1], pathToObjectFolder));
                    } else {
                        files.add(new File(pathToObjectFolder + File.separator + parts[1]));
                    }
                }
            }
        } catch (IOException e) {
            throw new RepositoryException(eErrorCodes.FAILED_RECOVER_FOLDER_CONTENT);
        }

        return files;
    }

    public static String makeContent(String content) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            stringBuilder.append(lines[i]);
            if (i + 1 != lines.length) {
                stringBuilder.append(Settings.MAGIT_LINE_SEPARATOR);
            }
        }

        return stringBuilder.toString();
    }

    public static int countLines(File prCenter) throws FileNotFoundException {
        int counter = 0;
        Scanner scanner = new Scanner(prCenter);
        while (scanner.hasNextLine()) {
            counter++;
            scanner.nextLine();
        }

        return counter;
    }

    public static void removeLineFromFile(String lineToRemove, File file) throws FileNotFoundException, IOException{
        StringBuilder sb = new StringBuilder();
        try (Scanner sc = new Scanner(file)) {
            String currentLine;
            while(sc.hasNext()){
                currentLine = sc.nextLine();
                if(currentLine.equals(lineToRemove)){
                    continue; //skips lineToRemove
                }
                sb.append(currentLine).append("\n");
            }
        }
        //Delete File Content
        PrintWriter pw = new PrintWriter(file);
        pw.close();

        BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
        writer.append(sb.toString());
        writer.close();
    }
}