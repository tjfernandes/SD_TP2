package tp2.impl.servers.dropbox.msgs;

public record UploadFileArgs (String path, boolean autorename) {
}