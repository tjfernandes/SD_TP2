package tp2.impl.servers.kafka;

import java.util.List;
import java.util.logging.Logger;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import jakarta.inject.Singleton;
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
import static tp2.impl.servers.common.JavaDirectory.fileId;

@Singleton
public class KafkaDirectoryResources extends RestResource implements RestDirectory, RecordProcessor {

    private static Logger Log = Logger.getLogger(KafkaDirectoryResources.class.getName());

    final KafkaSubscriber subscriber;

    final KafkaPublisher publisher;

    final SyncPoint<String> sync;

	private static final String FROM_BEGINNING = "earliest";

	public static final String KAFKA_BROKERS = "kafka:9092";

    final KafkaJavaDirectory impl;

    static KafkaDirectoryResources instance;

    ObjectMapper mapper;

    public KafkaDirectoryResources() {
        impl = new KafkaJavaDirectory();
        publisher = KafkaPublisher.createPublisher(KAFKA_BROKERS);
        subscriber = KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(Topics.DELETE_USER.name(), Topics.WRITE_FILE.name(), Topics.DELETE_FILE.name(), Topics.UNSHARE_FILE.name(), Topics.SHARE_FILE.name()), FROM_BEGINNING);
		subscriber.start(false, this);
        sync = new SyncPoint<>();
        mapper = new ObjectMapper();
    }

    public static KafkaDirectoryResources getInstance() {
        if (instance == null)
            instance = new KafkaDirectoryResources();
        return instance;
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
            case DELETE_FILE:
                impl.deleteFile(r.value());
                break;
            case SHARE_FILE:
                impl.shareFile(r.value());
                break;
            case UNSHARE_FILE:
                impl.unshareFile(r.value());
                break;
            default:
                break;
		}
        sync.setResult(r.offset(), r.value());
	}

    @Override
    public FileInfo writeFile(long version, String filename, byte[] data, String userId, String password) {
        Result<FileInfo> res =  impl.writeFile(filename, data, userId, password);
        if(res.isOK()) {
            try {
                sync.waitForResult(publisher.publish(Topics.WRITE_FILE.name(), mapper.writeValueAsString(res.value())));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
            
		return super.resultOrThrow(res);
    }

    @Override
    public void deleteFile(long version, String filename, String userId, String password) {
        Result<Void> res =  impl.deleteFile(filename, userId, password);
        if(res.isOK()) {
            try {
				sync.waitForResult(publisher.publish(Topics.DELETE_FILE.name(), mapper.writeValueAsString(new DeleteFileInfo(filename, userId, password))));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
        }
        super.resultOrThrow(res);
    }

    @Override
    public void shareFile(long version, String filename, String userId, String userIdShare, String password) {
        Result<Void> res =  impl.shareFile(filename, userId, userIdShare, password);
        if(res.isOK()) {
            try {
				sync.waitForResult(publisher.publish(Topics.SHARE_FILE.name(), mapper.writeValueAsString(new ShareFileInfo(filename, userId, userIdShare, password))));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
        }
        super.resultOrThrow(res);
    }

    @Override
    public void unshareFile(long version, String filename, String userId, String userIdShare, String password) {
        Result<Void> res =  impl.unshareFile(filename, userId, userIdShare, password);
        if(res.isOK()) {
            try {
				sync.waitForResult(publisher.publish(Topics.UNSHARE_FILE.name(), mapper.writeValueAsString(new ShareFileInfo(filename, userId, userIdShare, password))));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
        }
        super.resultOrThrow(res);
    }

    @Override
    public void deleteUserFiles(long version, String userId, String password, String token) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public byte[] getFile(long version, String filename, String userId, String accUserId, String password) {
        Result<byte[]> res =  impl.getFile(filename, userId, accUserId, password);  
		return super.resultOrThrow(res);
    }

    @Override
    public List<FileInfo> lsFile(long version, String userId, String password) {
        Result<List<FileInfo>> res =  impl.lsFile(userId, password);  
		return super.resultOrThrow(res);
    }

    static record DeleteFileInfo(String filename, String userId, String password) {		
	}

    static record ShareFileInfo(String filename, String userId, String userIdShare, String password) {
    }
    
}
