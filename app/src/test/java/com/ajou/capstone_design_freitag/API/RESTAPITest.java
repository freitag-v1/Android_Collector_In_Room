package com.ajou.capstone_design_freitag.API;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RESTAPITest {

    @Test
    public void getInstance() {
        assertNotNull(RESTAPI.getInstance());
    }

    @Test
    public void login() {
        assertTrue(RESTAPI.getInstance().login("test","1234"));
    }

    @Test
    public void notRegisterdUserlogin() {
        assertFalse(RESTAPI.getInstance().login("dddd","1234"));
    }

    @Test
    public void randomIdSignup() {
        String randomId = UUID.randomUUID().toString().substring(0,7);
        assertTrue(RESTAPI.getInstance().signup(randomId, "1234", "testtest", "01027540421", "wodnd999999@ajou.ac.kr", "Ajou Univ."));
    }

    @Test
    public void tooLongIdSignup() {
        String randomId = UUID.randomUUID().toString();
        assertFalse(RESTAPI.getInstance().signup(randomId, "1234", "testtest", "01027540421", "wodnd999999@ajou.ac.kr", "Ajou Univ."));
    }

    @Test
    public void redundantSignup() {
        assertFalse(RESTAPI.getInstance().signup("test", "1234", "testtest", "01027540421", "wodnd999999@ajou.ac.kr", "Ajou Univ."));
    }
}