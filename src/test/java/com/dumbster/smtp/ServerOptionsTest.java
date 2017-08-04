/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dumbster.smtp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.dumbster.smtp.mailstores.EMLMailStore;
import com.dumbster.smtp.mailstores.RollingMailStore;
import org.junit.Test;

/**
 * User: rj
 * Date: 7/21/13
 * Time: 8:20 AM
 */
public class ServerOptionsTest
{

    private ServerOptions options;


    @Test
    public void defaultConfiguration()
    {
        options = new ServerOptions();
        assertEquals(25, options.getPort());
        assertEquals(true, options.isThreaded());
        assertEquals(RollingMailStore.class, options.getMailStore().getClass());
    }

    @Test
    public void emptyOptions()
    {
        String[] args = new String[] {};
        options = new ServerOptions(args);
        assertEquals(25, options.getPort());
        assertEquals(true, options.isThreaded());
        assertEquals(RollingMailStore.class, options.getMailStore().getClass());
    }

    @Test
    public void optionMailStoreEMLMailStore()
    {
        String[] args = new String[] {"--mailStore=EMLMailStore"};
        options = new ServerOptions(args);
        assertEquals(EMLMailStore.class, options.getMailStore().getClass());
        assertEquals(25, options.getPort());
        assertEquals(true, options.isThreaded());
    }

    @Test
    public void optionMailStoreInvalid()
    {
        String[] args = new String[] {"--mailStore"};
        try {
            options = new ServerOptions(args);
            fail();
        }
        catch (Throwable t) {
            assertEquals(IllegalArgumentException.class, t.getClass());
        }
    }

    @Test
    public void badMailStore()
    {
        String[] args = new String[] {"--mailStore=foo"};
        try {
            options = new ServerOptions(args);
            fail();
        }
        catch (Throwable t) {
            assertEquals(IllegalArgumentException.class, t.getClass());
        }
    }

    @Test
    public void threaded()
    {
        String[] args = new String[] {"--threaded"};
        options = new ServerOptions(args);
        assertEquals(true, options.isThreaded());
        assertEquals(25, options.getPort());
        assertEquals(RollingMailStore.class, options.getMailStore().getClass());
    }

    @Test
    public void notThreaded()
    {
        String[] args = new String[] {"--threaded=false"};
        options = new ServerOptions(args);
        assertEquals(false, options.isThreaded());
        assertEquals(25, options.getPort());
        assertEquals(RollingMailStore.class, options.getMailStore().getClass());
    }

    @Test
    public void alternativePort()
    {
        String[] args = new String[] {"12345"};
        options = new ServerOptions(args);
        assertEquals(12345, options.getPort());
        assertEquals(true, options.isThreaded());
        assertEquals(RollingMailStore.class, options.getMailStore().getClass());
    }

    @Test
    public void badPort()
    {
        String[] args = new String[] {"invalid"};

        try {
            options = new ServerOptions(args);
            fail();
        }
        catch (Throwable t) {
            assertEquals(IllegalArgumentException.class, t.getClass());
        }
    }


}
