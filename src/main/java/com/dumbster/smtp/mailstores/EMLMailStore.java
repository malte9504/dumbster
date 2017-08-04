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
package com.dumbster.smtp.mailstores;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import com.dumbster.smtp.MailMessage;
import com.dumbster.smtp.MailStore;
import com.dumbster.smtp.eml.EMLMailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Store messages as EML files.
 * <br/>
 * This class makes no guarantees as to the order of the received messages.
 * The messages are stored in order but getMessages won't return messages in the same order they were received.
 */
public class EMLMailStore implements MailStore
{
    private final Logger LOG = LoggerFactory.getLogger(EMLMailStore.class);

    private AtomicBoolean initialized = new AtomicBoolean();
    private int count = 0;
    private File directory = new File("eml_store");
    private List<MailMessage> messages = new ArrayList<MailMessage>();

    /**
     * Checks if mail mailStore is initialized and initializes it if it's not.
     */
    private void checkInitialized()
    {
        synchronized(this) {
            if (!initialized.getAndSet(true)) {
                if (!directory.exists()) {
                    LOG.debug(format("Creating directory '%s'", directory.getAbsolutePath()));
                    if (!directory.mkdirs()) {
                        throw new UncheckedIOException(new IOException(format("Could not create '%s'", directory.getAbsolutePath())));
                    }
                }
                else {
                    loadMessages();
                }
            }
        }
    }

    /**
     * Load previous messages from directory.
     */
    private void loadMessages()
    {
        File[] files = loadMessageFiles();

        for (File file : files) {
            MailMessage message = new EMLMailMessage(file);
            messages.add(message);
        }
        count = files.length;
    }

    /**
     * Load message files from mailStore directory.
     *
     * @return an array of {@code File}
     */
    private File[] loadMessageFiles()
    {
        File[] files = this.directory.listFiles(new EMLFilenameFilter());
        if (files == null) {
            LOG.error(format("Unable to load messages from eml mailStore directory '%s'", directory));
            return new File[0];
        }
        return files;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getEmailCount()
    {
        checkInitialized();
        synchronized(this) {
            return count;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMessage(MailMessage message)
    {
        requireNonNull(message, "message is null");

        checkInitialized();

        File file;
        synchronized(this) {
            count++;
            String filename = getFilename(message, count);
            file = new File(directory, filename);
            messages.add(message);
        }

        LOG.debug("Received message: " + message);

        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
                OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.ISO_8859_1)) {

            for (Iterator<String> i = message.getHeaderNames(); i.hasNext();) {
                String name = i.next();
                String[] values = message.getHeaderValues(name);
                for (String value : values) {
                    writer.append(name);
                    writer.append(": ");
                    writer.append(value);
                    writer.append('\n');
                }
            }
            writer.append('\n');
            writer.append(message.getBody());
            writer.append('\n');
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String getFilename(MailMessage message, int count)
    {
        requireNonNull(message, "message is null");

        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0");
        }

        String filename = new StringBuilder().append(count)
            .append("_")
            .append(message.getFirstHeaderValue("Subject"))
            .append(".eml")
            .toString();
        filename = filename.replaceAll("[\\\\/<>\\?>\\*\"\\|]", "_");
        return filename;
    }

    /**
     * Return a list of messages stored by this mail mailStore.
     *
     * @return a list of {@code EMLMailMessage}
     */
    @Override
    public MailMessage[] getMessages()
    {
        checkInitialized();

        synchronized (this) {
            return messages.toArray(new MailMessage[messages.size()]);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MailMessage getMessage(int index)
    {
        return getMessages()[index];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearMessages()
    {
        synchronized(this) {
            File [] files = this.directory.listFiles(new EMLFilenameFilter());
            if (files != null) {
                for (File file : files) {
                    if (!file.delete()) {
                        LOG.warn(format("Could not delete '%s'", file.getAbsolutePath()));
                    }
                }
            }

            count = 0;
            messages.clear();
        }
    }

    public void setDirectory(String directory)
    {
        requireNonNull(directory, "directory is null");
        setDirectory(new File(directory));
    }

    public void setDirectory(File directory)
    {
        requireNonNull(directory, "directory is null");
        synchronized(this) {
            this.directory = directory;
        }
    }

    /**
     * Filter only files matching name of files saved by EMLMailStore.
     */
    public static class EMLFilenameFilter implements FilenameFilter
    {
        private final Pattern PATTERN = Pattern.compile("\\d+_.*\\.eml");
        private final Matcher MATCHER = PATTERN.matcher("");

        @Override
        public boolean accept(File dir, String name)
        {
            MATCHER.reset(name);
            return MATCHER.matches();
        }

    }
}
