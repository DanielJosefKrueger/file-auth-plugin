/*
 * Copyright 2015 dc-square GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.hivemq.plugin.fileauthentication.configuration;

import com.hivemq.plugin.fileauthentication.callback.CredentialChangeCallback;
import com.hivemq.spi.config.SystemInformation;
import com.hivemq.spi.services.PluginExecutorService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Götz
 */
public class CredentialsConfiguration extends ReloadingPropertiesReader {

    private final String filename;
    private final int reloadSeconds;
    private final List<CredentialChangeCallback> callbacks;
    private int previousCredentialsHash;


    @Inject
    public CredentialsConfiguration(final PluginExecutorService pluginExecutorService, final String filename, final int reloadSeconds, final SystemInformation systemInformation) {
        super(pluginExecutorService, systemInformation);
        this.callbacks = new ArrayList<>();
        this.filename = filename;
        this.reloadSeconds = reloadSeconds;
    }

    public String getUser(final String username) {

        String pw = properties.getProperty(username);
        if ((pw == null) || (pw.equals(""))) {
            return null;
        }
        return properties.getProperty(username);
    }


    @Override
    void afterReload() {
        if (this.hashCode() != previousCredentialsHash) {
            for (CredentialChangeCallback credentialChangeCallback : callbacks) {
                credentialChangeCallback.onCredentialChange();
            }
        }
        this.previousCredentialsHash = this.hashCode();
    }

    /**
     * @param newCallback the {@link CredentialChangeCallback} that should be performed after credential got changed
     * @return true: callback was registered successfully, otherwise false
     */
    public boolean addCallback(CredentialChangeCallback newCallback) {

        if (newCallback == null)
            throw new NullPointerException("null is not allowed as Callback");

        if (!this.callbacks.contains(newCallback)) {
            this.callbacks.add(newCallback);
            return true;
        }
        return false;

    }


    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public int getReloadIntervalinSeconds() {
        return reloadSeconds;
    }

    @Override
    public int hashCode() {

        int result = filename != null ? filename.hashCode() : 0;
        result = 31 * result + reloadSeconds;
        result = 31 * result + properties.size();
        //used because properties.hashcode doesnt change then properties change
        result = 31 * result + (properties.toString().hashCode());
        return result;
    }

}