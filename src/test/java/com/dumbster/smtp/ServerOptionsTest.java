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
        assertEquals(true, options.valid);
        assertEquals(25, options.port);
        assertEquals(true, options.threaded);
        assertEquals(RollingMailStore.class, options.mailStore.getClass());
    }

    @Test
    public void emptyOptions()
    {
        String[] args = new String[] {};
        options = new ServerOptions(args);
        assertEquals(true, options.valid);
        assertEquals(25, options.port);
        assertEquals(true, options.threaded);
        assertEquals(RollingMailStore.class, options.mailStore.getClass());
    }

    @Test
    public void optionMailStoreEMLMailStore()
    {
        String[] args = new String[] {"--mailStore=EMLMailStore"};
        options = new ServerOptions(args);
        assertEquals(EMLMailStore.class, options.mailStore.getClass());
        assertEquals(true, options.valid);
        assertEquals(25, options.port);
        assertEquals(true, options.threaded);
    }

    @Test
    public void optionMailStoreInvalid()
    {
        String[] args = new String[] {"--mailStore"};
        options = new ServerOptions(args);
        assertEquals(false, options.valid);
    }

    @Test
    public void badMailStore()
    {
        String[] args = new String[] {"--mailStore=foo"};
        options = new ServerOptions(args);
        assertEquals(RollingMailStore.class, options.mailStore.getClass());
        assertEquals(false, options.valid);
    }

    @Test
    public void threaded()
    {
        String[] args = new String[] {"--threaded"};
        options = new ServerOptions(args);
        assertEquals(true, options.threaded);
        assertEquals(true, options.valid);
        assertEquals(25, options.port);
        assertEquals(RollingMailStore.class, options.mailStore.getClass());
    }

    @Test
    public void notThreaded()
    {
        String[] args = new String[] {"--threaded=false"};
        options = new ServerOptions(args);
        assertEquals(false, options.threaded);
        assertEquals(true, options.valid);
        assertEquals(25, options.port);
        assertEquals(RollingMailStore.class, options.mailStore.getClass());
    }

    @Test
    public void alternativePort()
    {
        String[] args = new String[] {"12345"};
        options = new ServerOptions(args);
        assertEquals(12345, options.port);
        assertEquals(true, options.threaded);
        assertEquals(true, options.valid);
        assertEquals(RollingMailStore.class, options.mailStore.getClass());
    }

    @Test
    public void badPort()
    {
        String[] args = new String[] {"invalid"};
        options = new ServerOptions(args);
        assertEquals(false, options.valid);
    }


}
