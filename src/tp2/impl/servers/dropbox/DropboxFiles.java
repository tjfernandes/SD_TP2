package tp2.impl.servers.dropbox;

import static tp2.api.service.java.Result.error;
import static tp2.api.service.java.Result.ok;
import static tp2.api.service.java.Result.ErrorCode.NOT_FOUND;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.dropbox.core.v2.files.DeleteArg;
import org.pac4j.scribe.builder.api.DropboxApi20;

import jakarta.ws.rs.core.Response.Status;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;

import tp2.api.service.java.Files;
import tp2.api.service.java.Result;
import tp2.impl.servers.dropbox.msgs.*;
import com.dropbox.core.v2.files.WriteMode;

public class DropboxFiles implements Files {

    static final String DELIMITER = "$$$";

	private static final String DROPBOX_API_ARG = "Dropbox-API-Arg";

	private static final String UPLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/upload";
	private static final String DOWNLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/download";
	private static final String DELETE_FILE_URL = "https://api.dropboxapi.com/2/files/delete_v2";


	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String OCTET_STREAM_CONTENT_TYPE = "application/octet-stream";
	private static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";

	private static final String MAIN_DIR_PATH = "/Users";

	private final Gson json;
    private final OAuth20Service service;
    private final OAuth2AccessToken accessToken;
	
	public DropboxFiles(String apiKey, String apiSecret, String accessTokenStr, boolean cleanDropbox) {
		json = new Gson();
		accessToken = new OAuth2AccessToken(accessTokenStr);
		service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
		if (cleanDropbox) {
			deleteDir();
		}
	}

	private Result<Void> deleteDir() {
		var deleteDir = new OAuthRequest(Verb.POST, DELETE_FILE_URL);

		deleteDir.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

		deleteDir.setPayload(json.toJson(new DeleteArg(MAIN_DIR_PATH)));

		service.signRequest(accessToken, deleteDir);

		Response r;
		try {
			r = service.execute(deleteDir);
			if (r.getCode() != Status.OK.getStatusCode()) {
				System.out.println(String.format("\n\n\n\n\nFailed to delete Users folder: Status: %d, \nReason: %s\n", r.getCode(), r.getBody()));
				if (r.getCode() == Status.NOT_FOUND.getStatusCode())
					return error(NOT_FOUND);
			}
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}

		return ok();
	}

	@Override
	public Result<byte[]> getFile(String fileId, String token) {
		
		fileId = fileId.replace( DELIMITER, "/");

		var downloadFile = new OAuthRequest(Verb.POST, DOWNLOAD_FILE_URL);

		downloadFile.addHeader(CONTENT_TYPE_HDR, OCTET_STREAM_CONTENT_TYPE);
		downloadFile.addHeader(DROPBOX_API_ARG, json.toJson(new DownloadFileArgs(MAIN_DIR_PATH+"/"+fileId)));

		service.signRequest(accessToken, downloadFile);

		Response r;
		byte[] data = null;
		try {
			r = service.execute(downloadFile);
			if (r.getCode() != Status.OK.getStatusCode()) {
				System.out.println(String.format("\n\n\n\nFailed to download file: %s, Status: %d, \nReason: %s\n", fileId, r.getCode(), r.getBody()));
			}

			data = r.getStream().readAllBytes();
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}


		return data != null ? ok( data) : error( NOT_FOUND );
	}

	@Override
	public Result<Void> deleteFile(String fileId, String token) {

		fileId = fileId.replace( DELIMITER, "/");

		var deleteFile = new OAuthRequest(Verb.POST, DELETE_FILE_URL);

		deleteFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

		deleteFile.setPayload(json.toJson(new DeleteArg(MAIN_DIR_PATH+"/"+fileId)));

		service.signRequest(accessToken, deleteFile);

		Response r;
		try {
			r = service.execute(deleteFile);
			if (r.getCode() != Status.OK.getStatusCode()) {
				System.out.println(String.format("\n\n\n\n\nFailed to delete file: %s, Status: %d, \nReason: %s\n", fileId, r.getCode(), r.getBody()));
				if (r.getCode() == Status.NOT_FOUND.getStatusCode())
					return error(NOT_FOUND);
			}
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}

		return ok();
	}

	@Override
	public Result<Void> writeFile(String fileId, byte[] data, String token) {
		fileId = fileId.replace( DELIMITER, "/");
		
		var uploadFile = new OAuthRequest(Verb.POST, UPLOAD_FILE_URL);

		uploadFile.addHeader(CONTENT_TYPE_HDR, OCTET_STREAM_CONTENT_TYPE);
		uploadFile.addHeader(DROPBOX_API_ARG, json.toJson(new UploadArgs(MAIN_DIR_PATH+"/"+fileId, WriteMode.OVERWRITE.tag().toString().toLowerCase())));

		uploadFile.setPayload(data);

		service.signRequest(accessToken, uploadFile);

		Response r;
		try {
			r = service.execute(uploadFile);
			if (r.getCode() != Status.OK.getStatusCode()) {
				throw new RuntimeException(String.format("\n\n\n\nFailed to upload file: %s, Status: %d, \nReason: %s\n", fileId, r.getCode(), r.getBody()));
			}
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}
		

		return ok();
	}

	@Override
	public Result<Void> deleteUserFiles(String userId, String token) {
		var deleteFolder = new OAuthRequest(Verb.POST, DELETE_FILE_URL);

		deleteFolder.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

		deleteFolder.setPayload(json.toJson(new DeleteArg(MAIN_DIR_PATH+"/"+userId)));

		service.signRequest(accessToken, deleteFolder);

		Response r;
		try {
			r = service.execute(deleteFolder);
			if (r.getCode() != Status.OK.getStatusCode()) {
				throw new RuntimeException(String.format("Failed to delete files from user: %s, Status: %d, \nReason: %s\n", userId, r.getCode(), r.getBody()));
			}
		} catch (InterruptedException | ExecutionException | IOException e) {
			e.printStackTrace();
		}

		return ok();
	}

	public static String fileId(String filename, String userId) {
		return userId + DropboxFiles.DELIMITER + filename;
	}
    
}
