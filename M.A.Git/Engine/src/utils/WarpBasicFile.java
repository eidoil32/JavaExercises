package utils;

import magit.Blob;

public class WarpBasicFile {
    private Blob file;
    private String SHA_ONE;

    public WarpBasicFile(Blob file) {
        if (file != null) {
            this.file = file;
            this.SHA_ONE = file.getSHA_ONE();
        } else {
            this.SHA_ONE = "";
        }
    }

    public void setFile(Blob file) {
        if (file != null) {
            this.file = file;
            this.SHA_ONE = file.getSHA_ONE();
        }
    }

    public Blob getFile() {
        return file;
    }

    @Override
    public int hashCode() {
        return SHA_ONE.hashCode();
    }

    public String getSHA_ONE() {
        return SHA_ONE;
    }

    @Override
    public boolean equals(Object obj) {
        return SHA_ONE.equals(((WarpBasicFile) obj).getSHA_ONE());
    }
}
