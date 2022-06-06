package tp2.impl.servers.kafka;

import java.net.URI;

import tp2.api.FileInfo;
import tp2.api.service.java.Result;
import tp2.impl.kafka.sync.SyncPoint;
import tp2.impl.servers.common.JavaDirectory;
import static tp2.api.service.java.Result.ok;


public class KafkaJavaDirectory extends JavaDirectory {

    final SyncPoint<String> sync;

    public KafkaJavaDirectory() {
        super();
        sync = new SyncPoint<>();
    }

    public Result<FileInfo> writeFile(String fileId) {
        System.out.println("\n\n\n\n\n\nEntrou writefile\n\n\n\n\n");
        var file = files.get(fileId);
        System.out.println("\n\n\n\n\n\nDeu files get\n\n\n\n\n");
        var info = file.info();
        
        var url = info.getFileURL().split(FileInfo.DELIMITER)[0];

        files.put(fileId, file = new ExtendedFileInfo(URI.create(url), fileId, info));
        System.out.println("\n\n\n\n\n\nDeu files put\n\n\n\n\n");

        return ok(info);
    }


}