/* ownCloud Android Library is available under MIT license
 *   Copyright (C) 2015 ownCloud Inc.
 *   
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *   
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *   
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 *   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
 *   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN 
 *   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 *   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 *
 */

package com.owncloud.android.lib.testclient;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.network.NetworkUtils;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.ChunkedFileUploadRemoteOperation;
import com.owncloud.android.lib.resources.files.CreateFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.DownloadFileRemoteOperation;
import com.owncloud.android.lib.resources.files.ReadFolderRemoteOperation;
import com.owncloud.android.lib.resources.files.RemoveFileRemoteOperation;
import com.owncloud.android.lib.resources.files.RenameFileRemoteOperation;
import com.owncloud.android.lib.resources.files.UploadFileRemoteOperation;
import com.owncloud.android.lib.resources.files.model.RemoteFile;
import com.owncloud.android.lib.resources.shares.CreateRemoteShareOperation;
import com.owncloud.android.lib.resources.shares.GetRemoteSharesOperation;
import com.owncloud.android.lib.resources.shares.RemoveRemoteShareOperation;
import com.owncloud.android.lib.resources.shares.ShareType;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

/**
 * Activity to test OC framework
 * @author masensio
 * @author David A. Velasco
 */

public class TestActivity extends Activity {
	
	private static final String TAG = null;
	// This account must exists on the server side
	private String mServerUri;
	private String mUser;
	private String mPass;
	
	private static final int BUFFER_SIZE = 1024;
	
	public static final String ASSETS__TEXT_FILE_NAME = "textFile.txt";
	public static final String ASSETS__IMAGE_FILE_NAME = "imageFile.png";
	public static final String ASSETS__VIDEO_FILE_NAME = "videoFile.mp4";
	
	//private Account mAccount = null;
	private OwnCloudClient mClient;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		
		mServerUri = getString(R.string.server_base_url);
		mUser = getString(R.string.username);
		mPass = getString(R.string.password);
    	
		Protocol pr = Protocol.getProtocol("https");
		if (pr == null || !(pr.getSocketFactory() instanceof SelfSignedConfidentSslSocketFactory)) {
			try {
				ProtocolSocketFactory psf = new SelfSignedConfidentSslSocketFactory();
				Protocol.registerProtocol(
						"https",
						new Protocol("https", psf, 443));
				
			} catch (GeneralSecurityException e) {
				Log.e(TAG, "Self-signed confident SSL context could not be loaded");
			}
		}

		mClient = new OwnCloudClient(Uri.parse(mServerUri), NetworkUtils.getMultiThreadedConnManager(), false);
		mClient.setDefaultTimeouts(
				OwnCloudClientFactory.DEFAULT_DATA_TIMEOUT, 
				OwnCloudClientFactory.DEFAULT_CONNECTION_TIMEOUT);
		mClient.setFollowRedirects(true);
		mClient.setCredentials(
				OwnCloudCredentialsFactory.newBasicCredentials(
						mUser, 
						mPass
				)
		);
		mClient.setBaseUri(Uri.parse(mServerUri));

		Log.v(TAG, "onCreate finished, ownCloud client ready");
    	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test, menu);
		return true;
	}

	/**
	 * Access to the library method to Create a Folder
	 * @param remotePath            Full path to the new directory to create in the remote server.
     * @param createFullPath        'True' means that all the ancestor folders should be created if 
     * 								don't exist yet.
	 * 
	 * @return
	 */
	public RemoteOperationResult createFolder(String remotePath, boolean createFullPath) {
		
		return TestActivity.createFolder(remotePath, createFullPath, mClient);
	}

	/**
	 * Access to the library method to Create a Folder
	 * @param remotePath		Full path to the new directory to create in the remote server.
     * @param createFullPath    'True' means that all the ancestor folders should be created if 
     * 							don't exist yet.
	 * @param client			Client instance configured to access the target OC server.
	 * 
	 * @return	Result of the operation
	 */
	public static RemoteOperationResult createFolder(
			String remotePath, boolean createFullPath, OwnCloudClient client
		) {

        CreateFolderRemoteOperation createOperation =
                new CreateFolderRemoteOperation(remotePath, createFullPath);
		RemoteOperationResult result =  createOperation.execute(client);
		
		return result;
	}
	
	/**
	 * Access to the library method to Rename a File or Folder
	 * @param oldName			Old name of the file.
     * @param oldRemotePath		Old remote path of the file. For folders it starts and ends by "/"
     * @param newName			New name to set as the name of file.
     * @param isFolder			'true' for folder and 'false' for files
     * 
     * @return
     */

	public RemoteOperationResult renameFile(String oldName, String oldRemotePath, String newName, boolean isFolder) {

        RenameFileRemoteOperation renameOperation = new RenameFileRemoteOperation(oldName, oldRemotePath, newName, isFolder);
		RemoteOperationResult result = renameOperation.execute(mClient);
		
		return result;
	}
	
	/** 
	 * Access to the library method to Remove a File or Folder
	 * 
	 * @param remotePath	Remote path of the file or folder in the server.
	 * @return
	 */
	public RemoteOperationResult removeFile(String remotePath) {
        RemoveFileRemoteOperation removeOperation = new RemoveFileRemoteOperation(remotePath);
		RemoteOperationResult result = removeOperation.execute(mClient);
		return result;
	}
	
	/** 
	 * Access to the library method to Remove a File or Folder
	 * 
	 * @param remotePath	Remote path of the file or folder in the server.
	 * @return
	 */
	public static RemoteOperationResult removeFile(String remotePath, OwnCloudClient client) {
        RemoveFileRemoteOperation removeOperation = new RemoveFileRemoteOperation(remotePath);
		RemoteOperationResult result = removeOperation.execute(client);
		return result;
	}
	
		
	/**
	 * Access to the library method to Read a Folder (PROPFIND DEPTH 1)
	 * @param remotePath
	 * 
	 * @return
	 */
	public RemoteOperationResult readFile(String remotePath) {

        ReadFolderRemoteOperation readOperation = new ReadFolderRemoteOperation(remotePath);
		RemoteOperationResult result = readOperation.execute(mClient);

		return result;
	}
	
	/**
	 * Access to the library method to Download a File
	 * @param remoteFile
	 * @param temporalFolder
	 * 
	 * @return
	 */
	public RemoteOperationResult downloadFile(RemoteFile remoteFile, String temporalFolder) {
		// Create folder 
		String path =  "/owncloud/tmp/" + temporalFolder;
		File privateFolder = getFilesDir();
		File folder = new File(privateFolder.getAbsolutePath() + "/" + path);
		folder.mkdirs();

        DownloadFileRemoteOperation downloadOperation = new DownloadFileRemoteOperation(remoteFile.getRemotePath(), folder.getAbsolutePath());
		RemoteOperationResult result = downloadOperation.execute(mClient);

		return result;
	}
	
	/** Access to the library method to Upload a File 
	 * @param storagePath
	 * @param remotePath
	 * @param mimeType
	 * @param requiredEtag
	 * 
	 * @return
	 */
	public RemoteOperationResult uploadFile(String storagePath, String remotePath, String mimeType,
											String requiredEtag) {
		return TestActivity.uploadFile(this, storagePath, remotePath, mimeType, mClient, requiredEtag);
	}


	/** Access to the library method to Upload a File
	 *
	 * @param context
	 * @param storagePath
	 * @param remotePath
	 * @param mimeType
	 * @param client       Client instance configured to access the target OC server.
	 * @param requiredEtag
	 * @return
	 */
	public static RemoteOperationResult uploadFile(Context context, String storagePath, String remotePath,
												   String mimeType, OwnCloudClient client, String requiredEtag) {

		String fileLastModifTimestamp = getFileLastModifTimeStamp(storagePath);

        UploadFileRemoteOperation uploadOperation;

        if ((new File(storagePath)).length() > ChunkedFileUploadRemoteOperation.CHUNK_SIZE) {
            uploadOperation = new ChunkedFileUploadRemoteOperation(
					context, storagePath, remotePath, mimeType, requiredEtag, fileLastModifTimestamp
			);
		} else {
            uploadOperation = new UploadFileRemoteOperation(
                    storagePath, remotePath, mimeType, requiredEtag, fileLastModifTimestamp
    		);
        }

		return uploadOperation.execute(client);
	}

	/** Access to the library method to Get Shares 
	 * 
	 * @return
	 */
	public RemoteOperationResult getShares() {
		GetRemoteSharesOperation getOperation = new GetRemoteSharesOperation();
		return getOperation.execute(mClient);
	}
	
	/** Access to the library method to Create Share
	 * @param path			Full path of the file/folder being shared. Mandatory argument
	 * @param shareType		0 = user, 1 = group, 3 = Public link. Mandatory argument
	 * @param shareWith		User/group ID with who the file should be shared.  This is mandatory for shareType of 0 or 1
	 * @param publicUpload	If false (default) public cannot upload to a public shared folder.
	 * 						If true public can upload to a shared folder. Only available for public link shares
	 * @param password		Password to protect a public link share. Only available for public link shares
	 * @param permissions	1 - Read only  Default for public shares
	 * 						2 - Update
	 * 						4 - Create
	 * 						8 - Delete
	 * 						16- Re-share
	 * 						31- All above Default for private shares
	 * 						For user or group shares.
	 * 						To obtain combinations, add the desired values together.  
	 * 						For instance, for Re-Share, delete, read, update add 16+8+2+1 = 27.
	 * 
	 * @return
	 */
	public RemoteOperationResult createShare(String path, ShareType shareType, String shareWith, boolean publicUpload, 
			String password, int permissions){
		
		CreateRemoteShareOperation createOperation = new CreateRemoteShareOperation(path, shareType, shareWith,
				publicUpload, password, permissions);
		return createOperation.execute(mClient);
	}
	
	
	/**
	 * Access to the library method to Remove Share
	 * 
	 * @param idShare	Share ID
	 */
	
	public RemoteOperationResult removeShare(int idShare) {
		RemoveRemoteShareOperation removeOperation = new RemoveRemoteShareOperation(idShare);
		return removeOperation.execute(mClient);
	}

	
	/**
	 * Extracts file from AssetManager to cache folder.
	 * 
	 * @param	fileName	Name of the asset file to extract.
	 * @return				File instance of the extracted file.
	 */
	public File extractAsset(String fileName) throws IOException {
		return TestActivity.extractAsset(fileName, this);
	}
	
	/**
	 * Extracts file from AssetManager to cache folder.
	 * 
	 * @param	fileName	Name of the asset file to extract.
	 * @param	context		Android context to access assets and file system.
	 * @return				File instance of the extracted file.
	 */
	public static File extractAsset(String fileName, Context context) throws IOException {
		File extractedFile = new File(context.getCacheDir() + File.separator + fileName);
		if (!extractedFile.exists()) {
			InputStream in = null;
			FileOutputStream out = null;
			in = context.getAssets().open(fileName);
			out = new FileOutputStream(extractedFile);
			byte[] buffer = new byte[BUFFER_SIZE];
			int readCount;
			while((readCount = in.read(buffer)) != -1){
				out.write(buffer, 0, readCount);
			}
			out.flush();
			out.close();
			in.close();
		}
		return extractedFile;
	}

    private static String getFileLastModifTimeStamp (String storagePath) {
        File file = new File(storagePath);
        Long timeStampLong = file.lastModified()/1000;
        return timeStampLong.toString();
    }
}
