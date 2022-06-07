package tp2.impl.servers.kafka;

import java.net.URI;

import com.google.gson.Gson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tp2.api.FileInfo;
import tp2.api.service.java.Result;
import tp2.impl.kafka.sync.SyncPoint;
import tp2.impl.servers.common.JavaDirectory;
import tp2.impl.servers.kafka.KafkaDirectoryResources.DeleteFileInfo;
import tp2.impl.servers.kafka.KafkaDirectoryResources.ShareFileInfo;

import static tp2.api.service.java.Result.ok;
import static tp2.api.service.java.Result.error;
import static tp2.api.service.java.Result.ErrorCode.*;


public class KafkaJavaDirectory extends JavaDirectory {

    final SyncPoint<String> sync;

    Gson json;

	ObjectMapper mapper;

    public KafkaJavaDirectory() {
        super();
        sync = new SyncPoint<>();
        json = new Gson();
		mapper = new ObjectMapper();
    }

    public Result<FileInfo> writeFile(String fileInfo) {
        FileInfo info = null;
        try {
            info = mapper.readValue(fileInfo, FileInfo.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        var fileId = fileId(info.getFilename(), info.getOwner());

        var url = info.getFileURL().split("/files")[0];

        var uf = userFiles.computeIfAbsent(info.getOwner(), (k) -> new UserFiles());

        ExtendedFileInfo file = null;
        
        synchronized (uf) {
            files.put(fileId, file = new ExtendedFileInfo(URI.create(url), fileId, info));
            if( uf.owned().add(fileId)){
                getFileCounts(file.uri(), true).numFiles().incrementAndGet();
            }
        }

        return ok(info);
    }

    public Result<Void> deleteUserFiles(String userId) {
		users.invalidate(userId);
		var fileIds = userFiles.remove(userId);
		if (fileIds != null)
			for (var id : fileIds.owned()) {
				var file = files.remove(id);
				removeSharesOfFile(file);
				getFileCounts(file.uri(), false).numFiles().decrementAndGet();
			}
		return ok();
	}

    public Result<Void> deleteFile(String value) {

        DeleteFileInfo info = null;
        try {
            info = mapper.readValue(value, DeleteFileInfo.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        var filename = info.filename();
        var userId = info.userId();
        var password = info.password();

        if (badParam(filename) || badParam(userId))
			return error(BAD_REQUEST);

		var fileId = fileId(filename, userId);

		var file = files.get(fileId);
		if (file == null)
			return error(NOT_FOUND);

		var user = getUser(userId, password);
		if (!user.isOK())
			return error(user.error());

		var uf = userFiles.getOrDefault(userId, new UserFiles());
		synchronized (uf) {
			var i = files.remove(fileId);
			uf.owned().remove(fileId);

			executor.execute(() -> this.removeSharesOfFile(i));
			
			getFileCounts(i.uri(), false).numFiles().decrementAndGet();
		}
		return ok();
    }

	public Result<Void> shareFileValidator(String filename, String userId, String userIdShare, String password) {

		if (badParam(filename) || badParam(userId) || badParam(userIdShare))
			return error(BAD_REQUEST);

		var fileId = fileId(filename, userId);

		var file = files.get(fileId);
		if (file == null || getUser(userIdShare, "").error() == NOT_FOUND)
			return error(NOT_FOUND);

		var user = getUser(userId, password);
		if (!user.isOK())
			return error(user.error());

		var uf = userFiles.computeIfAbsent(userIdShare, (k) -> new UserFiles());
		synchronized (uf) {
			uf.shared().add(fileId);
			file.info().getSharedWith().add(userIdShare);
		}

		return ok();
	}

	public Result<Void> unshareFile(String value) {
        ShareFileInfo info = null;
        try {
            info = mapper.readValue(value, ShareFileInfo.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        var filename = info.filename();
        var userId = info.userId();
        var userIdShare = info.userIdShare();
        var password = info.password();

		if (badParam(filename) || badParam(userId) || badParam(userIdShare))
			return error(BAD_REQUEST);

		var fileId = fileId(filename, userId);

		var file = files.get(fileId);
		if (file == null || getUser(userIdShare, "").error() == NOT_FOUND)
			return error(NOT_FOUND);

		var user = getUser(userId, password);
		if (!user.isOK())
			return error(user.error());

		var uf = userFiles.computeIfAbsent(userIdShare, (k) -> new UserFiles());
		synchronized (uf) {
			uf.shared().remove(fileId);
			file.info().getSharedWith().remove(userIdShare);
		}

		return ok();
	}


}