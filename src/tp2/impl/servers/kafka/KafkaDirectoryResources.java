package tp2.impl.servers.kafka;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.internals.Topic;

import tp2.impl.kafka.sync.SyncPoint;
import tp2.api.FileInfo;
import tp2.api.service.java.Result;
import tp2.api.service.rest.RestDirectory;
import tp2.impl.kafka.KafkaPublisher;
import tp2.impl.kafka.KafkaSubscriber;
import tp2.impl.kafka.RecordProcessor;
import tp2.impl.kafka.Topics;
import tp2.impl.servers.common.JavaDirectory;
import tp2.impl.servers.rest.RestResource;

public class KafkaDirectoryResources extends RestResource implements RestDirectory, RecordProcessor {

    private static Logger Log = Logger.getLogger(KafkaDirectoryResources.class.getName());

    final KafkaSubscriber subscriber;

    final KafkaPublisher publisher;

    final SyncPoint<String> sync;

	private static final String FROM_BEGINNING = "earliest";

	public static final String KAFKA_BROKERS = "kafka:9092";

    final KafkaJavaDirectory impl;

    public KafkaDirectoryResources() {
        impl = new KafkaJavaDirectory();
        publisher = KafkaPublisher.createPublisher(KAFKA_BROKERS);
        subscriber = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(Topics.deleteUser.name(), Topics.writeFile.name()), FROM_BEGINNING);
		subscriber.start(false, this);
        sync = new SyncPoint<>();
    }

    @Override
	public void onReceive(ConsumerRecord<String, String> r) {
        Log.info("\n\n\n\n\n\n"+Topics.valueOf(r.topic())+"\n\n\n\n\n\n");

		switch(Topics.valueOf(r.topic())) {
			case deleteUser:
				impl.deleteUserFiles(r.value());
                break;
            case writeFile:
                Log.info("\n\n\n\n\n\nENTROU writeFile case: \n\n\n\n\n\n");
                impl.writeFile(r.value());
                break;
		}
        sync.setResult(r.offset(), r.value());
	}

    @Override
    public FileInfo writeFile(long version, String filename, byte[] data, String userId, String password) {
        Result<FileInfo> res =  impl.writeFile(filename, data, userId, password);
        if(res.isOK()) {
            sync.waitForResult(publisher.publish(Topics.writeFile.name(), JavaDirectory.fileId(filename, userId)));
        }
            
		return super.resultOrThrow(res);
    }

    @Override
    public void deleteFile(long version, String filename, String userId, String password) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void shareFile(long version, String filename, String userId, String userIdShare, String password) {
        
    }

    @Override
    public void unshareFile(long version, String filename, String userId, String userIdShare, String password) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public byte[] getFile(long version, String filename, String userId, String accUserId, String password) {
        // TODO Auto-generated method stub
        Log.info("\n\n\n\n\n\nENTROU getFile()\n\n\n\n\n\n");
        return null;
    }

    @Override
    public List<FileInfo> lsFile(long version, String userId, String password) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteUserFiles(long version, String userId, String password, String token) {
        // TODO Auto-generated method stub
        
    }
    
}
