package com.teksystems.library;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
        LibraryApplicationTests.class,
        SpringJPAUnitTests.class
})

//run all 11 unit tests
public class JunitTestSuite {
}

