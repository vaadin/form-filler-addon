package org.vaadin.addons.ai.formfiller.services;

import com.vaadin.flow.internal.UsageStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/*
 * Copyright 2000-2023 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


/**
 * Reports vaadin-form-filler statistics. Internal.
 */
public class FormFillerStats {

    // Use these values as fallbacks only:
    private static final String PRODUCT_NAME = "vaadin-form-filler-addon";
    private static final String PRODUCT_VERSION = "1.0.0-SNAPSHOT";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(FormFillerStats.class);


    static {
        report();
    }

    private static Optional<String> getVaadinFormVersion() {
        try (final InputStream pomProperties = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(
                        "META-INF/pom.xml")) {
            if (pomProperties != null) {
                final Properties properties = new Properties();
                properties.load(pomProperties);
                return Optional.of(properties.getProperty("version", PRODUCT_VERSION));
            } else {
                // Potentially if we want to tie it with the Vaadin version then:
                // Platform.getVaadinVersion().orElse(PRODUCT_VERSION)
                return Optional.empty();
            }
        } catch (Exception e) {
            LOGGER.error("Unable to determine vaadin-form-filler-addon version will use fallback version: {}",
                    PRODUCT_VERSION,
                    e);
        }
        return Optional.empty();
    }

    public static void report() {
        UsageStatistics.markAsUsed(PRODUCT_NAME,
                getVaadinFormVersion().orElse(PRODUCT_VERSION));
    }
}