/*******************************************************************************
 * Copyright (c) 2019-10-30 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation.
 ******************************************************************************/

import junit.framework.TestCase;
import org.hitchain.Start;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * ApplicationTest
 *
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>
 * @since 2019-10-30
 * auto generate by qdp.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Start.class})// 指定启动类
public class ApplicationTest {
    @Test
    public void testOne() {
        System.out.println("test hello 1");
    }

    @Test
    public void testTwo() {
        System.out.println("test hello 2");
        TestCase.assertEquals(1, 1);
    }

    @Before
    public void testBefore() {
        System.out.println("before");
    }

    @After
    public void testAfter() {
        System.out.println("after");
    }
}
