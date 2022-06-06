package tp2.impl.servers.kafka;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

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

    final KafkaSubscriber subscriber;

    final KafkaPublisher publisher;

	private static final String FROM_BEGINNING = "earliest";

	public static final String KAFKA_BROKERS = "kafka:9092";

    final KafkaJavaDirectory impl;

    public KafkaDirectoryResources() {
        impl = new KafkaJavaDirectory();
        publisher = KafkaPublisher.createPublisher(KAFKA_BROKERS);
        subscriber = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(Topics.DELETE_USER.getLabel(), Topics.WRITE_FILE.getLabel()), FROM_BEGINNING);
		subscriber.start(false, this);
    }

    @Override
	public void onReceive(ConsumerRecord<String, String> r) {
		switch(Topics.valueOf(r.topic())) {
			case DELETE_USER:
				impl.deleteUserFiles(r.value());
                break;
            case WRITE_FILE:
                impl.writeFile(r.value());
                break;
		}

	}

    @Override
    public FileInfo writeFile(long version, String filename, byte[] data, String userId, String password) {
        Result<FileInfo> res =  impl.writeFile(filename, data, userId, password);
        if(res.isOK())
            publisher.publish(Topics.WRITE_FILE.getLabel(), JavaDirectory.fileId(filename, userId));
		return null;
    }

    @Override
    public void deleteFile(long version, String filename, String userId, String password) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void shareFile(long version, String filename, String userId, String userIdShare, String password) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void unshareFile(long version, String filename, String userId, String userIdShare, String password) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public byte[] getFile(long version, String filename, String userId, String accUserId, String password) {
        // TODO Auto-generated method stub
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
