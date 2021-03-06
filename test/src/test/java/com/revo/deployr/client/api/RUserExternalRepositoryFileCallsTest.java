/*
 * RUserExternalRepositoryFileCallsTest.java
 *
 * Copyright (C) 2010-2016, Microsoft Corporation
 *
 * This program is licensed to you under the terms of Version 2.0 of the
 * Apache License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * Apache License 2.0 (http://www.apache.org/licenses/LICENSE-2.0) for more details.
 *
 */
package com.revo.deployr.client.api;

import com.revo.deployr.DeployrUtil;
import com.revo.deployr.client.RClient;
import com.revo.deployr.client.RClientException;
import com.revo.deployr.client.RRepositoryFile;
import com.revo.deployr.client.RUser;
import com.revo.deployr.client.auth.basic.RBasicAuthentication;
import com.revo.deployr.client.factory.RClientFactory;
import com.revo.deployr.client.params.RepoUploadOptions;
import org.junit.*;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

public class RUserExternalRepositoryFileCallsTest {

    RClient rClient = null;
    RUser rUser = null;

    public RUserExternalRepositoryFileCallsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        try {
            String url = System.getProperty("connection.protocol") +
                            System.getProperty("connection.endpoint");
            if (url == null) {
                fail("setUp: connection.[protocol|endpoint] null.");
            }
            boolean allowSelfSigned = 
                Boolean.valueOf(System.getProperty("allow.SelfSignedSSLCert"));
            rClient =RClientFactory.createClient(url, allowSelfSigned);
            RBasicAuthentication rAuthentication = new RBasicAuthentication("testuser", System.getProperty("password.testuser"));
            String expResultName = "testuser";
            rUser = rClient.login(rAuthentication);
            assertNotNull(rUser);

        } catch (Exception ex) {

            if (rClient != null) {
                rClient.release();
            }
            fail("setUp: " + ex);
        }
    }

    @After
    public void tearDown() {
        if (rClient != null) {
            rClient.release();
        }
    }

    /**
     * Test RUserRepositoryFileCalls.listExternalFiles().
     */
    @Test
    public void testUserRepositoryListExternalFiles() {

        // Test variables.
        List<RRepositoryFile> listExternalFiles = null;
        int filesFoundOwnedByCaller = 0;
        int filesFoundNotOwnedByCaller = 0;

        // Test error handling.
        Exception exception = null;
        String exceptionMsg = "";
        Exception cleanupException = null;
        String cleanupExceptionMsg = "";

        // Test.
        try {
            listExternalFiles = rUser.listExternalFiles();
        } catch (Exception ex) {
            exception = ex;
            exceptionMsg = "rUser.listExternalFiles failed: ";
        }

        try {
            // Determine if files authored by other users 
            // were found on the response. The listExternalFiles()
            // call should only return files authored by the caller.
            for (RRepositoryFile file : listExternalFiles) {
                if (file.about().authors.contains("testuser")) {
                    filesFoundOwnedByCaller += 1;
                } else {
                    filesFoundNotOwnedByCaller += 1;
                }
            }
        } catch (Exception ex) {
            exception = ex;
            exceptionMsg = "file.about().authors.contains failed: ";
        }

        // Test cleanup.
        if (exception == null) {
            // Test assertions.
            assertEquals(filesFoundOwnedByCaller, listExternalFiles.size());
            assertEquals(filesFoundNotOwnedByCaller, 0);
        } else {
            fail(exceptionMsg + exception.getMessage());
        }

        // Test cleanup errors.
        if (cleanupException != null) {
            fail(cleanupExceptionMsg + cleanupException.getMessage());
        }
    }

    /**
     * Test RUserRepositoryFileCalls.listExternalFiles(categoryFilter, directoryFilter).
     */
    @Test
    public void testUserRepositoryListExternalFilesGoodFilters() {

        // Test variables.
        List<RRepositoryFile> listFilesExampleDirectory = null;
        List<RRepositoryFile> listFilesExampleScripts = null;
        List<RRepositoryFile> listFilesExampleBinary = null;
        String exampleFraudScoreDirectory = "external:root:example-fraud-score";
        int fraudExampleTotalFileCount = 2;
        int fraudExampleScriptFileCount = 1;
        boolean fraudExampleScriptsAreScripts = false;
        int fraudExampleBinaryFileCount = 1;
        boolean fraudExampleBinaryAreBinary = false;

        // Test error handling.
        Exception exception = null;
        String exceptionMsg = "";
        Exception cleanupException = null;
        String cleanupExceptionMsg = "";

        // Test.
        try {
            listFilesExampleDirectory =
                rUser.listExternalFiles((RRepositoryFile.Category) null,
                                        exampleFraudScoreDirectory);
        } catch (Exception ex) {
            exception = ex;
            exceptionMsg = "rUser.listExternalFiles(null, directory) failed: ";
        }

        if(exception == null) {

            try {
                listFilesExampleScripts =
                    rUser.listExternalFiles(RRepositoryFile.Category.RSCRIPT,
                                        exampleFraudScoreDirectory);
                    
                for(RRepositoryFile scriptFile : listFilesExampleScripts) {
                    if(scriptFile.about().category !=
                                        RRepositoryFile.Category.RSCRIPT) {
                        fraudExampleScriptsAreScripts = false;
                        break;
                    } else {
                        fraudExampleScriptsAreScripts = true;
                    }
                }
            } catch (Exception ex) {
                exception = ex;
                exceptionMsg = "rUser.listExternalFiles(RSCRIPT, directory) failed: ";
            }
        }

        if(exception == null) {

            try {
                listFilesExampleBinary =
                    rUser.listExternalFiles(RRepositoryFile.Category.RBINARY,
                                        exampleFraudScoreDirectory);

                for(RRepositoryFile binFile : listFilesExampleBinary) {
                    if(binFile.about().category !=
                                        RRepositoryFile.Category.RBINARY) {
                        fraudExampleBinaryAreBinary = false;
                        break;
                    } else {
                        fraudExampleBinaryAreBinary = true;
                    }
                }
            } catch (Exception ex) {
                exception = ex;
                exceptionMsg = "rUser.listExternalFiles(RSCRIPT, directory) failed: ";
            }
        }

        if (exception == null) {
            // Test assertions.
            assertEquals(fraudExampleTotalFileCount, listFilesExampleDirectory.size());
            assertEquals(fraudExampleScriptFileCount, listFilesExampleScripts.size());
            assertTrue(fraudExampleScriptsAreScripts);
            assertEquals(fraudExampleBinaryFileCount, listFilesExampleBinary.size());
            assertTrue(fraudExampleBinaryAreBinary);
        } else {
            fail(exceptionMsg + exception.getMessage());
        }

        // Test cleanup errors.
        if (cleanupException != null) {
            fail(cleanupExceptionMsg + cleanupException.getMessage());
        }
    }

    /**
     * Test RUserRepositoryFileCalls.listExternalFiles(categoryFilter, directoryFilter).
     */
    @Test
    public void testUserRepositoryListExternalFilesBadFilters() {

        // Test variables.
        List<RRepositoryFile> listFiles = null;
        RClientException clientEx = null;

        // Test error handling.
        Exception exception = null;
        String exceptionMsg = "";
        Exception cleanupException = null;
        String cleanupExceptionMsg = "";

        // Test.
        try {
            listFiles =
                rUser.listExternalFiles((RRepositoryFile.Category) null,
                                        "external:public:dir-not-found");
        } catch (RClientException cex) {
            clientEx = cex;
        } catch (Exception ex) {
            exception = ex;
            exceptionMsg = "rUser.listExternalFiles(null, dir-not-found) failed: ";
        }

        if (exception == null) {
            // Test assertions.
            assertNotNull(clientEx);
        } else {
            fail(exceptionMsg + exception.getMessage());
        }

        // Test cleanup errors.
        if (cleanupException != null) {
            fail(cleanupExceptionMsg + cleanupException.getMessage());
        }
    }

    /**
     * Test RUserRepositoryFileCalls.listExternalFiles(true, true).
     */
    @Test
    public void testUserRepositoryListExternalFilesSharedPublic() {

        // Test variables.
        RUser rAdminUser = null;
        List<RRepositoryFile> listExternalFiles = null;
        List<RRepositoryFile> listExternalFilesSharedPublic = null;
        int filesFoundOwnedByCaller = 0;
        int filesFoundNotOwnedByCaller= 0;

        // Test error handling.
        Exception exception = null;
        String exceptionMsg = "";
        Exception cleanupException = null;
        String cleanupExceptionMsg = "";

        // Test.
        try {
            rClient.logout(rUser);
        } catch (Exception ex) {
            exception = ex;
            exceptionMsg = "rClient.logout failed: ";
        }

        if (exception == null) {
            RBasicAuthentication rAuthentication =
                new RBasicAuthentication("admin", System.getProperty("password.admin"));
            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ex) {
                    exception = ex;
                    exceptionMsg = "Thread.sleep failed: ";
                }
                if (exception == null) {
                    try {
                        rAdminUser = rClient.login(rAuthentication);
                    } catch (Exception ex) {
                        if (i < 5) {
                            continue;
                        }
                        exception = ex;
                        exceptionMsg = "rClient.login failed: ";
                    }
                }
            }
        }

        if(exception == null) {
            try {
                // Retrieve list of external files owned by admin.
                listExternalFiles = rAdminUser.listExternalFiles();
            } catch (Exception ex) {
                exception = ex;
                exceptionMsg = "rAdminUser.listExternalFiles failed: ";
            }
        }

        if(exception == null) {
            try {
                // Retrieve list of external files owned by admin
                // plus list of external files owned by other users
                // that have been shared or public.
                listExternalFilesSharedPublic =
                    rAdminUser.listExternalFiles(true, true);
            } catch (Exception ex) {
                exception = ex;
                exceptionMsg = "rAdminUser.listExternalFiles(true, true) failed: ";
            }
        }

        if(exception == null) {

            try {

                for (RRepositoryFile file : listExternalFilesSharedPublic) {
                    if (file.about().authors.contains("admin")) {
                        filesFoundOwnedByCaller += 1;
                    } else {
                        filesFoundNotOwnedByCaller += 1;
                    }
                }
            } catch (Exception ex) {
                exception = ex;
                exceptionMsg = "file.about().authors.contains failed: ";
            }

        }

        // Test cleanup.
        if (exception == null) {
            // Test assertions.
            assertEquals(filesFoundOwnedByCaller, listExternalFiles.size());
            assertEquals(filesFoundNotOwnedByCaller,
                listExternalFilesSharedPublic.size() - listExternalFiles.size());
        } else {
            fail(exceptionMsg + exception.getMessage());
        }

        // Test cleanup errors.
        if (cleanupException != null) {
            fail(cleanupExceptionMsg + cleanupException.getMessage());
        }
    }

}
