package com.backend.awsimageupload.service;

import com.backend.awsimageupload.dataStore.FakeUserProfileDataStore;
import com.backend.awsimageupload.fileStore.FileStore;
import com.backend.awsimageupload.profile.UserProfile;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/*
 * @created 20/08/2022 - 23:23
 * @author quang
 * @project aws-image-upload
 */
@Service
public class UserProfileService {

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    private final FakeUserProfileDataStore dataStore;
    private final FileStore fileStore;

    @Autowired
    public UserProfileService(FakeUserProfileDataStore dataStore, FileStore fileStore) {
        this.dataStore = dataStore;
        this.fileStore = fileStore;
    }

    public List<UserProfile> getUserProfiles() {
        return dataStore.getUserProfiles();
    }

    public void uploadUserProfileImage(String userProfileId, MultipartFile file) {
        // check if file is empty
        isFileEmpty(file);
        // if file not an image
        isImage(file);
        //user exists in db
        UserProfile profile = getUserProfileOrThrow(userProfileId);

        Map<String, String> metaData = new HashMap<>();
        metaData.put("Content-Type", file.getContentType());
        metaData.put("Content-Length", String.valueOf(file.getSize()));

        // put to s3
        String path = String.format("%s/%s", bucketName, userProfileId);
        String fileName = String.format("%s-%s", file.getOriginalFilename(), UUID.randomUUID());
        try {
            fileStore.save(path, fileName, Optional.of(metaData), file.getInputStream());
            profile.setUserProfileImageLink(fileName);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }

    private UserProfile getUserProfileOrThrow(String userProfileId) {
        return dataStore.getUserProfiles()
                .stream()
                .filter(userProfile -> userProfile.getUserProfileId().equals(userProfileId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("User profile: %s not found", userProfileId)));
    }

    private static void isImage(MultipartFile file) {
        if(!Arrays.asList(ContentType.IMAGE_JPEG.getMimeType(), ContentType.IMAGE_PNG.getMimeType(), ContentType.IMAGE_GIF.getMimeType()).contains(file.getContentType())) {
            throw new IllegalStateException("file must be image");
        }
    }

    private void isFileEmpty(MultipartFile file) {
        if(file.isEmpty()) {
            throw new IllegalStateException("cannot upload empty file to S3");
        }
    }

    public byte[] downloadImageFromS3(String userProfileId) {
        UserProfile profile = getUserProfileOrThrow(userProfileId);
        String path = String.format("%s/%s", bucketName, userProfileId);
        return profile.getUserProfileImageLink().map(key -> fileStore.download(path, key)).orElse(new byte[0]);
    }
}
