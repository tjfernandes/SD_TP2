package tp2.impl.servers.kafka;

import tp2.api.FileInfo;
import tp2.api.service.java.Result;
import tp2.impl.kafka.sync.SyncPoint;
import tp2.impl.servers.common.JavaDirectory;

public class KafkaJavaDirectory extends JavaDirectory {

    final SyncPoint<String> sync;

    public KafkaJavaDirectory() {
        super();
        sync = new SyncPoint<>();
    }

    public Result<FileInfo> writeFile(String value) {

        System.out.println("\n\n\n\n\n\n"+value+"\n\n\n\n\n\n");

        return null;
    }

}