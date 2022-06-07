package tp2.impl.servers.common;

import static tp2.api.service.java.Result.error;
import static tp2.api.service.java.Result.ok;
import static tp2.api.service.java.Result.ErrorCode.INTERNAL_ERROR;
import static tp2.api.service.java.Result.ErrorCode.NOT_FOUND;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tp2.api.service.java.Files;
import tp2.api.service.java.Result;
import tp2.impl.kafka.KafkaSubscriber;
import tp2.impl.kafka.RecordProcessor;
import tp2.impl.kafka.Topics;
import tp2.impl.servers.kafka.KafkaDirectoryResources.DeleteFileInfo;
import util.IO;

public class JavaFiles implements Files, RecordProcessor {

	public static final String DELIMITER = "$$$";
	private static final String ROOT = "/tmp/";

	static final String FROM_BEGINNING = "earliest";
	static final String KAFKA_BROKERS = "kafka:9092";
	final KafkaSubscriber receiver;
	
	ObjectMapper mapper;

	public JavaFiles() {
		new File( ROOT ).mkdirs();
		receiver = 	KafkaSubscriber.createSubscriber(KAFKA_BROKERS, List.of(Topics.DELETE_FILE_GARBAGE.name(), Topics.DELETE_USER.name()), FROM_BEGINNING);
		receiver.start(false, this);
		this.mapper = new ObjectMapper();
	}

	@Override
	public void onReceive(ConsumerRecord<String, String> r) {
		DeleteFileInfo info = null;
		try {
			info = mapper.readValue(r.value(), DeleteFileInfo.class);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		switch(Topics.valueOf(r.topic())) {
			case DELETE_FILE_GARBAGE:
				deleteFile(JavaDirectory.fileId(info.filename(), info.userId()), "mysecret");
				break;
			case DELETE_USER:
				deleteUserFiles(info.userId(), "mysecret");
				break;
			default:
				break;
		}
		
	}

	@Override
	public Result<byte[]> getFile(String fileId, String token) {
		fileId = fileId.replace( DELIMITER, "/");
		byte[] data = IO.read( new File( ROOT + fileId ));
		return data != null ? ok( data) : error( NOT_FOUND );
	}

	@Override
	public Result<Void> deleteFile(String fileId, String token) {
		fileId = fileId.replace( DELIMITER, "/");
		boolean res = IO.delete( new File( ROOT + fileId ));	
		return res ? ok() : error( NOT_FOUND );
	}

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, String token) {
		fileId = fileId.replace( DELIMITER, "/");
		File file = new File(ROOT + fileId);
		file.getParentFile().mkdirs();
		IO.write( file, data);
		return ok();
	}

	@Override
	public Result<Void> deleteUserFiles(String userId, String token) {
		File file = new File(ROOT + userId);
		try {
			java.nio.file.Files.walk(file.toPath())
			.sorted(Comparator.reverseOrder())
			.map(Path::toFile)
			.forEach(File::delete);
		} catch (IOException e) {
			e.printStackTrace();
			return error(INTERNAL_ERROR);
		}
		return ok();
	}

	public static String fileId(String filename, String userId) {
		return userId + JavaFiles.DELIMITER + filename;
	}



	
}