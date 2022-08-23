package com.backend.awsimageupload.dataStore;

import com.backend.awsimageupload.profile.UserProfile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*
 * @created 20/08/2022 - 23:07
 * @author quang
 * @project aws-image-upload
 */
@Repository
public class FakeUserProfileDataStore {

    private static final List<UserProfile> USER_PROFILES = new ArrayList<>();
    static {
        USER_PROFILES.add(new UserProfile("1", "quang", null));
        USER_PROFILES.add(new UserProfile("2", "quang2", null));
    }

    public List<UserProfile> getUserProfiles() {
        return USER_PROFILES;
    }
}
