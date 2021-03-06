package com.cloudinary.test;

import com.cloudinary.Api;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.api.exceptions.AlreadyExists;
import com.cloudinary.utils.ObjectUtils;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

abstract public class AbstractStreamingProfilesApiTest extends MockableTest {
    private static final String PROFILE_NAME = "api_test_streaming_profile" + SUFFIX;
    protected Api api;
    private static final List<String> PREDEFINED_PROFILES = Arrays.asList("4k", "full_hd", "hd", "sd", "full_hd_wifi", "full_hd_lean", "hd_lean");

    @BeforeClass
    public static void setUpClass() throws IOException {
        Cloudinary cloudinary = new Cloudinary();
        if (cloudinary.config.apiSecret == null) {
            System.err.println("Please setup environment for Upload test to run");
        }
    }

    @Rule
    public TestName currentTest = new TestName();

    @Before
    public void setUp() {
        System.out.println("Running " + this.getClass().getName() + "." + currentTest.getMethodName());
        this.cloudinary = new Cloudinary();
        assumeNotNull(cloudinary.config.apiSecret);
        this.api = cloudinary.api();
    }

    @Test
    public void testCreate() throws Exception {
        final String name = PROFILE_NAME + "_create";
        ApiResponse result = api.createStreamingProfile(name, null, Collections.singletonList(ObjectUtils.asMap(
                "transformation", new Transformation().crop("limit").width(1200).height(1200).bitRate("5m")
        )), ObjectUtils.emptyMap());

        assertTrue(result.containsKey("data"));
        Map profile = (Map) result.get("data");
        assertThat(profile, (Matcher) hasEntry("name", (Object) name));
    }

    @Test
    public void testGet() throws Exception {
        ApiResponse result = api.getStreamingProfile(PREDEFINED_PROFILES.get(0));
        assertTrue(result.containsKey("data"));
        Map profile = (Map) result.get("data");
        assertThat(profile, (Matcher) hasEntry("name", (Object) (PREDEFINED_PROFILES.get(0))));

    }

    @Test
    public void testList() throws Exception {
        ApiResponse result = api.listStreamingProfiles();
        assertTrue(result.containsKey("data"));
        List profiles = (List) result.get("data");
        // check that the list contains all predefined profiles
        for (String p :
                PREDEFINED_PROFILES) {
            assertThat(profiles, (Matcher) hasItem(hasEntry("name", p)));
        }
    }

    @Test
    public void testDelete() throws Exception {
        ApiResponse result;
        final String name = PROFILE_NAME + "_delete";
        try {
            api.createStreamingProfile(name, null, Collections.singletonList(ObjectUtils.asMap(
                    "transformation", new Transformation().crop("limit").width(1200).height(1200).bitRate("5m")
            )), ObjectUtils.emptyMap());
        } catch (AlreadyExists ignored) {
        }

        result = api.deleteStreamingProfile(name);
        assertTrue(result.containsKey("data"));
        Map profile = (Map) result.get("data");
        assertThat(profile, (Matcher) hasEntry("name", (Object) (name)));

    }

    @Test
    public void testUpdate() throws Exception {
        final String name = PROFILE_NAME + "_update";
        try {
            api.createStreamingProfile(name, null, Collections.singletonList(ObjectUtils.asMap(
                    "transformation", new Transformation().crop("limit").width(1200).height(1200).bitRate("5m")
            )), ObjectUtils.emptyMap());
        } catch (AlreadyExists ignored) {
        }
        Map result = api.updateStreamingProfile(name, null, Collections.singletonList(
                ObjectUtils.asMap("transformation",
                        new Transformation().crop("limit").width(800).height(800).bitRate("5m")
                )), ObjectUtils.emptyMap());

        assertTrue(result.containsKey("data"));
        assertThat(result, (Matcher) hasEntry("message", (Object) "updated"));
        Map profile = (Map) result.get("data");
        assertThat(profile, (Matcher) hasEntry("name", (Object) (name)));
        assertThat(profile, Matchers.hasEntry(equalTo("representations"), (Matcher) hasItem(hasKey("transformation"))));
        final Map representation = (Map) ((List) profile.get("representations")).get(0);
        Map transformation = (Map) ((List)representation.get("transformation")).get(0);
        assertThat(transformation, allOf(
                (Matcher) hasEntry("width", 800),
                (Matcher) hasEntry("height", 800),
                (Matcher) hasEntry("crop", "limit"),
                (Matcher) hasEntry("bit_rate", "5m")
        ));
    }
}
